package betterquesting.api.questing;

import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;

public interface IQuestLine extends IDatabase<IQuestLineEntry>, INBTPartial<NBTTagCompound, Integer>,
                            IPropertyContainer {

    IQuestLineEntry createNew(int id);

    String getUnlocalisedName();

    String getUnlocalisedDescription();

    DBEntry<IQuestLineEntry> getEntryAt(int x, int y);
}
