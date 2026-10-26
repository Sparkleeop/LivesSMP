package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TopLivesCommand implements CommandExecutor {

    private static final int LIMIT = 10;

    private final LivesSMP plugin;

    public TopLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!plugin.getDatabaseManager().isConfigured()) {
            showLeaderboard(sender, plugin.getPlayerManager().getTopLives(LIMIT));
            return true;
        }

        Scheduler.async(plugin, () -> {
            List<Map.Entry<UUID, Integer>> top = plugin.getPlayerManager().getTopLives(LIMIT);
            Scheduler.forSender(sender, () -> showLeaderboard(sender, top));
        });
        return true;
    }

    private void showLeaderboard(CommandSender sender, List<Map.Entry<UUID, Integer>> top) {
        if (top == null) {
            sender.sendMessage(MessageManager.get("leaderboard-error", "&cFailed to load the leaderboard! Check the console."));
            return;
        }

        sender.sendMessage(" ");
        sender.sendMessage("§6§lTop Players by Lives:");
        sender.sendMessage("§7------------------------------------");

        if (top.isEmpty()) {
            sender.sendMessage(MessageManager.get("leaderboard-empty", "&7No player data found yet!"));
            sender.sendMessage("§7------------------------------------");
            sender.sendMessage(" ");
            return;
        }

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : top) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            String name = player.getName();
            if (name == null) {
                name = entry.getKey().toString().substring(0, 8);
            }

            sender.sendMessage("§e#" + rank + " §f" + name + " §7— §c" + entry.getValue() + " ♥");
            rank++;
        }

        sender.sendMessage("§7------------------------------------");
        sender.sendMessage(" ");
    }
}
