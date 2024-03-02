package betterquesting.client.gui2.editors.tasks;

import org.lwjgl.input.Keyboard;

import betterquesting.EnumUtil;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.buttons.PanelButtonBoolean;
import betterquesting.api2.client.gui.controls.buttons.PanelButtonEnum;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import betterquesting.core.ModReference;
import betterquesting.questing.tasks.TaskRetrieval;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class GuiEditTaskRetrieval extends GuiScreenCanvas implements IVolatileScreen {

    private final DBEntry<IQuest> quest;
    private final TaskRetrieval task;
    private NBTTagCompound current;

    public GuiEditTaskRetrieval(GuiScreen parent, DBEntry<IQuest> quest, TaskRetrieval task) {
        super(parent);
        this.quest = quest;
        this.task = task;
        current = task.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void initPanel() {
        super.initPanel();
        Keyboard.enableRepeatEvents(true);

        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0),
                                               QuestTranslation.translate("bq_standard.title.edit_retrieval_task")).setAlignment(1)
                .setColor(PresetColor.TEXT_HEADER.getColor()));

        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 24, 48), 0));
        cvBackground.addPanel(cvList);
        initItems(cvList);

        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-24, 32, 16, 48), 0));
        cvBackground.addPanel(scList);
        cvList.setScrollDriverY(scList);

        PanelButton btnEditNBT = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -36, 200, 16, 0),
                                                 -1,
                                                 QuestTranslation.translate("bq_standard.btn.edit_nbt"));
        btnEditNBT.setClickAction(btn -> {
            mc.displayGuiScreen(new GuiNbtEditor(GuiEditTaskRetrieval.this, current, value -> current = value));
        });
        cvBackground.addPanel(btnEditNBT);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.done")) {

            @Override
            public void onButtonClick() {
                task.readFromNBT(current);
                sendChanges();
                mc.displayGuiScreen(parent);
            }

        });
    }

    private void initItems(CanvasScrolling cvList) {
        int width = cvList.getTransform().getWidth();
        int lw = (int) (width / 3F);
        int rw = width - lw; // Width on right side (rounds up to account for rounding errors lost on left side)
        int idx = 0;
        addBoolean(idx++, "autoConsume", cvList);
        addBoolean(idx++, "consume", cvList);
        {
            PanelTextBox namePanel = new PanelTextBox(new GuiRectangle(0, idx * 16 + 4, lw - 8, 12, 0), "entryLogic").setAlignment(2);
            namePanel.setColor(PresetColor.TEXT_MAIN.getColor());
            cvList.addPanel(namePanel);
            cvList.addPanel(new PanelButtonEnum<>(new GuiRectangle(lw, idx * 16, rw - 32, 16),
                                                  -1,
                                                  EnumUtil.getEnum(current.getString("entryLogic"), EnumLogic.AND)).setCallback(value -> {
                                                      current.setString("entryLogic", value.name());
                                                  }));
            idx++;
        }
        addBoolean(idx++, "groupDetect", cvList);
        addBoolean(idx++, "ignoreNBT", cvList);
        addBoolean(idx++, "partialMatch", cvList);
        {
            PanelTextBox namePanel = new PanelTextBox(new GuiRectangle(0, idx * 16 + 4, lw - 8, 12, 0), "requiredItems").setAlignment(2);
            namePanel.setColor(PresetColor.TEXT_MAIN.getColor());
            cvList.addPanel(namePanel);
            PanelButton btn = new PanelButton(new GuiRectangle(lw, idx * 16, rw - 32, 16, 0), -1, "List...");
            btn.setClickAction(b -> {
                mc.displayGuiScreen(new GuiNbtEditor(mc.currentScreen, (NBTTagList) current.getTag("requiredItems"), null));
            });
            cvList.addPanel(btn);
            idx++;
        }

    }

    private void addBoolean(int idx, String name, CanvasScrolling cvList) {
        int width = cvList.getTransform().getWidth();
        int lw = (int) (width / 3F);
        int rw = width - lw; // Width on right side (rounds up to account for rounding errors lost on left side)
        PanelTextBox namePanel = new PanelTextBox(new GuiRectangle(0, idx * 16 + 4, lw - 8, 12, 0), name).setAlignment(2);
        namePanel.setColor(PresetColor.TEXT_MAIN.getColor());
        cvList.addPanel(namePanel);
        cvList.addPanel(new PanelButtonBoolean(new GuiRectangle(lw, idx * 16, rw - 32, 16), -1, current.getBoolean(name)).setCallback(value -> {
            current.setBoolean(name, value);
        }));

    }

    private static final ResourceLocation QUEST_EDIT = new ResourceLocation(ModReference.MODID, "quest_edit"); // TODO: Really need to make the native packet types accessible in the API

    private void sendChanges() {
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagList dataList = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("questID", quest.getID());
        entry.setTag("config", quest.getValue().writeToNBT(new NBTTagCompound()));
        dataList.appendTag(entry);
        payload.setTag("data", dataList);
        payload.setInteger("action", 0); // Action: Update data
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(QUEST_EDIT, payload));
    }

}
