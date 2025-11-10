package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
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

        if (!sender.hasPermission("threelives.admin")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don’t have permission!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(MessageManager.get("usage-set", "&eUsage: /setlives <player> <amount>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(MessageManager.get("player-never-joined", "&cThat player has never joined before!"));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageManager.get("invalid-amount", "&cAmount must be a number!"));
            return true;
        }

        plugin.getPlayerManager().setLives(target.getUniqueId(), amount);

        String senderName = sender instanceof Player ? sender.getName() : "Console";

        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("set-success", "&eYou set &e%target%&e's lives to &e%lives%&e."),
                senderName, target.getName(), amount)
        );

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("set-receive", "&eYour lives were set to &e%lives% &eby &e%player%&e."),
                    senderName, target.getName(), amount)
            );
        }

        // Console Log
        plugin.getLogger().info("[Admin] " + senderName + " set " + target.getName() + "'s lives to " + amount);

        return true;
    }
}
