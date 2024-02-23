package betterquesting.importers.ftbq.converters.rewards;

import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.questing.rewards.RewardXP;

public class FtbqRewardXP {

    private final boolean isLevels;

    public FtbqRewardXP(boolean isLevels) {
        this.isLevels = isLevels;
    }

    public IReward[] convertTask(NBTTagCompound tag) {
        RewardXP reward = new RewardXP();
        reward.levels = this.isLevels;
        reward.amount = isLevels ? tag.getInteger("xp_levels") : tag.getInteger("xp");
        return new IReward[] { reward };
    }
}
