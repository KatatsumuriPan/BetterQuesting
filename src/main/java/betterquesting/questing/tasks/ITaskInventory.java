package betterquesting.questing.tasks;

import javax.annotation.Nonnull;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;

public interface ITaskInventory extends ITask {

    void onInventoryChange(@Nonnull DBEntry<IQuest> quest, @Nonnull ParticipantInfo pInfo);

}
