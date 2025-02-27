package dev.pyro.lightSetup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages the activation and deactivation of Redstone Lamps.
 * Handles saving and loading lamp states to and from a configuration file.
 */
public class LampManager {
    private final Plugin plugin;
    private final Map<Block, BukkitRunnable> activeLamps;
    private final Map<String, Integer> savedLampTicks;
    private final File configFile;

    /**
     * Constructs a new LampManager.
     *
     * @param plugin the plugin instance
     */
    public LampManager(Plugin plugin) {
        this.plugin = plugin;
        this.activeLamps = new HashMap<>();
        this.savedLampTicks = new HashMap<>();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                plugin.getLogger().info("Creato nuovo file config.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile creare config.yml: " + e.getMessage());
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.contains("lamps")) {
            config.createSection("lamps");
            try {
                config.save(configFile);
                plugin.getLogger().info("Inizializzata sezione 'lamps' in config.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("Errore salvando config.yml: " + e.getMessage());
            }
        }

        loadLamps();
    }

    /**
     * Checks if a lamp is currently active.
     *
     * @param block the block to check
     * @return true if the lamp is active, otherwise false
     */
    public boolean isLampActive(Block block) {
        return activeLamps.containsKey(block);
    }

    /**
     * Toggles the state of a Redstone Lamp with a specified number of ticks.
     *
     * @param lamp the lamp block
     * @param player the player interacting with the lamp
     * @param ticks the number of ticks for the lamp to stay active
     */
    public void toggleLampWithTicks(Block lamp, Player player, int ticks) {
        if (lamp.getType() != org.bukkit.Material.REDSTONE_LAMP) {
            player.sendMessage("§cErrore: questo blocco non è una lampada redstone!");
            return;
        }

        if (isLampActive(lamp)) {
            deactivateLamp(lamp);
            player.sendMessage("§cLampada disattivata!");
        } else {
            activateLampWithTicks(lamp, ticks);
            player.sendMessage("§aLampada attivata con intervallo di " + ticks + " tick!");
        }
    }

    /**
     * Activates a Redstone Lamp with a specified number of ticks.
     *
     * @param lamp the lamp block
     * @param ticks the number of ticks for the lamp to stay active
     */
    public void activateLampWithTicks(Block lamp, int ticks) {
        if (lamp.getType() != org.bukkit.Material.REDSTONE_LAMP) {
            return;
        }

        try {
            LampTask task = new LampTask(lamp);
            task.runTaskTimer(plugin, 0, ticks);
            activeLamps.put(lamp, task);

            String locationKey = serializeLocation(lamp.getLocation());
            savedLampTicks.put(locationKey, ticks);

            saveLamp(lamp.getLocation(), ticks);

        } catch (Exception e) {
            plugin.getLogger().severe("Errore durante l'attivazione della lampada: " + e.getMessage());
        }
    }

    /**
     * Deactivates a Redstone Lamp.
     *
     * @param lamp the lamp block
     */
    public void deactivateLamp(Block lamp) {
        if (isLampActive(lamp)) {
            try {
                activeLamps.get(lamp).cancel();
                activeLamps.remove(lamp);
                lamp.setBlockData(LampState.OFF.getBlockData(lamp));

                removeLamp(lamp.getLocation());
            } catch (Exception e) {
                plugin.getLogger().severe("Errore durante la disattivazione della lampada: " + e.getMessage());
            }
        }
    }

    /**
     * Stops all active lamps.
     */
    public void stopAllLamps() {
        for (Map.Entry<Block, BukkitRunnable> entry : activeLamps.entrySet()) {
            try {
                entry.getValue().cancel();
                entry.getKey().setBlockData(LampState.OFF.getBlockData(entry.getKey()));
            } catch (Exception e) {
                plugin.getLogger().severe("Errore durante l'arresto di una lampada: " + e.getMessage());
            }
        }

        activeLamps.clear();
    }

    /**
     * Serializes a location to a string.
     *
     * @param location the location to serialize
     * @return the serialized location string
     */
    private String serializeLocation(Location location) {
        return location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();
    }

    /**
     * Deserializes a location from a string.
     *
     * @param locationStr the serialized location string
     * @return the deserialized location
     */
    private Location deserializeLocation(String locationStr) {
        String[] parts = locationStr.split(":");

        if (parts.length != 4) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Saves a lamp's state to the configuration file.
     *
     * @param location the location of the lamp
     * @param ticks the number of ticks for the lamp to stay active
     */
    private void saveLamp(Location location, int ticks) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String locationKey = serializeLocation(location);

        if (!config.contains("lamps")) {
            config.createSection("lamps");
        }

        config.set("lamps." + locationKey, ticks);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Errore salvando lampada in config.yml: " + e.getMessage());
        }
    }

    /**
     * Removes a lamp's state from the configuration file.
     *
     * @param location the location of the lamp
     */
    private void removeLamp(Location location) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String locationKey = serializeLocation(location);

        config.set("lamps." + locationKey, null);
        savedLampTicks.remove(locationKey);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Errore rimuovendo lampada da config.yml: " + e.getMessage());
        }
    }

    /**
     * Loads all lamps from the configuration file.
     */
    public void loadLamps() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.contains("lamps")) {
            config.createSection("lamps");
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Errore creando sezione lamps: " + e.getMessage());
            }
            return;
        }

        Set<String> keys = config.getConfigurationSection("lamps").getKeys(false);
        // plugin.getLogger().info("Trovate " + keys.size() + " lampade da caricare");

        for (String locationKey : keys) {
            int ticks = config.getInt("lamps." + locationKey);
            Location location = deserializeLocation(locationKey);

            if (location != null) {
                Block lamp = location.getBlock();

                if (lamp.getType() == org.bukkit.Material.REDSTONE_LAMP) {
                    activateLampWithTicks(lamp, ticks);
                    // plugin.getLogger().info("Lampada caricata: " + locationKey + " con " + ticks + " tick");
                } else {
                    config.set("lamps." + locationKey, null);
                    plugin.getLogger().info("Lampada rimossa perché non valida: " + locationKey);
                }
            }
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Errore salvando config.yml dopo caricamento: " + e.getMessage());
        }
    }

    /**
     * Saves all active lamps to the configuration file.
     */
    public void saveAllLamps() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        config.set("config-version", 1);
        config.set("lamps", null);
        config.createSection("lamps");

        // plugin.getLogger().info("Salvando " + savedLampTicks.size() + " lampade in config.yml");

        for (Map.Entry<String, Integer> entry : savedLampTicks.entrySet()) {
            String locationKey = entry.getKey();
            int ticks = entry.getValue();

            config.set("lamps." + locationKey, ticks);
            plugin.getLogger().info("Salvata lampada: " + locationKey + " con " + ticks + " tick");
        }

        try {
            config.save(configFile);
            plugin.getLogger().info("Tutte le lampade salvate con successo in config.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("ERRORE CRITICO durante il salvataggio di config.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}