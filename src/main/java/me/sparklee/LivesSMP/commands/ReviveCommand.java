package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.data.PlayerManager;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Scheduler;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ReviveCommand implements CommandExecutor {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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

        if (!plugin.getReviveItem().isReviveCrystal(player.getInventory().getItemInMainHand())) {
            player.sendMessage(MessageManager.get("revive-invalid-item", "&cYou must hold a Revive Crystal to use this!"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(MessageManager.get("revive-invalid-player", "&cThat player has never joined the server!"));
            return true;
        }

        if (plugin.getDatabaseManager().isConfigured()) {
            String targetUuid = target.getUniqueId().toString();
            Scheduler.async(plugin, () -> {
                Integer stored = plugin.getDatabaseManager().getLives(targetUuid);

                Scheduler.entity(player, () -> {
                    if (!player.isOnline()) return;

                    if (stored == null) {
                        player.sendMessage(MessageManager.get("revive-error", "&cCould not read that player's data. Please try again."));
                        return;
                    }

                    finishRevive(player, target, stored < 0 ? 0 : stored);
                });
            });
            return true;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        finishRevive(player, target, playerManager.getLivesLoaded(target.getUniqueId()));
        return true;
    }

    private void finishRevive(Player player, OfflinePlayer target, int lives) {
        String targetName = target.getName() == null ? "" : target.getName();

        if (lives > 0) {
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("revive-not-zero", "&eThat player still has lives left and cannot be revived!"),
                    player.getName(), targetName, lives
            ));
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!plugin.getReviveItem().isReviveCrystal(held)) {
            player.sendMessage(MessageManager.get("revive-invalid-item", "&cYou must hold a Revive Crystal to use this!"));
            return;
        }

        // Consume exactly one crystal.
        if (held.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            held.setAmount(held.getAmount() - 1);
        }

        plugin.getPlayerManager().setLives(target.getUniqueId(), 1);

        var profile = target.getPlayerProfile();
        Component broadcast = LEGACY.deserialize(MessageManager.formatPlaceholders(
                MessageManager.get("revive-broadcast", "&#FF9F68⚡ &e%player% &7has revived &b%target% &7using a Revive Crystal!"),
                player.getName(), targetName, 1
        ));

        // The ban list is shared global state; pardon + broadcast on the global region thread.
        Scheduler.global(() -> {
            ProfileBanList banList = Bukkit.getBanList(BanListType.PROFILE);
            if (banList.isBanned(profile)) {
                banList.pardon(profile);
            }
            Bukkit.broadcast(broadcast);
        });

        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("revive-success", "&aYou revived &e%target%&a!"),
                player.getName(), targetName, 1
        ));

        if (target.isOnline() && target.getPlayer() != null) {
            Player onlineTarget = target.getPlayer();
            String message = MessageManager.formatPlaceholders(
                    MessageManager.get("revive-target", "&aYou've been revived by &e%player%&a!"),
                    player.getName(), targetName, 1
            );
            Scheduler.entity(onlineTarget, () -> onlineTarget.sendMessage(message));
        }

        plugin.getLogger().info("[Revive] " + player.getName() + " revived " + targetName + ".");
    }
}
