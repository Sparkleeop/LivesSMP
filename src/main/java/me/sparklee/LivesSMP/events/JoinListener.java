package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class JoinListener implements Listener {

    private final LivesSMP plugin;

    public JoinListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int startingLives = plugin.getConfig().getInt("starting-lives", 3);

        if (!plugin.getDatabaseManager().isEnabled()) {
            // YAML path — synchronous, simple
            plugin.getPlayerManager().loadPlayer(player.getUniqueId());
            int lives = plugin.getPlayerManager().getLives(player);
            handleJoin(player, lives, startingLives);
            return;
        }

        // MySQL path — always read from DB on join, DB is source of truth
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int lives = plugin.getDatabaseManager().getLives(player.getUniqueId().toString());

            if (lives == -1) {
                // First time player — register in DB with default lives
                plugin.getDatabaseManager().setLives(player.getUniqueId().toString(), startingLives);
                lives = startingLives;
            }

            final int finalLives = lives;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                // Always trust DB value — load directly into cache
                plugin.getPlayerManager().getCacheDirectly().put(player.getUniqueId(), finalLives);
                handleJoin(player, finalLives, startingLives);
            });
        });
    }

    private void handleJoin(Player player, int lives, int startingLives) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        if (lives <= 0 && !banList.isBanned(player.getName())) {
            // Was unbanned externally — restore lives
            plugin.getPlayerManager().setLives(player, startingLives);
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("auto-revive", "&aYou were revived and restored to &e%lives% &alives!"),
                    player.getName(), null, startingLives
            ));
            plugin.getLogger().info("[LivesSMP] Auto-restored " + player.getName() + " to " + startingLives + " lives after unban.");
            return;
        }

        if (lives == startingLives && !plugin.getPlayerManager().hasData(player)) {
            // Brand new player
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("join-new", "&aWelcome to Lives SMP! You have &e%lives% &alives."),
                    player.getName(), null, startingLives
            ));
            return;
        }

        // Returning player
        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("join-return", "&7Welcome back! You currently have &e%lives% &7lives."),
                player.getName(), null, lives
        ));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int lives = plugin.getPlayerManager().getLives(uuid);

        if (plugin.getDatabaseManager().isEnabled()) {
            // Guaranteed save on quit — DB will always have correct value on next join
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDatabaseManager().setLives(uuid.toString(), lives);
                plugin.getPlayerManager().unload(uuid);
            });
        } else {
            plugin.getPlayerManager().unload(uuid);
        }
    }
}