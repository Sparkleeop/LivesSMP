package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.data.PlayerManager;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public SetLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("livessmp.admin")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don't have permission to do that!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(MessageManager.get("usage-set", "&eUsage: /setlives <player> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getPlayerExact(args[0]);
        if (target == null) target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(MessageManager.get("player-never-joined", "&cThat player has never joined before!"));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageManager.get("invalid-amount", "&cPlease provide a valid number for amount!"));
            return true;
        }

        if (amount < 0) {
            sender.sendMessage(MessageManager.get("invalid-amount", "&cPlease provide a valid number for amount!"));
            return true;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        Integer stored = playerManager.getStoredLives(target.getUniqueId());
        if (stored == null) {
            sender.sendMessage(MessageManager.get("database-error", "&cCould not read player data. Please try again later."));
            return true;
        }

        int maxLives = playerManager.getMaxLives();

        if (!playerManager.isUnlimitedLives() && amount > maxLives) {
            amount = maxLives;
            sender.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("max-lives-limit", "&eYou cannot set lives above the maximum of &e%lives%!"),
                    sender.getName(), target.getName(), maxLives
            ));
        }

        playerManager.setLives(target.getUniqueId(), amount);

        String senderName = sender instanceof Player ? sender.getName() : "Console";
        String targetName = target.getName() == null ? "" : target.getName();

        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("set-success", "&eYou set &e%target%&e's lives to &e%lives%&e."),
                senderName, targetName, amount
        ));

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("set-receive", "&eYour lives were set to &e%lives% &eby &e%player%&e."),
                    senderName, targetName, amount
            ));
        }

        plugin.getLogger().info("[Admin] " + senderName + " set " + targetName + "'s lives to " + amount);
        return true;
    }
}
