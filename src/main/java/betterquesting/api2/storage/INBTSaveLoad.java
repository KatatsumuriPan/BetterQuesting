package betterquesting.api2.storage;

import net.minecraft.nbt.NBTBase;

// TODO: Replace usage with INBTSerializable?
public interface INBTSaveLoad<T extends NBTBase> {
    @Deprecated
    T writeToNBT(T nbt);

    default T writeToNBT(T nbt, boolean reduce){
        return writeToNBT(nbt);
    }

    void readFromNBT(T nbt);

}
