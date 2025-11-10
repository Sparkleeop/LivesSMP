package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class TopLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public TopLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(" ");
        sender.sendMessage("§6§l[LivesSMP] §7Top Players by Lives:");
        sender.sendMessage("§7------------------------------------");

        if (plugin.getDatabaseManager().isEnabled()) {
            showMySQLLeaderboard(sender);
        } else {
            showYAMLLeaderboard(sender);
        }

        sender.sendMessage("§7------------------------------------");
        sender.sendMessage(" ");
        return true;
    }

    private void showMySQLLeaderboard(CommandSender sender) {
        try (PreparedStatement ps = plugin.getDatabaseManager().getConnection().prepareStatement(
                "SELECT uuid, lives FROM player_lives ORDER BY lives DESC LIMIT 10"
        )) {
            ResultSet rs = ps.executeQuery();

            int rank = 1;
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int lives = rs.getInt("lives");

                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                String name = player != null && player.getName() != null ? player.getName() : "Unknown";

                sender.sendMessage("§e#" + rank + " §f" + name + " §7— §c" + lives + " ♥");
                rank++;
            }

        } catch (Exception e) {
            sender.sendMessage(MessageManager.get("leaderboard-error", "&cFailed to load leaderboard! Check console."));
            plugin.getLogger().severe("[LivesSMP] Failed to fetch MySQL leaderboard: " + e.getMessage());
        }
    }

    private void showYAMLLeaderboard(CommandSender sender) {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            sender.sendMessage(MessageManager.get("leaderboard-empty", "&7No player data found yet!"));
            return;
        }

        YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        Map<String, Integer> livesMap = new HashMap<>();

        if (dataConfig.contains("data")) {
            for (String uuid : dataConfig.getConfigurationSection("data").getKeys(false)) {
                int lives = dataConfig.getInt("data." + uuid, 0);
                livesMap.put(uuid, lives);
            }
        }

        if (livesMap.isEmpty()) {
            sender.sendMessage(MessageManager.get("leaderboard-empty", "&7No player data found yet!"));
            return;
        }

        // Sort descending
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(livesMap.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted.subList(0, Math.min(10, sorted.size()))) {
            UUID uuid = UUID.fromString(entry.getKey());
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String name = player != null && player.getName() != null ? player.getName() : "Unknown";
            int lives = entry.getValue();

            sender.sendMessage("§e#" + rank + " §f" + name + " §7— §c" + lives + " ♥");
            rank++;
        }
    }
}
