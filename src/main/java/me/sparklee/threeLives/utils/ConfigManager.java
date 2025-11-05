package me.sparklee.threeLives.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final String fileName;
    private FileConfiguration configFile;
    private File config;

    public ConfigManager(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.config = new File(plugin.getDataFolder(), fileName);
    }

    public void load() {
        // Create config if not exists
        if (!config.exists()) {
            plugin.saveResource(fileName, false);
        }

        configFile = YamlConfiguration.loadConfiguration(config);
        FileConfiguration defaultConfig = getDefaultConfig();

        // Check for version field
        int currentVersion = configFile.getInt("config-version", 1);
        int defaultVersion = defaultConfig.getInt("config-version", 1);

        if (currentVersion < defaultVersion) {
            plugin.getLogger().warning("⚠ Outdated " + fileName + " detected (v" + currentVersion + "). Updating to v" + defaultVersion + "...");

            // Option 1: Backup old config
            File backup = new File(plugin.getDataFolder(), fileName.replace(".yml", "_backup_v" + currentVersion + ".yml"));
            try {
                configFile.save(backup);
                plugin.getLogger().info("Old " + fileName + " backed up as " + backup.getName());
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to back up old " + fileName + ": " + e.getMessage());
            }

            // Option 2: Replace with new default
            config.delete();
            plugin.saveResource(fileName, false);

            configFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), fileName));
            plugin.getLogger().info(fileName + " updated to v" + defaultVersion + ".");
        } else {
            plugin.getLogger().info(fileName + " is up to date (v" + currentVersion + ").");
        }
    }

    private FileConfiguration getDefaultConfig() {
        try (InputStream defStream = plugin.getResource(fileName)) {
            if (defStream == null) return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load default " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    public FileConfiguration getConfig() {
        return configFile;
    }
}
