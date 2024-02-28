package betterquesting.handlers;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nonnull;

import betterquesting.api.api.QuestingAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class PlayerContainerListener implements IContainerListener {

    private static final HashMap<UUID, PlayerContainerListener> LISTEN_MAP = new HashMap<>();

    public static void refreshListener(@Nonnull EntityPlayer player) {
        UUID uuid = QuestingAPI.getQuestingUUID(player);
        PlayerContainerListener listener = LISTEN_MAP.get(uuid);
        if (listener != null) {
            listener.player = player;
        } else {
            listener = new PlayerContainerListener(player);
            LISTEN_MAP.put(uuid, listener);
        }

        try {
            player.inventoryContainer.addListener(listener);
        } catch (Exception ignored) {
        }
    }

    private EntityPlayer player;

    private PlayerContainerListener(@Nonnull EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void sendAllContents(@Nonnull Container container, @Nonnull NonNullList<ItemStack> nonNullList) {
        updateTasks();
    }

    @Override
    public void sendSlotContents(@Nonnull Container container, int i, @Nonnull ItemStack itemStack) {
        // Ignore changes outside of main inventory (e.g. crafting grid and armor)
        if (i >= 9 && i <= 44) {
            updateTasks();
        }
    }

    @Override
    public void sendWindowProperty(@Nonnull Container container, int i, int i1) {
    }

    @Override
    public void sendAllWindowProperties(@Nonnull Container container, @Nonnull IInventory iInventory) {
    }

    private void updateTasks() {
        EventHandler.schedulePlayerInventoryCheck(player);
    }

}
