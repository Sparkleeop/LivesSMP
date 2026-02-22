package me.sparklee.LivesSMP.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Set;

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
        if (!config.exists()) {
            plugin.saveResource(fileName, false);
        }

        configFile = YamlConfiguration.loadConfiguration(config);
        FileConfiguration defaultConfig = getDefaultConfig();

        if (defaultConfig == null) {
            plugin.getLogger().severe("✘ Failed to load default config from JAR!");
            return;
        }

        int currentVersion = configFile.getInt("config-version", 1);
        int defaultVersion = defaultConfig.getInt("config-version", 1);

        if (currentVersion < defaultVersion) {
            plugin.getLogger().warning("⚠ Outdated " + fileName + " detected (v" + currentVersion + "). Updating to v" + defaultVersion + "...");
            createBackup(currentVersion);

            mergeConfigs(configFile, defaultConfig, "");

            removeUnusedKeys(configFile, defaultConfig, "");

            configFile.set("config-version", defaultVersion);

            try {
                configFile.save(config);
                plugin.getLogger().info("✓ Successfully merged new config options into " + fileName + " (v" + defaultVersion + ").");
            } catch (IOException e) {
                plugin.getLogger().severe("✘ Failed to save merged " + fileName + ": " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("✓ " + fileName + " is up to date (v" + currentVersion + ").");
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

    private void mergeConfigs(FileConfiguration target, FileConfiguration defaults, String path) {
        Set<String> keys = defaults.getConfigurationSection(path).getKeys(false);
        for (String key : keys) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (defaults.isConfigurationSection(fullPath)) {
                if (!target.isConfigurationSection(fullPath)) {
                    target.createSection(fullPath);
                }
                mergeConfigs(target, defaults, fullPath);
            } else {
                if (!target.contains(fullPath)) {
                    target.set(fullPath, defaults.get(fullPath));
                    plugin.getLogger().info("✓ Added missing key: " + fullPath);
                }
            }
        }
    }


    private void removeUnusedKeys(FileConfiguration current, FileConfiguration defaults, String path) {
        if (current.getConfigurationSection(path) == null) return;
        for (String key : current.getConfigurationSection(path).getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (current.isConfigurationSection(fullPath)) {
                removeUnusedKeys(current, defaults, fullPath);
                if (current.getConfigurationSection(fullPath).getKeys(false).isEmpty()
                        && !defaults.contains(fullPath)) {
                    current.set(fullPath, null);
                    plugin.getLogger().info("✘️ Removed deprecated section: " + fullPath);
                }
            } else if (!defaults.contains(fullPath)) {
                current.set(fullPath, null);
                plugin.getLogger().info("✘ Removed deprecated key: " + fullPath);
            }
        }
    }

    private void createBackup(int version) {
        File backup = new File(plugin.getDataFolder(), fileName.replace(".yml", "_backup_v" + version + ".yml"));
        try {
            if (!backup.exists()) {
                configFile.save(backup);
                plugin.getLogger().info("Old " + fileName + " backed up as " + backup.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("⚠ Failed to back up old " + fileName + ": " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return configFile;
    }
}
