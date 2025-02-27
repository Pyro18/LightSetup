package dev.pyro.lightSetup;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Main class for the LightSetup plugin.
 * This class handles the plugin's lifecycle and configuration setup.
 */
public class LightSetup extends JavaPlugin {
    private LampManager lampManager;
    private static LightSetup instance;

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin instance, configuration file, and registers event listeners and commands.
     */
    @Override
    public void onEnable() {
        instance = this;

        setupConfigFile();

        WandCreator.initialize(this);
        lampManager = new LampManager(this);

        getServer().getPluginManager().registerEvents(new LampListener(this, lampManager), this);
        getServer().getPluginManager().registerEvents(new LampBreakListener(this, lampManager), this);

        PluginCommand command = getCommand("setlights");
        if (command != null) {
            command.setExecutor(new SetLightsCommand(this, lampManager));
            getLogger().info("Comando '/setlights' registrato con successo");
        } else {
            getLogger().warning("Impossibile registrare il comando '/setlights'");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("LightSetup abilitato con successo");
    }

    /**
     * Called when the plugin is disabled.
     * Saves and stops all active lamps managed by the LampManager.
     */
    @Override
    public void onDisable() {
        if (lampManager != null) {
            getLogger().info("Salvataggio delle lampade attive...");

            lampManager.saveAllLamps();
            lampManager.stopAllLamps();
        }

        getLogger().info("LightSetup disabilitato");
        instance = null;
    }

    /**
     * Gets the instance of the LightSetup plugin.
     *
     * @return the instance of the LightSetup plugin
     */
    public static LightSetup getInstance() {
        return instance;
    }

    /**
     * Sets up the configuration file for the plugin.
     * Creates the configuration file from a template if it does not exist.
     */
    private void setupConfigFile() {
        if (!getDataFolder().exists()) {
            getLogger().info("Creazione cartella del plugin...");
            getDataFolder().mkdirs();
        }

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            getLogger().info("File config.yml non trovato, creazione da template...");

            try (InputStream in = getResource("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("File config.yml creato con successo");
                } else {
                    configFile.createNewFile();
                    getLogger().info("Creato file config.yml vuoto");
                }
            } catch (IOException e) {
                getLogger().severe("Errore durante la creazione del file config.yml: " + e.getMessage());
            }
        }

        reloadConfig();
        getLogger().info("Configurazione caricata");
    }
}