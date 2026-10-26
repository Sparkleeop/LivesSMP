package me.sparklee.LivesSMP.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final String fileName;
    private final File config;
    private FileConfiguration configFile;

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

        // Make sure JavaPlugin#getConfig() points at the file we just loaded/migrated.
        plugin.reloadConfig();
    }

    private FileConfiguration getDefaultConfig() {
        try (InputStream defStream = plugin.getResource(fileName)) {
            if (defStream == null) return null;
            return YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load default " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private void mergeConfigs(FileConfiguration target, FileConfiguration defaults, String path) {
        ConfigurationSection defaultsSection = defaults.getConfigurationSection(path);
        if (defaultsSection == null) return;

        Set<String> keys = defaultsSection.getKeys(false);
        for (String key : keys) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (defaults.isConfigurationSection(fullPath)) {
                if (!target.isConfigurationSection(fullPath)) {
                    target.createSection(fullPath);
                }
                mergeConfigs(target, defaults, fullPath);
            } else if (!target.contains(fullPath)) {
                target.set(fullPath, defaults.get(fullPath));
                plugin.getLogger().info("✓ Added missing key: " + fullPath);
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
