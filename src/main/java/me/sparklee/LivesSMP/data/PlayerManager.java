package me.sparklee.LivesSMP.data;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PlayerManager {

    private final LivesSMP plugin;
    private final Map<UUID, Integer> livesCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> dirty = new ConcurrentHashMap<>();
    private final AtomicBoolean saveQueued = new AtomicBoolean(false);
    private final Object yamlLock = new Object();

    private final File dataFile;
    private YamlConfiguration dataConfig;

    public PlayerManager(LivesSMP plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadDataFile();
        migrateLegacyConfigData();
    }

    // ------------------------------------------------------------------
    // Configuration
    // ------------------------------------------------------------------

    public int getMaxLives() {
        int max = plugin.getConfig().getInt("max-lives", 10);
        return max < 0 ? -1 : Math.max(1, max);
    }

    public boolean isUnlimitedLives() {
        return getMaxLives() == -1;
    }

    public int getDefaultLives() {
        int starting = Math.max(0, plugin.getConfig().getInt("starting-lives", 3));
        int max = getMaxLives();
        if (max != -1 && starting > max) {
            starting = max;
        }
        return starting;
    }

    // ------------------------------------------------------------------
    // Cache / storage helpers
    // ------------------------------------------------------------------

    public boolean hasData(Player player) {
        return hasData(player.getUniqueId());
    }

    public boolean hasData(UUID uuid) {
        return livesCache.containsKey(uuid);
    }

    public void cacheLives(UUID uuid, int lives) {
        livesCache.put(uuid, lives);
    }

    private boolean usingDatabase() {
        return plugin.getDatabaseManager() != null && plugin.getDatabaseManager().isConfigured();
    }

    /**
     * Fast lookup used on hot paths. Never performs I/O; online players are cached on join.
     */
    public int getLives(Player player) {
        return getLives(player.getUniqueId());
    }

    public int getLives(UUID uuid) {
        Integer cached = livesCache.get(uuid);
        return cached != null ? cached : getDefaultLives();
    }

    public int getLives(OfflinePlayer player) {
        return getLivesLoaded(player.getUniqueId());
    }

    /**
     * Lookup that falls back to the storage backend when the player is not cached.
     * Used by admin commands and revival so offline players are never reset to defaults.
     */
    public int getLivesLoaded(UUID uuid) {
        Integer stored = getStoredLives(uuid);
        return stored != null ? stored : getDefaultLives();
    }

    /**
     * Same as {@link #getLivesLoaded(UUID)} but distinguishes a database failure
     * ({@code null}) from a valid value. Callers that mutate lives should use this
     * to avoid overwriting real data with a default on a transient database error.
     */
    public Integer getStoredLives(UUID uuid) {
        Integer cached = livesCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        if (usingDatabase()) {
            Integer stored = plugin.getDatabaseManager().getLives(uuid.toString());
            if (stored == null) {
                return null;
            }
            if (stored < 0) {
                return getDefaultLives();
            }
            livesCache.put(uuid, stored);
            return stored;
        }

        synchronized (yamlLock) {
            String key = uuid.toString();
            if (dataConfig.contains(key)) {
                int stored = dataConfig.getInt(key, getDefaultLives());
                livesCache.put(uuid, stored);
                return stored;
            }
        }
        return getDefaultLives();
    }

    /**
     * Loads a player for the YAML backend.
     *
     * @return true when stored data existed, false when the player is new
     */
    public boolean loadPlayer(UUID uuid) {
        if (livesCache.containsKey(uuid)) {
            return true;
        }

        synchronized (yamlLock) {
            String key = uuid.toString();
            if (dataConfig.contains(key)) {
                livesCache.put(uuid, dataConfig.getInt(key, getDefaultLives()));
                return true;
            }
        }

        // Brand new player — persist the default so a crash does not lose the record.
        setLives(uuid, getDefaultLives());
        return false;
    }

    // ------------------------------------------------------------------
    // Mutations
    // ------------------------------------------------------------------

    public void setLives(UUID uuid, int lives) {
        int normalized = Math.max(0, lives);
        int max = getMaxLives();
        if (max != -1 && normalized > max) {
            normalized = max;
        }

        livesCache.put(uuid, normalized);
        dirty.put(uuid, normalized);
        queueSave();
    }

    public void setLives(Player player, int lives) {
        setLives(player.getUniqueId(), lives);
    }

    /**
     * @return the player's new lives value
     */
    public int addLives(UUID uuid, int amount) {
        Integer stored = getStoredLives(uuid);
        if (stored == null) {
            // Database error — do not write a guessed value over real data.
            return getDefaultLives();
        }

        int current = stored;
        if (amount <= 0) {
            return current;
        }

        int max = getMaxLives();
        if (max != -1 && current >= max) {
            return current;
        }

        int newLives = current + amount;
        if (max != -1) {
            newLives = Math.min(newLives, max);
        }

        setLives(uuid, newLives);
        return newLives;
    }

    /**
     * @return the player's new lives value
     */
    public int removeLives(UUID uuid, int amount) {
        Integer stored = getStoredLives(uuid);
        if (stored == null) {
            // Database error — do not write a guessed value over real data.
            return getDefaultLives();
        }

        int current = stored;
        if (amount <= 0) {
            return current;
        }

        int newLives = Math.max(0, current - amount);
        setLives(uuid, newLives);
        return newLives;
    }

    public int decrementLife(Player player) {
        return removeLives(player.getUniqueId(), 1);
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    private void loadDataFile() {
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("[LivesSMP] Could not create the plugin data folder.");
            }
            if (!dataFile.exists() && !dataFile.createNewFile()) {
                plugin.getLogger().warning("[LivesSMP] Could not create data.yml.");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("[LivesSMP] Failed to create data.yml: " + e.getMessage());
        }

        synchronized (yamlLock) {
            dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            dataConfig.options().setHeader(List.of(
                    "LivesSMP player data",
                    "Do not edit this file while the server is running."
            ));
        }
    }

    /**
     * Older versions stored player lives inside config.yml under {@code data.<uuid>}.
     * Move those entries to data.yml before config migrations can touch them.
     */
    private void migrateLegacyConfigData() {
        ConfigurationSection legacy = plugin.getConfig().getConfigurationSection("data");
        if (legacy == null || legacy.getKeys(false).isEmpty()) {
            return;
        }

        int migrated = 0;
        synchronized (yamlLock) {
            for (String key : legacy.getKeys(false)) {
                if (!dataConfig.contains(key)) {
                    dataConfig.set(key, legacy.getInt(key, getDefaultLives()));
                    migrated++;
                }
            }
            if (migrated > 0) {
                saveYamlNow();
            }
        }

        plugin.getConfig().set("data", null);
        if (!usingDatabase()) {
            // Keep the legacy section when MySQL is active so the data is not lost
            // if the server is later switched back to YAML storage.
            plugin.saveConfig();
        }

        if (migrated > 0) {
            plugin.getLogger().info("✓ Migrated " + migrated + " player record(s) from config.yml to data.yml.");
        }
    }

    private void queueSave() {
        if (!plugin.isEnabled()) {
            flushDirty();
            return;
        }

        if (saveQueued.compareAndSet(false, true)) {
            Scheduler.async(plugin, () -> {
                saveQueued.set(false);
                flushDirty();
            });
        }
    }

    private void flushDirty() {
        if (dirty.isEmpty()) {
            return;
        }

        Map<UUID, Integer> batch = new HashMap<>(dirty);
        batch.forEach((uuid, lives) -> dirty.remove(uuid, lives));

        if (usingDatabase()) {
            batch.forEach((uuid, lives) -> {
                if (!plugin.getDatabaseManager().setLives(uuid.toString(), lives)) {
                    // Keep any newer value that was queued while this write was failing.
                    dirty.putIfAbsent(uuid, lives);
                }
            });
            return;
        }

        synchronized (yamlLock) {
            batch.forEach((uuid, lives) -> dataConfig.set(uuid.toString(), lives));
            saveYamlNow();
        }
    }

    /**
     * Must be called while holding {@link #yamlLock}.
     */
    private void saveYamlNow() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[LivesSMP] Failed to save data.yml: " + e.getMessage());
        }
    }

    /**
     * Synchronous flush used on shutdown.
     */
    public void saveData() {
        livesCache.forEach(dirty::put);
        flushDirty();
    }

    /**
     * @return sorted leaderboard entries, or {@code null} when the database query failed
     */
    public List<Map.Entry<UUID, Integer>> getTopLives(int limit) {
        Map<UUID, Integer> snapshot = new HashMap<>();

        if (usingDatabase()) {
            List<DatabaseManager.LivesEntry> entries = plugin.getDatabaseManager().getTopLives(limit);
            if (entries == null) {
                return null;
            }
            for (DatabaseManager.LivesEntry entry : entries) {
                snapshot.put(entry.uuid(), entry.lives());
            }
        } else {
            synchronized (yamlLock) {
                for (String key : dataConfig.getKeys(false)) {
                    try {
                        snapshot.put(UUID.fromString(key), dataConfig.getInt(key));
                    } catch (IllegalArgumentException ignored) {
                        // Not a UUID key — ignore it instead of crashing the leaderboard.
                    }
                }
            }
            snapshot.putAll(livesCache);
        }

        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>();
        snapshot.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .forEach(entry -> sorted.add(new AbstractMap.SimpleImmutableEntry<>(entry)));
        return sorted;
    }
}
