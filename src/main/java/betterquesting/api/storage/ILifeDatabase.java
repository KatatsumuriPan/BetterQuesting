package betterquesting.api.storage;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api2.storage.INBTPartial;

public interface ILifeDatabase extends INBTPartial<NBTTagCompound, UUID> {

    int getLives(@Nonnull UUID uuid);

    void setLives(@Nonnull UUID uuid, int value);

    void reset();
}
