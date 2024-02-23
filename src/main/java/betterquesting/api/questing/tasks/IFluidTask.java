package betterquesting.api.questing.tasks;

import java.util.UUID;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidTask extends ITask {

    boolean canAcceptFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid);

    FluidStack submitFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid);

}
