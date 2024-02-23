package betterquesting.api.questing;

import net.minecraft.nbt.NBTTagList;

import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import betterquesting.api2.storage.INBTProgress;

public interface IQuestDatabase extends IDatabase<IQuest>, INBTPartial<NBTTagList, Integer>, INBTProgress<NBTTagList> {

    IQuest createNew(int id);
}
