package betterquesting.api.storage;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;

public interface INameCache extends INBTPartial<NBTTagList, UUID> {

    boolean updateName(@Nonnull
    EntityPlayerMP player);

    String getName(@Nonnull
    UUID uuid);

    UUID getUUID(@Nonnull
    String name);

    List<String> getAllNames();

    // Primarily used client side for GUIs
    boolean isOP(@Nonnull
    UUID uuid);

    int size();

    void reset();

}
