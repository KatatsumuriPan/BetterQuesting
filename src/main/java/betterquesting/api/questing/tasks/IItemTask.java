package betterquesting.api.questing.tasks;

import java.util.UUID;

import net.minecraft.item.ItemStack;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;

public interface IItemTask extends ITask {

    boolean canAcceptItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);

    ItemStack submitItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);
}
