package me.sparklee.threeLives.data;

import me.sparklee.threeLives.ThreeLivesSMP;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private final ThreeLivesSMP plugin;
    private Connection connection;
    private boolean enabled = false;

    public DatabaseManager(ThreeLivesSMP plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        if (!plugin.getConfig().getBoolean("mysql.enabled")) {
            plugin.getLogger().info("MySQL disabled — using YAML storage.");
            return;
        }

        String host = plugin.getConfig().getString("mysql.host");
        int port = plugin.getConfig().getInt("mysql.port");
        String database = plugin.getConfig().getString("mysql.database");
        String username = plugin.getConfig().getString("mysql.username");
        String password = plugin.getConfig().getString("mysql.password");

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    username, password
            );
            plugin.getLogger().info("Connected to MySQL successfully!");
            enabled = true;
            setupTable();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to MySQL: " + e.getMessage());
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

    public boolean isEnabled() {
        return enabled;
    }

    public int getLives(String uuid) {
        if (!enabled) return -1;
        try (PreparedStatement ps = connection.prepareStatement("SELECT lives FROM player_lives WHERE uuid=?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("lives");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[3LivesSMP] getLives() SQL error: " + e.getMessage());
        }
        return -1;
    }

    public void setLives(String uuid, int lives) {
        if (!enabled) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO player_lives (uuid, lives) VALUES (?, ?) ON DUPLICATE KEY UPDATE lives=?"
        )) {
            ps.setString(1, uuid);
            ps.setInt(2, lives);
            ps.setInt(3, lives);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[3LivesSMP] setLives() SQL error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing MySQL connection: " + e.getMessage());
        }
    }
}
