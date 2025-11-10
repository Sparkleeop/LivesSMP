package me.sparklee.LivesSMP.data;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;

public class DatabaseManager {
    private final LivesSMP plugin;
    private Connection connection;
    private boolean enabled = false;

    private String host, database, username, password;
    private int port;

    public DatabaseManager(LivesSMP plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        if (!plugin.getConfig().getBoolean("mysql.enabled")) {
            plugin.getLogger().info("MySQL disabled — using YAML storage.");
            return;
        }

        host = plugin.getConfig().getString("mysql.host");
        port = plugin.getConfig().getInt("mysql.port");
        database = plugin.getConfig().getString("mysql.database");
        username = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true",
                    username, password
            );
            plugin.getLogger().info("✅ Connected to MySQL successfully!");
            enabled = true;
            setupTable();
            startKeepAlive();
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ Failed to connect to MySQL: " + e.getMessage());
            enabled = false;
        }
    }

    private void setupTable() {
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS player_lives (uuid VARCHAR(36) PRIMARY KEY, lives INT)"
        )) {
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create table: " + e.getMessage());
        }
    }

    /**
     * Keeps the MySQL connection alive every 5 minutes.
     */
    private void startKeepAlive() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled) return;
                try {
                    if (!isConnected()) {
                        plugin.getLogger().warning("[LivesSMP] MySQL connection lost! Attempting reconnect...");
                        reconnect();
                    } else {
                        try (PreparedStatement ps = connection.prepareStatement("SELECT 1")) {
                            ps.executeQuery();
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("[LivesSMP] MySQL keep-alive failed: " + e.getMessage());
                    reconnect();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60 * 5, 20L * 60 * 5); // every 5 min
    }

    /**
     * Checks if the connection is valid.
     */
    private boolean isConnected() {
        try {
            return connection != null && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Attempts to reconnect safely.
     */
    private synchronized void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true",
                    username, password
            );
            plugin.getLogger().info("🔄 Reconnected to MySQL successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ MySQL reconnection failed: " + e.getMessage());
        }
    }

    private void ensureConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                reconnect();
            }
        } catch (SQLException e) {
            reconnect();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getLives(String uuid) {
        if (!enabled) return -1;
        ensureConnection();
        try (PreparedStatement ps = connection.prepareStatement("SELECT lives FROM player_lives WHERE uuid=?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("lives");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[LivesSMP] getLives() SQL error: " + e.getMessage());
        }
        return -1;
    }

    public void setLives(String uuid, int lives) {
        if (!enabled) return;
        ensureConnection();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO player_lives (uuid, lives) VALUES (?, ?) ON DUPLICATE KEY UPDATE lives=?"
        )) {
            ps.setString(1, uuid);
            ps.setInt(2, lives);
            ps.setInt(3, lives);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[LivesSMP] setLives() SQL error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing MySQL connection: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        ensureConnection();
        return connection;
    }

}
