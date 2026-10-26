package me.sparklee.LivesSMP.data;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.Scheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final LivesSMP plugin;

    private volatile Connection connection;
    private volatile boolean enabled = false;
    private volatile boolean configured = false;

    private String host, database, username, password;
    private int port;
    private ScheduledTask keepAliveTask;

    public DatabaseManager(LivesSMP plugin) {
        this.plugin = plugin;
    }

    /**
     * Connects synchronously so the storage backend is known before players join.
     * If the connection fails the plugin falls back to YAML storage automatically.
     */
    public void connect() {
        if (!plugin.getConfig().getBoolean("mysql.enabled", false)) {
            plugin.getLogger().info("MySQL disabled — using YAML storage.");
            return;
        }

        host = plugin.getConfig().getString("mysql.host", "localhost");
        port = plugin.getConfig().getInt("mysql.port", 3306);
        database = plugin.getConfig().getString("mysql.database", "livesmp");
        username = plugin.getConfig().getString("mysql.username", "root");
        password = plugin.getConfig().getString("mysql.password", "");

        try {
            DriverManager.setLoginTimeout(5);
            connection = openConnection();
            setupTable();
            configured = true;
            enabled = true;
            plugin.getLogger().info("✅ Connected to MySQL successfully!");
            startKeepAlive();
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ Failed to connect to MySQL: " + e.getMessage());
            plugin.getLogger().warning("Falling back to YAML storage for this session.");
            configured = false;
            enabled = false;
            closeQuietly();
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true"
                        + "&connectTimeout=5000&socketTimeout=10000",
                username, password
        );
    }

    private void setupTable() throws SQLException {
        if (connection == null) throw new SQLException("No database connection");
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS player_lives (uuid VARCHAR(36) PRIMARY KEY, lives INT NOT NULL)"
        )) {
            ps.executeUpdate();
        }
    }

    private void startKeepAlive() {
        keepAliveTask = Scheduler.asyncTimer(plugin, () -> {
            if (!configured) return;
            ensureConnection();
            if (connection == null) return;
            try (PreparedStatement ps = connection.prepareStatement("SELECT 1")) {
                ps.executeQuery();
            } catch (SQLException e) {
                plugin.getLogger().warning("[LivesSMP] MySQL keep-alive failed: " + e.getMessage());
                reconnect();
            }
        }, 20L * 60 * 5, 20L * 60 * 5);
    }

    private synchronized void ensureConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                reconnect();
            }
        } catch (SQLException e) {
            reconnect();
        }
    }

    private synchronized void reconnect() {
        closeQuietly();
        try {
            connection = openConnection();
            setupTable();
            enabled = true;
            plugin.getLogger().info("🔄 Reconnected to MySQL successfully!");
        } catch (SQLException e) {
            connection = null;
            enabled = false;
            plugin.getLogger().severe("❌ MySQL reconnection failed: " + e.getMessage());
        }
    }

    private void closeQuietly() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
        } finally {
            connection = null;
        }
    }

    public boolean isEnabled() {
        return enabled && connection != null;
    }

    /**
     * True when MySQL was selected and successfully connected at startup.
     * Unlike {@link #isEnabled()} this stays true during a temporary outage so the
     * plugin keeps writing to the database instead of silently switching to YAML.
     */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * @return the stored lives, {@code -1} when the player has no row, or {@code null} on a database error.
     */
    public synchronized Integer getLives(String uuid) {
        if (!configured) return null;
        ensureConnection();
        if (!isEnabled()) return null;

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT lives FROM player_lives WHERE uuid=?"
        )) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("lives") : -1;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[LivesSMP] getLives() SQL error: " + e.getMessage());
            return null;
        }
    }

    /**
     * @return true when the value was persisted, false on a database error.
     */
    public synchronized boolean setLives(String uuid, int lives) {
        if (!configured) return false;
        ensureConnection();
        if (!isEnabled()) return false;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO player_lives (uuid, lives) VALUES (?, ?) ON DUPLICATE KEY UPDATE lives=?"
        )) {
            ps.setString(1, uuid);
            ps.setInt(2, lives);
            ps.setInt(3, lives);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("[LivesSMP] setLives() SQL error: " + e.getMessage());
            return false;
        }
    }

    /**
     * @return the leaderboard entries, or {@code null} when the query failed.
     */
    public synchronized List<LivesEntry> getTopLives(int limit) {
        List<LivesEntry> entries = new ArrayList<>();
        if (!configured) return entries;
        ensureConnection();
        if (!isEnabled()) return null;

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT uuid, lives FROM player_lives ORDER BY lives DESC LIMIT ?"
        )) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        entries.add(new LivesEntry(UUID.fromString(rs.getString("uuid")), rs.getInt("lives")));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("[LivesSMP] Skipping invalid UUID in leaderboard: " + rs.getString("uuid"));
                    }
                }
            }
            return entries;
        } catch (SQLException e) {
            plugin.getLogger().severe("[LivesSMP] Failed to fetch MySQL leaderboard: " + e.getMessage());
            return null;
        }
    }

    public synchronized void close() {
        enabled = false;
        configured = false;
        if (keepAliveTask != null) {
            keepAliveTask.cancel();
            keepAliveTask = null;
        }
        closeQuietly();
    }

    public record LivesEntry(UUID uuid, int lives) {
    }
}
