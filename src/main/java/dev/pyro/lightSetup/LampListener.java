package dev.pyro.lightSetup;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Listener for player interactions with Redstone Lamps.
 * This listener toggles the state of Redstone Lamps when a player interacts with them using a specific wand item.
 */
public class LampListener implements Listener {
    private final Plugin plugin;
    private final LampManager lampManager;

    /**
     * Constructs a new LampListener.
     *
     * @param plugin the plugin instance
     * @param lampManager the lamp manager instance
     */
    public LampListener(Plugin plugin, LampManager lampManager) {
        this.plugin = plugin;
        this.lampManager = lampManager;
    }

    /**
     * Handles the PlayerInteractEvent.
     * If the player right-clicks a Redstone Lamp with a specific wand item, it toggles the lamp's state.
     *
     * @param event the player interact event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.REDSTONE_LAMP) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) {
            return;
        }

        if (!isWandItem(item)) {
            return;
        }

        event.setCancelled(true);

        int ticks = WandCreator.getWandTicks(item);

        lampManager.toggleLampWithTicks(clickedBlock, event.getPlayer(), ticks);
    }

    /**
     * Checks if the given item is the specific wand item used to toggle lamps.
     *
     * @param item the item to check
     * @return true if the item is the wand item, otherwise false
     */
    private boolean isWandItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();

        if (displayName.equals("§bBastone delle Luci")) {
            return true;
        }

        if (displayName.contains("Bastone delle Luci")) {
            return true;
        }

        return false;
    }
}