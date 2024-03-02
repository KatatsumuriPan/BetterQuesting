package betterquesting.client.gui2.editors.nbt;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.lwjgl.input.Keyboard;

import betterquesting.NBTUtil;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.callbacks.CallbackMulti;
import betterquesting.api2.client.gui.controls.callbacks.CallbackNBTPrimitive;
import betterquesting.api2.client.gui.controls.callbacks.CallbackNBTTagString;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

// Self contained editing panel
// TODO: Add ability for expansions to register modules for identifying and providing custom editors to various NBT data sets (inventory, tinker tool, magic,
// etc.)
// TODO: This however should not be forced in the event of incorrect identification. Open as...
public class PanelScrollingNBT extends CanvasScrolling implements IPEventListener {

    private NBTBase nbt;

    private final int btnEdit;
    private final int btnAdv;
    private final int btnUp;
    private final int btnDown;
    private final int btnInsert;
    private final int btnDelete;

    // TODO: Set a special tag that tells the editor about special serialised objects that should be handled in a special way.
    // Considering taglists can't contain extra data there may need a way to pass a special manifest file (via NbtDocs?)
    // This tag type should be hidden in by the editor but auto-changed when manually switch to a new type

    private final Stack<NBTBase> nbtStack = new Stack<>();

    public PanelScrollingNBT(IGuiRect rect, NBTTagCompound tag, int btnEdit, int btnAdv, int btnUp, int btnDown, int btnInsert, int btnDelete) {
        this(rect, btnEdit, btnAdv, btnUp, btnDown, btnInsert, btnDelete);

        this.setNBT(tag);
    }

    public PanelScrollingNBT(IGuiRect rect, NBTTagList tag, int btnEdit, int btnAdv, int btnUp, int btnDown, int btnInsert, int btnDelete) {
        this(rect, btnEdit, btnAdv, btnUp, btnDown, btnInsert, btnDelete);

        this.setNBT(tag);
    }

