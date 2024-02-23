package betterquesting.api.questing.party;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagList;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;

public interface IPartyDatabase extends IDatabase<IParty>, INBTPartial<NBTTagList, Integer> {

    IParty createNew(int id);

    @Nullable
    DBEntry<IParty> getParty(@Nonnull UUID uuid);
}
