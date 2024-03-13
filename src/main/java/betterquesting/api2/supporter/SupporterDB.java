package betterquesting.api2.supporter;

import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.core.BetterQuesting;
import net.minecraft.nbt.NBTTagCompound;

public class SupporterDB implements INBTSaveLoad<NBTTagCompound> {

    public static final SupporterDB INSTANCE = new SupporterDB();

    private final TreeMap<UUID, SupporterEntry> mapDB = new TreeMap<>();

    public synchronized SupporterEntry add(@Nonnull UUID playerID) {
        SupporterEntry entry = new SupporterEntry();
        if (mapDB.putIfAbsent(playerID, entry) != null) {
            BetterQuesting.logger.warn("Tried to add duplicate supporter to DB: " + playerID.toString());
            return mapDB.get(playerID);
        }
        return entry;
    }

    public synchronized boolean removeID(@Nonnull UUID playerID) {
        return mapDB.remove(playerID) != null;
    }

    @Nullable
    public synchronized SupporterEntry getValue(@Nonnull UUID playerID) {
        return mapDB.get(playerID);
    }

    /*@Nullable
    public UUID getKey(@Nonnull SupporterEntry entry)
    {
        for(Entry<UUID, SupporterEntry> pair : mapDB.entrySet())
        {
            if(pair.getValue() == entry) return pair.getKey();
        }
    
        return null;
    }*/

    @Deprecated @Nonnull @Override
    public synchronized NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        return writeToNBT(nbt, false);
    }

    @Nonnull @Override
    public synchronized NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt, boolean reduce) {
        mapDB.forEach((key, value) -> nbt.setTag(key.toString(), value.writeToNBT(new NBTTagCompound(), reduce)));
        return nbt;
    }

    @Override
    public synchronized void readFromNBT(@Nonnull NBTTagCompound nbt) {
        mapDB.clear();
        nbt.getKeySet().forEach((key) -> {
            try {
                SupporterEntry entry = new SupporterEntry();
                entry.readFromNBT(nbt.getCompoundTag(key));
                mapDB.put(UUID.fromString(key), entry);
            } catch (Exception ignored) {
            }
        });
    }

}