    private PanelScrollingNBT(IGuiRect rect, int btnEdit, int btnAdv, int btnUp, int btnDown, int btnInsert, int btnDelete) {
        super(rect);

        this.btnEdit = btnEdit;
        this.btnAdv = btnAdv;
        this.btnUp = btnUp;
        this.btnDown = btnDown;
        this.btnInsert = btnInsert;
        this.btnDelete = btnDelete;

        // Listens to its own buttons to update NBT values. The parent screen defines what the IDs are and any furter actions
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);
    }

    // TODO: Implement this with a proper way of displaying and choosing between options (and setting priority for default)
    /*private final List<Pair<Predicate<NBTBase>, Consumer<NBTBase>>> filterMap = new ArrayList<>();
    
    public void registerHandler(Predicate<NBTBase> filter, Consumer<NBTBase> action)
    {
        filterMap.add(new Pair<>(filter, action));
    }
    
    @Nullable public List<Consumer<NBTBase>> getHandler(@Nonnull NBTBase tag)
    {
        List<Consumer<NBTBase>> list = new ArrayList<>();
    
        for(Pair<Predicate<NBTBase>, Consumer<NBTBase>> p : filterMap)
        {
            if(p.getKey().test(tag))
            {
                list.add(p.getValue());
            }
        }
    
        return list;
    }*/

    public PanelScrollingNBT setNBT(NBTTagCompound tag) {
        this.nbt = tag;
        refreshList();
        return this;
    }

    public PanelScrollingNBT setNBT(NBTTagList list) {
        this.nbt = list;
        refreshList();
        return this;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        this.refreshList();
    }

    @SuppressWarnings("unchecked")
    private void refreshList() {
        this.resetCanvas();

        if (this.nbt == null) {
            return;
        }

        int preSX = getScrollX();
        int preSY = getScrollY();
        int width = getTransform().getWidth();
        int lw = (int) (width / 3F);
        int rw = width - lw; // Width on right side (rounds up to account for rounding errors lost on left side)

        if (nbt.getId() == 10) // NBTTagCompound
        {
            NBTTagCompound tag = (NBTTagCompound) nbt;
            List<String> sortedKeys = new ArrayList<>(tag.getKeySet());
            Collections.sort(sortedKeys);
            Iterator<String> keys = sortedKeys.iterator();
            int i = 0;

            while (keys.hasNext()) {
                String k = keys.next();
                NBTBase entry = tag.getTag(k);

                PanelTextBox name = new PanelTextBox(new GuiRectangle(0, i * 16 + 4, lw - 8, 12, 0), k).setAlignment(2);
                name.setColor(PresetColor.TEXT_MAIN.getColor());
                this.addPanel(name);

                if (entry.getId() == 10) // Object
                {
                    PanelButtonStorage<String> btn = new PanelButtonStorage<>(new GuiRectangle(lw, i * 16, rw - 32, 16, 0),
                                                                              btnEdit,
                                                                              getButtonTitle((NBTTagCompound) entry),
                                                                              k);
                    this.addPanel(btn);

                    btn = new PanelButtonStorage<>(new GuiRectangle(width - 32, i * 16, 16, 16, 0), btnAdv, "...", k);
                    this.addPanel(btn);
                } else if (entry.getId() == 9) // List
                {
                    PanelButtonStorage<String> btn = new PanelButtonStorage<>(new GuiRectangle(lw, i * 16, rw - 16, 16, 0), btnEdit, "List...", k);
                    this.addPanel(btn);
                } else if (entry.getId() == 8) // Text
                {
                    PanelTextField<String> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 32, 16, 0),
                                                                       "" + ((NBTTagString) entry).getString(),
                                                                       FieldFilterString.INSTANCE);
                    text.setCallback(new CallbackNBTTagString(tag, k)).setMaxLength(Integer.MAX_VALUE);
                    this.addPanel(text);

                    PanelButtonStorage<String> btn = new PanelButtonStorage<>(new GuiRectangle(width - 32, i * 16, 16, 16, 0), btnEdit, "Aa", k);
                    this.addPanel(btn);
                } else if (entry.getId() == 1) // Byte/Boolean
                {
                    PanelTextField<Byte> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw / 2, 16, 0),
                                                                     "" + ((NBTPrimitive) entry).getByte(),
                                                                     FieldFilterNumber.BYTE);
                    text.setMaxLength(Integer.MAX_VALUE); // Put callback here
                    this.addPanel(text);

                    PanelButtonStorage<String> btn = new PanelButtonStorage<>(new GuiRectangle(lw + rw / 2, i * 16, (int) Math.ceil(rw / 2F) - 16, 16, 0),
                                                                              btnEdit,
                                                                              ((NBTPrimitive) entry).getByte() > 0 ? "true" : "false",
                                                                              k);
                    this.addPanel(btn);

                    text.setMaxLength(Integer.MAX_VALUE)
                            .setCallback(new CallbackMulti<>(new CallbackNBTPrimitive<>(tag, k, Byte.class),
                                                             value -> btn.setText(value > 0 ? "true" : "false")));
                    btn.setCallback(value -> {
                        boolean flag = tag.getByte(value) > 0;
                        tag.setByte(value, flag ? (byte) 0 : (byte) 1);
                        text.setText(flag ? "0" : "1");
                        btn.setText(flag ? "false" : "true");
                    });
                } else if (entry.getId() > 1 && entry.getId() < 7) // Number
                {
                    switch (entry.getId()) {
                        case 2: // Short
                        {
                            PanelTextField<Short> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 16, 16, 0),
                                                                              "" + ((NBTPrimitive) entry).getShort(),
                                                                              FieldFilterNumber.SHORT);
                            text.setCallback(new CallbackNBTPrimitive<>(tag, k, Short.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 3: // Integer
                        {
                            PanelTextField<Integer> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 16, 16, 0),
                                                                                "" + ((NBTPrimitive) entry).getInt(),
                                                                                FieldFilterNumber.INT);
                            text.setCallback(new CallbackNBTPrimitive<>(tag, k, Integer.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 4: // Long
                        {
                            PanelTextField<Long> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 16, 16, 0),
                                                                             "" + ((NBTPrimitive) entry).getLong(),
                                                                             FieldFilterNumber.LONG);
                            text.setCallback(new CallbackNBTPrimitive<>(tag, k, Long.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 5: // Float
                        {
                            PanelTextField<Float> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 16, 16, 0),
                                                                              "" + ((NBTPrimitive) entry).getFloat(),
                                                                              FieldFilterNumber.FLOAT);
                            text.setCallback(new CallbackNBTPrimitive<>(tag, k, Float.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 6: // Double
                        {
                            PanelTextField<Double> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 16, 16, 0),
                                                                               "" + ((NBTPrimitive) entry).getDouble(),
                                                                               FieldFilterNumber.DOUBLE);
                            text.setCallback(new CallbackNBTPrimitive<>(tag, k, Double.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                    }
                } else {
                    PanelTextBox err = new PanelTextBox(new GuiRectangle(lw, i * 16 + 4, rw - 16, 12, 0),
                                                        entry.getClass().getSimpleName() + " Not Supported Yet").setAlignment(1);
                    err.setColor(PresetColor.TEXT_MAIN.getColor());
                    this.addPanel(err);
                }

                PanelButtonStorage<String> btnD = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), btnDelete, "x", k);
                btnD.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(255, 0, 0, 255), new GuiColorStatic(255, 0, 0, 255));
                this.addPanel(btnD);

                i++;
            }

            this.addPanel(new PanelGeneric(new GuiRectangle(0, i * 16, width - 16, 16, 0), null)); // Keeps the list from auto resizing

            PanelButtonStorage<String> btnI = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), btnInsert, "+", "");
            btnI.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(0, 255, 0, 255), new GuiColorStatic(0, 255, 0, 255));
            this.addPanel(btnI);
        } else if (nbt.getId() == 9) // NBTTagList
        {
            NBTTagList list = (NBTTagList) nbt;

            int i = 0;

            for (; i < list.tagCount(); i++) {
                NBTBase entry = list.get(i);

                PanelTextBox name = new PanelTextBox(new GuiRectangle(0, i * 16 + 4, lw - 8, 16, 0), "#" + i).setAlignment(2);
                name.setColor(PresetColor.TEXT_MAIN.getColor());
                this.addPanel(name);

                if (entry.getId() == 10) // Object
                {
                    PanelButtonStorage<Integer> btn = new PanelButtonStorage<>(new GuiRectangle(lw, i * 16, rw - 80, 16, 0),
                                                                               btnEdit,
                                                                               getButtonTitle((NBTTagCompound) entry),
                                                                               i);
                    this.addPanel(btn);

                    btn = new PanelButtonStorage<>(new GuiRectangle(width - 80, i * 16, 16, 16, 0), btnAdv, "...", i);
                    this.addPanel(btn);
                } else if (entry.getId() == 9) // List
                {
                    PanelButtonStorage<Integer> btn = new PanelButtonStorage<>(new GuiRectangle(lw, i * 16, rw - 64, 16, 0), btnEdit, "List...", i);
                    this.addPanel(btn);
                } else if (entry.getId() == 8) // Text
                {
                    PanelTextField<String> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 80, 16, 0),
                                                                       "" + ((NBTTagString) entry).getString(),
                                                                       FieldFilterString.INSTANCE);
                    text.setCallback(new CallbackNBTTagString(list, i)).setMaxLength(Integer.MAX_VALUE);
                    this.addPanel(text);

                    PanelButtonStorage<Integer> btn = new PanelButtonStorage<>(new GuiRectangle(width - 80, i * 16, 16, 16, 0), btnEdit, "Aa", i);
                    this.addPanel(btn);
                } else if (entry.getId() == 1) // Byte/Boolean
                {
                    PanelTextField<Byte> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw / 2, 16, 0),
                                                                     "" + ((NBTPrimitive) entry).getByte(),
                                                                     FieldFilterNumber.BYTE);
                    this.addPanel(text);

                    PanelButtonStorage<Integer> btn = new PanelButtonStorage<>(new GuiRectangle(lw + rw / 2, i * 16, (int) Math.ceil(rw / 2F) - 64, 16, 0),
                                                                               btnEdit,
                                                                               ((NBTPrimitive) entry).getByte() > 0 ? "true" : "false",
                                                                               i);
                    this.addPanel(btn);

                    text.setMaxLength(Integer.MAX_VALUE)
                            .setCallback(new CallbackMulti<>(new CallbackNBTPrimitive<>(list, i, Byte.class),
                                                             value -> btn.setText(value > 0 ? "true" : "false")));
                    btn.setCallback(value -> {
                        boolean flag = ((NBTTagByte) list.get(value)).getByte() > 0;
                        list.set(value, new NBTTagByte(flag ? (byte) 0 : (byte) 1));
                        text.setText(flag ? "0" : "1");
                        btn.setText(flag ? "false" : "true");
                    });
                } else if (entry.getId() > 1 && entry.getId() < 7) // Number
                {
                    switch (entry.getId()) {
                        case 2: // Short
                        {
                            PanelTextField<Short> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 64, 16, 0),
                                                                              "" + ((NBTPrimitive) entry).getShort(),
                                                                              FieldFilterNumber.SHORT);
                            text.setCallback(new CallbackNBTPrimitive<>(list, i, Short.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 3: // Integer
                        {
                            PanelTextField<Integer> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 64, 16, 0),
                                                                                "" + ((NBTPrimitive) entry).getInt(),
                                                                                FieldFilterNumber.INT);
                            text.setCallback(new CallbackNBTPrimitive<>(list, i, Integer.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 4: // Long
                        {
                            PanelTextField<Long> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 64, 16, 0),
                                                                             "" + ((NBTPrimitive) entry).getLong(),
                                                                             FieldFilterNumber.LONG);
                            text.setCallback(new CallbackNBTPrimitive<>(list, i, Long.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 5: // Float
                        {
                            PanelTextField<Float> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 64, 16, 0),
                                                                              "" + ((NBTPrimitive) entry).getFloat(),
                                                                              FieldFilterNumber.FLOAT);
                            text.setCallback(new CallbackNBTPrimitive<>(list, i, Float.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                        case 6: // Double
                        {
                            PanelTextField<Double> text = new PanelTextField<>(new GuiRectangle(lw, i * 16, rw - 64, 16, 0),
                                                                               "" + ((NBTPrimitive) entry).getDouble(),
                                                                               FieldFilterNumber.DOUBLE);
                            text.setCallback(new CallbackNBTPrimitive<>(list, i, Double.class)).setMaxLength(Integer.MAX_VALUE);
                            this.addPanel(text);
                            break;
                        }
                    }
                } else {
                    PanelTextBox err = new PanelTextBox(new GuiRectangle(lw, i * 16 + 4, rw - 48, 12, 0),
                                                        entry.getClass().getSimpleName() + " Not Supported Yet").setAlignment(1);
                    err.setColor(PresetColor.TEXT_MAIN.getColor());
                    this.addPanel(err);
                }

                PanelButton btnUp = new PanelButtonStorage<>(new GuiRectangle(width - 64, i * 16, 16, 16, 0), this.btnUp, "", i).setIcon(PresetIcon.ICON_UP
                        .getTexture());
                btnUp.setActive(i > 0);
                this.addPanel(btnUp);

                PanelButton btnDown = new PanelButtonStorage<>(new GuiRectangle(width - 48, i * 16, 16, 16, 0), this.btnDown, "", i).setIcon(
                                                                                                                                             PresetIcon.ICON_DOWN
                                                                                                                                                     .getTexture());
                btnDown.setActive(i < list.tagCount() - 1);
                this.addPanel(btnDown);

                PanelButtonStorage<Integer> btnInsert = new PanelButtonStorage<>(new GuiRectangle(width - 32, i * 16, 16, 16, 0), this.btnInsert, "+", i);
                btnInsert.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(0, 255, 0, 255), new GuiColorStatic(0, 255, 0, 255));
                this.addPanel(btnInsert);

                PanelButtonStorage<Integer> btnDelete = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), this.btnDelete, "x", i);
                btnDelete.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(255, 0, 0, 255), new GuiColorStatic(255, 0, 0, 255));
                this.addPanel(btnDelete);
            }

            this.addPanel(new PanelGeneric(new GuiRectangle(0, i * 16, width - 32, 16, 0), null)); // Keeps the list from auto resizing

            PanelButtonStorage<Integer> btnI = new PanelButtonStorage<>(new GuiRectangle(width - 32, i * 16, 16, 16, 0), btnInsert, "+", i);
            btnI.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(0, 255, 0, 255), new GuiColorStatic(0, 255, 0, 255));
            this.addPanel(btnI);
        }

        this.setScrollX(preSX);
        this.setScrollY(preSY);
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event) {
        if (nbt == null) {
            return;
        }

        IPanelButton btn = event.getButton();
        NBTBase entry;

        if (!(btn.getButtonID() == btnEdit || btn.getButtonID() == btnAdv || btn.getButtonID() == btnUp || btn.getButtonID() == btnDown || btn.getButtonID() ==
                btnInsert || btn.getButtonID() == btnDelete)) {
            return;
        }

        if (nbt.getId() == 10) {
            entry = ((NBTTagCompound) nbt).getTag(((PanelButtonStorage<String>) btn).getStoredValue());
        } else if (nbt.getId() == 9) {
            entry = ((NBTTagList) nbt).get(((PanelButtonStorage<Integer>) btn).getStoredValue());
        } else {
            throw new RuntimeException("Invalid NBT tag type!");
        }

        if (btn.getButtonID() == btnEdit) // Context dependent action/toggle
        {
            if (nbt.getId() == 10) {
                mc.displayGuiScreen(GuiNbtEditor.createEditorGui(mc.currentScreen, (NBTTagCompound) nbt, ((PanelButtonStorage<String>) btn).getStoredValue()));
            } else if (nbt.getId() == 9) {
                mc.displayGuiScreen(GuiNbtEditor.createEditorGui(mc.currentScreen, (NBTTagList) nbt, ((PanelButtonStorage<Integer>) btn).getStoredValue()));
            } else {
                throw new RuntimeException("Invalid NBT tag type!");
            }
        } else if (btn.getButtonID() == btnAdv) // Open advanced editor (on supported types)
        {
            if (entry.getId() == 10) {
                mc.displayGuiScreen(new GuiNbtType(mc.currentScreen, (NBTTagCompound) entry));
            } else if (entry.getId() == 9) // Not currently available but will be when context list editors (enchantments/inventories/etc) are available
            {
                // TODO: Replace with context based list editors
                mc.displayGuiScreen(new GuiNbtEditor(mc.currentScreen, (NBTTagList) entry, null));
            }
        } else if (btn.getButtonID() == btnUp) {
            if (nbt.getId() == 9) {
                NBTTagList tagList = (NBTTagList) nbt;
                int index = ((PanelButtonStorage<Integer>) btn).getStoredValue();
                if (index > 0) {
                    NBTBase tmp = tagList.get(index);
                    tagList.set(index, tagList.get(index - 1));
                    tagList.set(index - 1, tmp);
                    refreshList();
                }
            }
        } else if (btn.getButtonID() == btnDown) {
            if (nbt.getId() == 9) {
                NBTTagList tagList = (NBTTagList) nbt;
                int index = ((PanelButtonStorage<Integer>) btn).getStoredValue();
                if (index < tagList.tagCount() - 1) {
                    NBTBase tmp = tagList.get(index);
                    tagList.set(index, tagList.get(index + 1));
                    tagList.set(index + 1, tmp);
                    refreshList();
                }
            }
        } else if (btn.getButtonID() == btnInsert) {
            if (nbt.getId() == 10) {
                mc.displayGuiScreen(new GuiNbtAdd(mc.currentScreen, (NBTTagCompound) nbt));
            } else if (nbt.getId() == 9) {
                NBTTagList tagList = (NBTTagList) nbt;
                int index = ((PanelButtonStorage<Integer>) btn).getStoredValue();
                if (tagList.tagCount() > 0) {
                    NBTUtil.insertTag(tagList, index, createDefaultNbtTag(tagList.get(0)));
                    mc.displayGuiScreen(GuiNbtEditor.createEditorGui(mc.currentScreen, tagList, index));
                } else {
                    mc.displayGuiScreen(new GuiNbtListAdd(mc.currentScreen, tagList, index));
                }
            }
        } else if (btn.getButtonID() == btnDelete) {
            if (nbt.getId() == 10) {
                ((NBTTagCompound) nbt).removeTag(((PanelButtonStorage<String>) btn).getStoredValue());
                refreshList();
            } else if (nbt.getId() == 9) {
                ((NBTTagList) nbt).removeTag(((PanelButtonStorage<Integer>) btn).getStoredValue());
                refreshList();
            }
        }
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    private String getButtonTitle(NBTTagCompound tag) {
        if (JsonHelper.isItem(tag)) {
            BigItemStack stack = JsonHelper.JsonToItemStack(tag);
            return QuestTranslation.translate("betterquesting.btn.item") + ": " + stack.getBaseStack().getDisplayName();
        } else if (JsonHelper.isFluid(tag)) {
            FluidStack fluid = JsonHelper.JsonToFluidStack(tag);
            return QuestTranslation.translate("betterquesting.btn.fluid") + ": " + fluid.getLocalizedName();
        } else if (JsonHelper.isEntity(tag)) {
            Entity entity = JsonHelper.JsonToEntity(tag, this.mc.world);
            return QuestTranslation.translate("betterquesting.btn.entity") + ": " + entity.getName();
        }

        return "Object...";
    }

    private static NBTBase createDefaultNbtTag(NBTBase base) {
        if (base.getId() == 10) {
            NBTTagCompound tag = (NBTTagCompound) base;

            if (JsonHelper.isItem(tag)) {
                return JsonHelper.ItemStackToJson(new BigItemStack(Blocks.STONE), new NBTTagCompound());
            } else if (JsonHelper.isFluid(tag)) {
                return JsonHelper.FluidStackToJson(new FluidStack(FluidRegistry.WATER, 1000), new NBTTagCompound());
            } else if (JsonHelper.isEntity(tag)) {
                return JsonHelper.EntityToJson(new EntityPig(Minecraft.getMinecraft().world), new NBTTagCompound());
            } else {
                return new NBTTagCompound();
            }
        } else {
            try {
                return base.getClass().getConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
