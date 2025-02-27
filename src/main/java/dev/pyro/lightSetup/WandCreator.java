package dev.pyro.lightSetup;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating and managing wands used to interact with Redstone Lamps.
 */
public class WandCreator {
    private static final String TICK_LORE_PREFIX = "§7Intervallo: ";
    private static final String WAND_NAME = "§bBastone delle Luci";

    private static NamespacedKey tickKey;
    private static Plugin pluginInstance;

    /**
     * Initializes the WandCreator with the plugin instance.
     *
     * @param plugin the plugin instance
     */
    public static void initialize(Plugin plugin) {
        pluginInstance = plugin;
        tickKey = new NamespacedKey(plugin, "lamp_tick_value");
    }

    /**
     * Creates a wand item with the specified number of ticks.
     *
     * @param ticks the number of ticks for the wand
     * @return the created wand item
     */
    public static ItemStack createWand(int ticks) {
        if (pluginInstance == null) {
            throw new IllegalStateException("WandCreator non è stato inizializzato!");
        }

        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();

        meta.setDisplayName(WAND_NAME + " §7(" + ticks + " tick)");

        List<String> lore = new ArrayList<>();
        lore.add(TICK_LORE_PREFIX + ticks + " tick");
        lore.add("§7Clicca con il tasto destro su una Redstone Lamp");
        meta.setLore(lore);

        try {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        } catch (Exception e) {
            try {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            } catch (Exception e2) {
                // Ignora errori - l'incantesimo non è essenziale per il funzionamento
            }
        }

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        try {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(tickKey, PersistentDataType.INTEGER, ticks);
        } catch (Exception e) {
            // Ignora errori - il valore verrà comunque letto dalla lore
        }

        wand.setItemMeta(meta);

        return wand;
    }

    /**
     * Gets the number of ticks from the wand item.
     *
     * @param wand the wand item
     * @return the number of ticks
     */
    public static int getWandTicks(ItemStack wand) {
        if (wand == null || !wand.hasItemMeta()) {
            return 20;
        }

        ItemMeta meta = wand.getItemMeta();

        try {
            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (container.has(tickKey, PersistentDataType.INTEGER)) {
                return container.get(tickKey, PersistentDataType.INTEGER);
            }
        } catch (Exception e) {
            // Se non riesce a leggere dai metadati, continua con la lore
        }

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();

            for (String line : lore) {
                if (line.startsWith(TICK_LORE_PREFIX)) {
                    try {
                        String tickStr = line.substring(TICK_LORE_PREFIX.length());
                        tickStr = tickStr.replace(" tick", "");
                        return Integer.parseInt(tickStr);
                    } catch (Exception e) {
                        // Ignora errori di parsing
                    }
                }
            }
        }
        return 20;
    }

    /**
     * Checks if the given item is a valid wand.
     *
     * @param item the item to check
     * @return true if the item is a valid wand, otherwise false
     */
    public static boolean isValidWand(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) {
            return false;
        }

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();

        if (displayName.equals(WAND_NAME)) {
            return true;
        }

        String strippedActual = stripColorCodes(displayName);
        String strippedExpected = stripColorCodes(WAND_NAME);

        if (strippedActual.equals(strippedExpected)) {
            return true;
        }

        if (strippedActual.contains("Bastone delle Luci")) {
            return true;
        }

        return false;
    }

    /**
     * Strips color codes from the given string.
     *
     * @param input the string to strip color codes from
     * @return the string without color codes
     */
    private static String stripColorCodes(String input) {
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}