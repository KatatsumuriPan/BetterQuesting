package betterquesting.importers.hqm.converters.items;

import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api.utils.BigItemStack;

public interface HQMItem {

    BigItemStack convertItem(int damage, int amount, NBTTagCompound tags);
}
