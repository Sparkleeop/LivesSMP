package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public RemoveLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("lives.admin")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don’t have permission!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(MessageManager.get("usage-remove", "&eUsage: /removelives <player> <amount>"));
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

        int current = plugin.getPlayerManager().getLives(
                target.isOnline() ? target.getPlayer() : Bukkit.getPlayer(target.getUniqueId()));
        int newLives = Math.max(0, current - amount);
        plugin.getPlayerManager().setLives(target.getUniqueId(), newLives);

        String senderName = sender instanceof Player ? sender.getName() : "Console";

        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("remove-success", "&cRemoved &e%amount% &clives from &e%target%&c. (Now: &e%lives%&c)"),
                senderName, target.getName(), newLives).replace("%amount%", String.valueOf(amount))
        );

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("remove-receive", "&cYour lives were reduced by &e%player%&c! You now have &e%lives%&c lives."),
                    senderName, target.getName(), newLives)
            );
        }

        // Console Log
        plugin.getLogger().info("[Admin] " + senderName + " removed " + amount + " lives from " + target.getName() + " (now " + newLives + ")");

        return true;
    }
}
