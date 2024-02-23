package betterquesting.api.questing.party;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api2.storage.INBTSaveLoad;

public interface IParty extends INBTSaveLoad<NBTTagCompound> {

    IPropertyContainer getProperties();

    void kickUser(@Nonnull UUID uuid);

    void setStatus(@Nonnull UUID uuid, @Nonnull EnumPartyStatus priv);

    @Nullable
    EnumPartyStatus getStatus(@Nonnull UUID uuid);

    List<UUID> getMembers();

    NBTTagCompound writeProperties(NBTTagCompound nbt);

    void readProperties(NBTTagCompound nbt);
}
