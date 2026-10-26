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

public class AddLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public AddLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("livessmp.admin")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don't have permission to do that!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(MessageManager.get("usage-add", "&eUsage: /addlives <player> <amount>"));
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

        if (amount <= 0) {
            sender.sendMessage(MessageManager.get("invalid-amount", "&cPlease provide a valid number for amount!"));
            return true;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        Integer stored = playerManager.getStoredLives(target.getUniqueId());
        if (stored == null) {
            sender.sendMessage(MessageManager.get("database-error", "&cCould not read player data. Please try again later."));
            return true;
        }

        int current = stored;
        int newLives = playerManager.addLives(target.getUniqueId(), amount);
        int added = newLives - current;

        String senderName = sender instanceof Player ? sender.getName() : "Console";
        String targetName = target.getName() == null ? "" : target.getName();

        if (added <= 0) {
            sender.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("max-lives-reached", "&e%target% &aalready has the maximum of &e%lives% &alives!"),
                    senderName, targetName, playerManager.getMaxLives()
            ));
            return true;
        }

        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("add-success", "&aYou added &e%amount% &alives to &e%target%&a. (Now: &e%lives%&a)"),
                senderName, targetName, newLives, added
        ));

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("add-receive", "&aYour lives were increased by &e%player%&a! You now have &e%lives%&a lives."),
                    senderName, targetName, newLives, added
            ));
        }

        plugin.getLogger().info("[Admin] " + senderName + " added " + added + " lives to "
                + targetName + " (now " + newLives + ")");
        return true;
    }
}
