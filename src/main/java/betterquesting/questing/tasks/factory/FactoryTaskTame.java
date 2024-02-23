package betterquesting.questing.tasks.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskTame;

public final class FactoryTaskTame implements IFactoryData<ITask, NBTTagCompound> {

    public static final FactoryTaskTame INSTANCE = new FactoryTaskTame();

    private final ResourceLocation REG_ID = new ResourceLocation(BetterQuesting.MODID_STD, "tame");

    @Override
    public ResourceLocation getRegistryName() {
        return REG_ID;
    }

    @Override
    public TaskTame createNew() {
        return new TaskTame();
    }

    @Override
    public TaskTame loadFromData(NBTTagCompound nbt) {
        TaskTame task = new TaskTame();
        task.readFromNBT(nbt);
        return task;
    }
}
