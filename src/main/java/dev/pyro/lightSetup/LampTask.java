package dev.pyro.lightSetup;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task that toggles the state of a Redstone Lamp.
 * This task is scheduled to run periodically to switch the lamp on and off.
 */
public class LampTask extends BukkitRunnable {
    private final Block lamp;
    private boolean isLit;
    private final Plugin plugin;

    /**
     * Constructs a new LampTask.
     *
     * @param lamp the Redstone Lamp block to be toggled
     */
    public LampTask(Block lamp) {
        this.lamp = lamp;
        this.isLit = false;
        this.plugin = LightSetup.getInstance();
    }

    /**
     * The main logic of the task that toggles the lamp's state.
     * This method is called periodically by the scheduler.
     */
    @Override
    public void run() {
        try {
            if (!lamp.getType().equals(Material.REDSTONE_LAMP)) {
                this.cancel();
                return;
            }

            isLit = !isLit;

            BlockData blockData = lamp.getBlockData();

            if (blockData instanceof Lightable) {
                Lightable lightable = (Lightable) blockData;
                lightable.setLit(isLit);
                lamp.setBlockData(lightable);
            } else {
                this.cancel();
            }
        } catch (Exception e) {
            if (plugin != null) {
                e.printStackTrace();
            }
            this.cancel();
        }
    }

    /**
     * Formats the location of the block into a string.
     *
     * @param block the block whose location is to be formatted
     * @return the formatted location string
     */
    private String formatLocation(Block block) {
        return block.getWorld().getName() + ":" +
                block.getX() + ":" +
                block.getY() + ":" +
                block.getZ();
    }
}