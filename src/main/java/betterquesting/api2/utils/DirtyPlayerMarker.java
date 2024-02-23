package betterquesting.api2.utils;

import java.util.Collection;
import java.util.UUID;

import betterquesting.api.events.MarkDirtyPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

public class DirtyPlayerMarker {

    public static void markDirty(Collection<UUID> players) {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyPlayerEvent(players));
    }

    public static void markDirty(UUID player) {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyPlayerEvent(player));
    }

}
