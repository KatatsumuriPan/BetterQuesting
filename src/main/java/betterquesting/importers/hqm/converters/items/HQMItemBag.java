package betterquesting.importers.hqm.converters.items;

import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api.utils.BigItemStack;
import betterquesting.core.BetterQuesting;

public class HQMItemBag implements HQMItem {

    @Override
    public BigItemStack convertItem(int damage, int amount, NBTTagCompound tags) {
        return new BigItemStack(BetterQuesting.lootChest, amount, damage * 25);
    }
}
