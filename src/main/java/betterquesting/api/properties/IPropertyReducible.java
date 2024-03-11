package betterquesting.api.properties;

import net.minecraft.nbt.NBTBase;

public interface IPropertyReducible<T> extends IPropertyType<T> {

    NBTBase reduceNBT(NBTBase nbt);

}
