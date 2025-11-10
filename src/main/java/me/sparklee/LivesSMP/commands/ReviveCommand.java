package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReviveCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public ReviveCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageManager.get("only-player", "&cOnly players can use this command!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageManager.get("revive-usage", "&cUsage: /revive <player>"));
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!held.hasItemMeta() || held.getItemMeta().getDisplayName() == null ||
                !held.getItemMeta().getDisplayName().equals("§dRevive Crystal")) {
            player.sendMessage(MessageManager.get("revive-invalid-item", "&cYou must hold a Revive Crystal to use this!"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(MessageManager.get("revive-invalid-player", "&cThat player has never joined the server!"));
            return true;
        }

        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(target.getName());
        plugin.getPlayerManager().setLives(target.getUniqueId(), 1);
        held.setAmount(held.getAmount() - 1);

        // Broadcast
        Bukkit.broadcastMessage(MessageManager.formatPlaceholders(
                MessageManager.get("revive-broadcast", "&#FF9F68⚡ &e%player% &7has revived &b%target% &7using a Revive Crystal!"),
                player.getName(), target.getName(), 0
        ));

        // Sender message
        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("revive-success", "&aYou revived &e%target%&a!"),
                player.getName(), target.getName(), 0
        ));

        // Target message
        if (target.isOnline()) {
            target.getPlayer().sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("revive-target", "&aYou’ve been revived by &e%player%&a!"),
                    player.getName(), target.getName(), 0
            ));
        }

        return true;
    }
}
