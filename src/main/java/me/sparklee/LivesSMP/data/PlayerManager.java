package me.sparklee.LivesSMP.data;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final LivesSMP plugin;
    private final Map<UUID, Integer> livesCache = new ConcurrentHashMap<>();

    public PlayerManager(LivesSMP plugin) {
        this.plugin = plugin;
    }

    public int getMaxLives() {
        return plugin.getConfig().getInt("max-lives", 10);
    }

    public boolean isUnlimitedLives() {
        return getMaxLives() == -1;
    }

    public int getDefaultLives() {
        return plugin.getConfig().getInt("starting-lives", 3);
    }

    public boolean hasData(Player player) {
        return livesCache.containsKey(player.getUniqueId());
    }

    public boolean hasData(UUID uuid) {
        return livesCache.containsKey(uuid);
    }

    public void loadPlayer(UUID uuid) {
        if (!plugin.getDatabaseManager().isEnabled()) {
            int lives = plugin.getConfig().getInt("data." + uuid, getDefaultLives());
            livesCache.put(uuid, lives);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int lives = plugin.getDatabaseManager().getLives(uuid.toString());
            if (lives != -1) {
                livesCache.put(uuid, lives);
            }
        });
    }

    public int getLives(Player player) {
        return getLives(player.getUniqueId());
    }

    public int getLives(UUID uuid) {
        return livesCache.getOrDefault(uuid, getDefaultLives());
    }

    // Needed for offline player lookups (e.g. AddLivesCommand, CheckLivesCommand)
    public int getLives(org.bukkit.OfflinePlayer player) {
        return getLives(player.getUniqueId());
    }

    public void setLives(UUID uuid, int lives) {
        livesCache.put(uuid, lives);

        if (plugin.getDatabaseManager().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                    plugin.getDatabaseManager().setLives(uuid.toString(), lives)
            );
        }
    }

    public void setLives(Player player, int lives) {
        setLives(player.getUniqueId(), lives);
    }

    public int addLives(UUID uuid, int amount) {
        int current = getLives(uuid);
        int max = getMaxLives();

        if (!isUnlimitedLives() && current + amount > max) {
            amount = Math.max(0, max - current);
        }

        int newLives = isUnlimitedLives()
                ? current + amount
                : Math.min(current + amount, max);

        setLives(uuid, newLives);
        return newLives;
    }

    public int removeLives(UUID uuid, int amount) {
        int newLives = Math.max(0, getLives(uuid) - amount);
        setLives(uuid, newLives);
        return newLives;
    }

    public int decrementLife(Player player) {
        return removeLives(player.getUniqueId(), 1);
    }

    public void unload(UUID uuid) {
        livesCache.remove(uuid);
    }

    public void saveData() {
        if (plugin.getDatabaseManager().isEnabled()) {
            livesCache.forEach((uuid, lives) ->
                    plugin.getDatabaseManager().setLives(uuid.toString(), lives)
            );
        } else {
            livesCache.forEach((uuid, lives) ->
                    plugin.getConfig().set("data." + uuid, lives)
            );
            plugin.saveConfig();
        }
    }
}