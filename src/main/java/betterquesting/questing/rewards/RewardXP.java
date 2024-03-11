package betterquesting.questing.rewards;

import betterquesting.NBTUtil;
import betterquesting.XPHelper;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.gui2.rewards.PanelRewardXP;
import betterquesting.questing.rewards.factory.FactoryRewardXP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RewardXP implements IReward {
    private static final int DEFAULT_AMOUNT = 1;
    private static final boolean DEFAULT_LEVELS = true;
    public int amount = DEFAULT_AMOUNT;
    public boolean levels = DEFAULT_LEVELS;

    @Override
    public ResourceLocation getFactoryID() { return FactoryRewardXP.INSTANCE.getRegistryName(); }

    @Override
    public String getUnlocalisedName() { return "bq_standard.reward.xp"; }

    @Override
    public boolean canClaim(EntityPlayer player, DBEntry<IQuest> quest) {
        return true;
    }

    @Override
    public void claimReward(EntityPlayer player, DBEntry<IQuest> quest) {
        XPHelper.addXP(player, !levels ? amount : XPHelper.getLevelXP(amount));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        amount = NBTUtil.getInteger(nbt, "amount", DEFAULT_AMOUNT);
        levels = NBTUtil.getBoolean(nbt, "isLevels", DEFAULT_LEVELS);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt, boolean reduce) {
        if (!reduce || amount != DEFAULT_AMOUNT)
            nbt.setInteger("amount", amount);
        if (!reduce || levels != DEFAULT_LEVELS)
            nbt.setBoolean("isLevels", levels);
        return nbt;
    }

    @Override @SideOnly(Side.CLIENT)
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelRewardXP(rect, this);
    }

    @Override @SideOnly(Side.CLIENT)
    public GuiScreen getRewardEditor(GuiScreen screen, DBEntry<IQuest> quest) {
        return null;
    }

}
