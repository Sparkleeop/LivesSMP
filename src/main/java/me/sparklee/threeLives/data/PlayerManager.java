package me.sparklee.threeLives.data;

import me.sparklee.threeLives.ThreeLivesSMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerManager {
    private final ThreeLivesSMP plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerManager(ThreeLivesSMP plugin) {
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

    public int getLives(Player player) {
        UUID uuid = player.getUniqueId();

        // ✅ MySQL mode
        if (plugin.getDatabaseManager().isEnabled()) {
            int lives = plugin.getDatabaseManager().getLives(uuid.toString());
            if (lives == -1) {
                setLives(uuid, 3);
                return 3;
            }
            return lives;
        }

        // ✅ File mode
        return dataConfig.getInt("data." + uuid, 3);
    }

    public void setLives(Player player, int lives) {
        setLives(player.getUniqueId(), lives);
    }

    public void setLives(UUID uuid, int lives) {
        // ✅ MySQL mode
        if (plugin.getDatabaseManager().isEnabled()) {
            plugin.getDatabaseManager().setLives(uuid.toString(), lives);
            return;
        }

        // ✅ File mode
        dataConfig.set("data." + uuid, lives);
        saveData();
    }

    public boolean hasData(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.getDatabaseManager().isEnabled()) {
            return plugin.getDatabaseManager().getLives(uuid.toString()) != -1;
        }
        return dataConfig.contains("data." + uuid);
    }

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
