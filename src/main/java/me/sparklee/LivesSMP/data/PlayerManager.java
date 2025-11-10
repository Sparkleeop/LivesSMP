package me.sparklee.LivesSMP.data;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerManager {

    private final LivesSMP plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerManager(LivesSMP plugin) {
        this.plugin = plugin;

        // Load or create data.yml only if MySQL is disabled
        if (!plugin.getDatabaseManager().isEnabled()) {
            dataFile = new File(plugin.getDataFolder(), "data.yml");
            if (!dataFile.exists()) {
                try {
                    dataFile.getParentFile().mkdirs();
                    dataFile.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to create data.yml!");
                }
            }
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        }
    }

    // ==================================================
    //                 CONFIG HELPERS
    // ==================================================

    /**
     * Returns the configured max lives from config.yml
     * If set to -1, it means there is no limit.
     */
    public int getMaxLives() {
        return plugin.getConfig().getInt("max-lives", 10);
    }

    /**
     * Returns true if there is no max-lives limit (-1 in config.yml)
     */
    public boolean isUnlimitedLives() {
        return getMaxLives() == -1;
    }

    /**
     * Returns the default starting lives (configurable)
     */
    public int getDefaultLives() {
        return plugin.getConfig().getInt("starting-lives", 3);
    }

    // ==================================================
    //               GET LIVES METHODS
    // ==================================================

    public int getLives(Player player) {
        return getLives(player.getUniqueId());
    }

    public int getLives(OfflinePlayer offlinePlayer) {
        return getLives(offlinePlayer.getUniqueId());
    }

    public int getLives(UUID uuid) {
        int defaultLives = getDefaultLives();

        // MySQL mode
        if (plugin.getDatabaseManager().isEnabled()) {
            int lives = plugin.getDatabaseManager().getLives(uuid.toString());
            if (lives == -1) {
                setLives(uuid, defaultLives);
                return defaultLives;
            }
            return lives;
        }

        // File mode
        return dataConfig.getInt("data." + uuid, defaultLives);
    }

    // ==================================================
    //               SET LIVES METHODS
    // ==================================================

    public void setLives(Player player, int lives) {
        setLives(player.getUniqueId(), lives);
    }

    public void setLives(UUID uuid, int lives) {
        // MySQL mode
        if (plugin.getDatabaseManager().isEnabled()) {
            plugin.getDatabaseManager().setLives(uuid.toString(), lives);
            return;
        }

        // File mode
        dataConfig.set("data." + uuid, lives);
        saveData();
    }

    // ==================================================
    //               MODIFY LIVES METHODS
    // ==================================================

    /**
     * Increases a player's lives safely, respecting the max-lives limit (unless unlimited)
     */
    public int addLives(UUID uuid, int amount) {
        int current = getLives(uuid);
        int max = getMaxLives();

        if (!isUnlimitedLives() && current + amount > max) {
            amount = Math.max(0, max - current);
        }

        int newLives = isUnlimitedLives() ? current + amount : Math.min(current + amount, max);
        setLives(uuid, newLives);
        return newLives;
    }

    /**
     * Decreases a player's lives, never below 0
     */
    public int removeLives(UUID uuid, int amount) {
        int current = getLives(uuid);
        int newLives = Math.max(0, current - amount);
        setLives(uuid, newLives);
        return newLives;
    }

    /**
     * Decrements 1 life (used on player death)
     */
    public int decrementLife(Player player) {
        int lives = getLives(player) - 1;
        if (lives < 0) lives = 0;
        setLives(player, lives);
        return lives;
    }

    // ==================================================
    //                 HAS DATA CHECK
    // ==================================================

    public boolean hasData(Player player) {
        return hasData(player.getUniqueId());
    }

    public boolean hasData(UUID uuid) {
        if (plugin.getDatabaseManager().isEnabled()) {
            return plugin.getDatabaseManager().getLives(uuid.toString()) != -1;
        }
        return dataConfig.contains("data." + uuid);
    }

    // ==================================================
    //                   SAVE HANDLER
    // ==================================================

    public void saveData() {
        if (plugin.getDatabaseManager().isEnabled()) return;
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data.yml!");
        }
    }
}
