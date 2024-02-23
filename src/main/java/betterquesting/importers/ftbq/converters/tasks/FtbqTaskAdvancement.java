package betterquesting.importers.ftbq.converters.tasks;

import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import betterquesting.questing.tasks.TaskTrigger;

public class FtbqTaskAdvancement {

    public ITask[] converTask(NBTTagCompound tag) {
        TaskTrigger task = new TaskTrigger();

        task.setTriggerID(tag.getString("advancement"));
        task.setCriteriaJson(tag.getString("criterion"));

        FTBQQuestImporter.provideQuestIcon(new BigItemStack(Items.WHEAT));

        return new ITask[] { task };
    }
}
