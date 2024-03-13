package betterquesting.client.toolbox.tools;

import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumFrameType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.client.gui2.editors.nbt.GuiQuestFrameSelection;
import betterquesting.network.handlers.NetQuestEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class ToolboxToolFrame implements IToolboxTool {

    private CanvasQuestLine gui;

    @Override
    public void initTool(CanvasQuestLine gui) {
        this.gui = gui;
    }

    @Override
    public void refresh(CanvasQuestLine gui) {

    }

    @Override
    public void disableTool() {

    }

    @Override
    public void drawCanvas(int mx, int my, float partialTick) {

    }

    @Override
    public void drawOverlay(int mx, int my, float partialTick) {

    }

    @Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons) {
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (click != 0 || !gui.getTransform().contains(mx, my))
            return false;

        PanelButtonQuest btn = gui.getButtonAt(mx, my);

        if (btn == null)
            return false;
        if (PanelToolController.selected.size() > 0 && !PanelToolController.selected.contains(btn))
            return false;

        List<PanelButtonQuest> btnList = PanelToolController.selected.size() > 0 ? PanelToolController.selected : Collections.singletonList(btn);
        changeFrame(btnList);
        return true;
    }

    private void changeFrame(List<PanelButtonQuest> btnList) {
        Minecraft mc = Minecraft.getMinecraft();
        IQuest quest = btnList.get(0).getStoredValue().getValue();
        EnumFrameType current = quest.getProperty(NativeProps.FRAME);
        mc.displayGuiScreen(new GuiQuestFrameSelection(mc.currentScreen, current, quest.getProperty(NativeProps.ICON), value -> {
            NBTTagList dataList = new NBTTagList();
            for (PanelButtonQuest btn : btnList) {
                btn.getStoredValue().getValue().setProperty(NativeProps.FRAME, value);

            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("questID", btn.getStoredValue().getID());
            entry.setTag("config", btn.getStoredValue().getValue().writeToNBT(new NBTTagCompound(), true));
            dataList.appendTag(entry);
        }

            NBTTagCompound payload = new NBTTagCompound();
            payload.setTag("data", dataList);
            payload.setInteger("action", 0);
            NetQuestEdit.sendEdit(payload);
        }, quest.getProperty(NativeProps.MAIN)));

    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        return false;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        return false;
    }

    @Override
    public boolean onKeyPressed(char c, int key) {
        if (PanelToolController.selected.size() <= 0 || key != Keyboard.KEY_RETURN)
            return false;

        changeFrame(PanelToolController.selected);
        return true;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        return null;
    }

    @Override
    public boolean clampScrolling() {
        return true;
    }

    @Override
    public boolean useSelection() {
        return true;
    }

}
