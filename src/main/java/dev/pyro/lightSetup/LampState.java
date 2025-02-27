package dev.pyro.lightSetup;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;

/**
 * Enum representing the state of a Redstone Lamp.
 */
public enum LampState {
    ON(true),
    OFF(false);

    private final boolean lit;

    /**
     * Constructs a LampState with the specified lit state.
     *
     * @param lit whether the lamp is lit
     */
    LampState(boolean lit) {
        this.lit = lit;
    }

    /**
     * Gets the BlockData for the specified block with the current lamp state.
     *
     * @param block the block to get the BlockData for
     * @return the BlockData with the current lamp state
     */
    public BlockData getBlockData(Block block) {
        BlockData blockData = Material.REDSTONE_LAMP.createBlockData();
        if (blockData instanceof Lightable lightable) {
            lightable.setLit(lit);
        }
        return blockData;
    }
}