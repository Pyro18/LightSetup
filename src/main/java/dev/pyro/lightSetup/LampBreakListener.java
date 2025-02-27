package dev.pyro.lightSetup;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listener for block break events specifically targeting Redstone Lamps.
 * This listener deactivates lamps managed by the LampManager when they are broken.
 */
public class LampBreakListener implements Listener {
    private final Plugin plugin;
    private final LampManager lampManager;

    /**
     * Constructs a new LampBreakListener.
     *
     * @param plugin the plugin instance
     * @param lampManager the lamp manager instance
     */
    public LampBreakListener(Plugin plugin, LampManager lampManager) {
        this.plugin = plugin;
        this.lampManager = lampManager;
    }

    /**
     * Handles the BlockBreakEvent.
     * If the broken block is an active Redstone Lamp, it deactivates the lamp and notifies the player.
     *
     * @param event the block break event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.REDSTONE_LAMP) {
            if (lampManager.isLampActive(block)) {
                plugin.getLogger().info("Lampada attiva rotta a " + block.getLocation() + ", rimozione in corso...");

                lampManager.deactivateLamp(block);

                event.getPlayer().sendMessage("§cHai rotto una lampada attiva!");
            }
        }
    }
}