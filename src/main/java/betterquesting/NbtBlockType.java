package betterquesting;

import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api.utils.BigItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class NbtBlockType // TODO: Make a version of this for the base mod and give it a dedicated editor
{

    public Block b = Blocks.LOG;
    public int m = -1;
    public int n = 1;
    public String oreDict = "";
    public NBTTagCompound tags = new NBTTagCompound();

    public NbtBlockType() {
    }

    public NbtBlockType(Block block) {
        this.b = block;
        this.oreDict = "";
        this.tags = new NBTTagCompound();
    }

    public NbtBlockType(IBlockState state) {
        this.b = state.getBlock();
        this.m = b.getMetaFromState(state);
        this.oreDict = "";
        this.tags = new NBTTagCompound();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt, boolean reduce) {
        nbt.setString("blockID", b.getRegistryName().toString());
        if (!reduce || m != -1)
            nbt.setInteger("meta", m);
        if (!reduce || !tags.isEmpty())
            nbt.setTag("nbt", tags);
        if (!reduce || n != 1)
            nbt.setInteger("amount", n);
        if (!reduce || !oreDict.isEmpty())
            nbt.setString("oreDict", oreDict);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        b = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("blockID")));
        m = NBTUtil.getInteger(nbt, "meta", -1);
        tags = nbt.getCompoundTag("nbt");
        n = NBTUtil.getInteger(nbt, "amount", 1);
        oreDict = NBTUtil.getString(nbt, "oreDict", "");
    }

    public BigItemStack getItemStack() {
        BigItemStack stack;

        if (b == null) {
            stack = new BigItemStack(ItemPlaceholder.placeholder, n, 0);
            stack.getBaseStack().setStackDisplayName("NULL");
        } else {
            stack = new BigItemStack(b, n, m);
        }

        stack.setOreDict(oreDict);
        return stack;
    }

}
