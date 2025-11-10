package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public AddLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("livessmp.admin")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don’t have permission!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(MessageManager.get("usage-add", "&eUsage: /addlives <player> <amount>"));
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
        int newLives = current + amount;

        int maxLives = plugin.getPlayerManager().getMaxLives();
        boolean unlimited = plugin.getPlayerManager().isUnlimitedLives();

        if (!unlimited && newLives > maxLives) {
            newLives = maxLives;
            sender.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("max-lives-reached", "&e%target% &aalready has the maximum of &e%lives% &alives!"),
                    sender.getName(), target.getName(), maxLives)
            );
        }

        plugin.getPlayerManager().setLives(target.getUniqueId(), newLives);

        String senderName = sender instanceof Player ? sender.getName() : "Console";

        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("add-success", "&aYou added &e%amount% &alives to &e%target%&a. (Now: &e%lives%&a)"),
                senderName, target.getName(), newLives).replace("%amount%", String.valueOf(amount))
        );

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("add-receive", "&aYour lives were increased by &e%player%&a! You now have &e%lives%&a lives."),
                    senderName, target.getName(), newLives)
            );
        }

        plugin.getLogger().info("[Admin] " + senderName + " added " + amount + " lives to " + target.getName() + " (now " + newLives + ")");

        return true;
    }
}
