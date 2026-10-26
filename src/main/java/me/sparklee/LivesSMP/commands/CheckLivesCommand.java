package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CheckLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public CheckLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("livessmp.check")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don't have permission to do that!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageManager.get("usage-check", "&eUsage: /checklives <player>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getPlayerExact(args[0]);
        if (target == null) target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(MessageManager.get("player-never-joined", "&cThat player has never joined before!"));
            return true;
        }

        String targetName = target.getName() == null ? "" : target.getName();
        Integer stored = plugin.getPlayerManager().getStoredLives(target.getUniqueId());
        if (stored == null) {
            sender.sendMessage(MessageManager.get("database-error", "&cCould not read player data. Please try again later."));
            return true;
        }
        int lives = stored;

        sender.sendMessage(" ");
        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("check-lives", "&6%target% &fcurrently has: %hearts%"),
                sender.getName(), targetName, lives
        ).replace("%hearts%", Messages.hearts(lives)));
        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("check-lives-remaining", "&7Lives remaining: &e%lives%"),
                sender.getName(), targetName, lives
        ));
        sender.sendMessage(" ");
        return true;
    }
}
