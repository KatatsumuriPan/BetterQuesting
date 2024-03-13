package betterquesting.api2.supporter;

import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class SupporterEntry implements INBTSaveLoad<NBTTagCompound> {
    private final HashMap<String, HashMap<String, Integer>> services = new HashMap<>();

    @Nonnull
    public HashMap<String, Integer> getServices(@Nonnull String token) {
        return this.services.computeIfAbsent(token, (t) -> new HashMap<>());
    }

    @Deprecated
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return writeToNBT(nbt, false);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt, boolean reduce) {
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }
}