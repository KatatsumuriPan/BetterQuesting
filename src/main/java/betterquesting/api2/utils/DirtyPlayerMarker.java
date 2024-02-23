package betterquesting.api2.utils;

import java.util.Collection;
import java.util.UUID;

import net.minecraftforge.common.MinecraftForge;

import betterquesting.api.events.MarkDirtyPlayerEvent;

public class DirtyPlayerMarker {

    public static void markDirty(Collection<UUID> players) {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyPlayerEvent(players));
    }

    public static void markDirty(UUID player) {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyPlayerEvent(player));
    }
}
