package betterquesting.api.properties.basic;

import betterquesting.api.properties.IPropertyReducible;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeItemStack extends PropertyTypeBase<BigItemStack> implements IPropertyReducible<BigItemStack> {

    public PropertyTypeItemStack(ResourceLocation key, BigItemStack def) {
        super(key, def);
    }

    @Override
    public BigItemStack readValue(NBTBase nbt) {
        if (nbt == null || nbt.getId() != 10) {
            return this.getDefault();
        }

        return JsonHelper.JsonToItemStack((NBTTagCompound) nbt);
    }

    @Override
    public NBTBase writeValue(BigItemStack value) {
        NBTTagCompound nbt = new NBTTagCompound();

        if (value == null || value.getBaseStack() == null) {
            getDefault().writeToNBT(nbt, false);
        } else {
            value.writeToNBT(nbt, false);
        }

        return nbt;
    }

    @Override
    public NBTBase reduceNBT(NBTBase nbt) {
        BigItemStack value;
        if (nbt == null || nbt.getId() != 10) {
            value = this.getDefault();
        } else {
            value = JsonHelper.JsonToItemStack((NBTTagCompound) nbt);
        }
        return value.writeToNBT(new NBTTagCompound(), true);
    }

}
