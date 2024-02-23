package betterquesting.api.questing.tasks;

import java.util.UUID;

import net.minecraftforge.fluids.FluidStack;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;

public interface IFluidTask extends ITask {

    boolean canAcceptFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid);

    FluidStack submitFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid);
}
