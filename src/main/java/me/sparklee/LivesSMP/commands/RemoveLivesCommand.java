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

public class RemoveLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public RemoveLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("livessmp.admin")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don't have permission to do that!"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(MessageManager.get("usage-remove", "&eUsage: /removelives <player> <amount>"));
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
        int newLives = playerManager.removeLives(target.getUniqueId(), amount);
        int removed = current - newLives;

        String senderName = sender instanceof Player ? sender.getName() : "Console";
        String targetName = target.getName() == null ? "" : target.getName();

        sender.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("remove-success", "&cYou removed &e%amount% &clives from &e%target%&c. (Now: &e%lives%&c)"),
                senderName, targetName, newLives, removed
        ));

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("remove-receive", "&cYour lives were reduced by &e%player%&c! You now have &e%lives%&c lives."),
                    senderName, targetName, newLives, removed
            ));
        }

        plugin.getLogger().info("[Admin] " + senderName + " removed " + removed + " lives from "
                + targetName + " (now " + newLives + ")");
        return true;
    }
}
