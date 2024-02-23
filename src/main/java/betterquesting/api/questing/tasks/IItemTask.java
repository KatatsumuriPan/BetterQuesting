package betterquesting.api.questing.tasks;

import java.util.UUID;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.item.ItemStack;

public interface IItemTask extends ITask {

    boolean canAcceptItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);

    ItemStack submitItem(UUID owner, DBEntry<IQuest> quest, ItemStack stack);

}
