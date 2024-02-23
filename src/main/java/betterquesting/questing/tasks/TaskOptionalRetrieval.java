package betterquesting.questing.tasks;

import java.util.UUID;

import net.minecraft.util.ResourceLocation;

import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.factory.FactoryTaskOptionalRetrieval;

public class TaskOptionalRetrieval extends TaskRetrieval {

    @Override
    public String getUnlocalisedName() {
        return BetterQuesting.MODID_STD + ".task.optional_retrieval";
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskOptionalRetrieval.INSTANCE.getRegistryName();
    }

    @Override
    public boolean ignored(UUID uuid) {
        return true;
    }
}
