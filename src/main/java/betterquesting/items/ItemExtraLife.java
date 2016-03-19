package betterquesting.items;

import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.core.BetterQuesting;
import betterquesting.lives.LifeManager;
import betterquesting.quests.QuestDatabase;

public class ItemExtraLife extends Item
{
	public ItemExtraLife()
	{
		//this.setTextureName("betterquesting:heart");
		this.setUnlocalizedName("betterquesting.extra_life");
		this.setCreativeTab(BetterQuesting.tabQuesting);
		this.setHasSubtypes(true);
	}

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        return true;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
    	if(world.isRemote)
    	{
    		return stack;
    	}
    	
    	if(QuestDatabase.bqHardcore)
    	{
    		if(player.capabilities.isCreativeMode)
    		{
    			stack.stackSize--;
    		}
    		
    		LifeManager.AddRemoveLives(player, 1);
    		world.playSoundAtEntity(player, "random.levelup", 1F, 1F);
    		player.addChatComponentMessage(new ChatComponentText(I18n.format("betterquesting.gui.remaining_lives", EnumChatFormatting.YELLOW + "" + LifeManager.getLives(player))));
    	} else
    	{
    		player.addChatComponentMessage(new ChatComponentText(I18n.format("betterquesting.msg.heart_disabled")));
    	}
    	
        return stack;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        switch(stack.getItemDamage()%3)
        {
        	case 2:
        		return this.getUnlocalizedName() + ".quarter";
        	case 1:
        		return this.getUnlocalizedName() + ".half";
        	default:
        		return this.getUnlocalizedName() + ".full";	
        }
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
    	list.add(new ItemStack(item, 1, 0));
    	list.add(new ItemStack(item, 1, 1));
    	list.add(new ItemStack(item, 1, 2));
    }

    /**
     * Gets an icon index based on an item's damage value
     */
    /*@SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int dmg)
    {
    	switch(dmg%3)
    	{
    		case 2:
    			return iconQuarter;
    		case 1:
    			return iconHalf;
    		default:
    			return itemIcon;
    	}
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {
    	iconQuarter = register.registerIcon(this.getIconString() + "_quarter");
    	iconHalf = register.registerIcon(this.getIconString() + "_half");
    	itemIcon = register.registerIcon(this.getIconString() + "_full");
    }*/
}
