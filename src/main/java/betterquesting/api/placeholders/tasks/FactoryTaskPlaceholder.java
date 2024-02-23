package betterquesting.api.placeholders.tasks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.ModReference;

public class FactoryTaskPlaceholder implements IFactoryData<TaskPlaceholder, NBTTagCompound> {

    public static final FactoryTaskPlaceholder INSTANCE = new FactoryTaskPlaceholder();

    private final ResourceLocation ID = new ResourceLocation(ModReference.MODID, "placeholder");

    private FactoryTaskPlaceholder() {}

    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    public TaskPlaceholder createNew() {
        return new TaskPlaceholder();
    }

    @Override
    public TaskPlaceholder loadFromData(NBTTagCompound nbt) {
        TaskPlaceholder task = createNew();
        task.readFromNBT(nbt);
        return task;
    }
}
