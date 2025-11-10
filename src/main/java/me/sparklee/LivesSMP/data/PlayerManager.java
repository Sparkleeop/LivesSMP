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
    //               GET LIVES METHODS
    // ==================================================

    public int getLives(Player player) {
        return getLives(player.getUniqueId());
    }

    public int getLives(OfflinePlayer offlinePlayer) {
        return getLives(offlinePlayer.getUniqueId());
    }

    public int getLives(UUID uuid) {
        int defaultLives = plugin.getConfig().getInt("starting-lives", 3);

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
    //             DECREMENT + SAVE HANDLERS
    // ==================================================

    public int decrementLife(Player player) {
        int lives = getLives(player) - 1;
        if (lives < 0) lives = 0;
        setLives(player, lives);
        return lives;
    }

    public void saveData() {
        if (plugin.getDatabaseManager().isEnabled()) return;
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data.yml!");
        }
    }
}
