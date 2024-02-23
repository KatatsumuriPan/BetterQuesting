package betterquesting.api.placeholders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import betterquesting.core.ModReference;

public class FluidPlaceholder extends Fluid {

    public static Fluid fluidPlaceholder = new FluidPlaceholder();

    public FluidPlaceholder() {
        super(ModReference.MODID + ".placeholder", new ResourceLocation(ModReference.MODID, "blocks/fluid_placeholder"),
                new ResourceLocation(ModReference.MODID, "blocks/fluid_placeholder"));
    }
}
