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
            plugin.getPlayerManager().loadPlayer(player.getUniqueId());
            int lives = plugin.getPlayerManager().getLives(player);
            handleJoin(player, lives, startingLives);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int lives = plugin.getDatabaseManager().getLives(player.getUniqueId().toString());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                if (lives == -1) {
                    plugin.getPlayerManager().setLives(player, startingLives);
                    player.sendMessage(MessageManager.formatPlaceholders(
                            MessageManager.get("join-new", "&aWelcome to Lives SMP! You have &e%lives% &alives."),
                            player.getName(), null, startingLives
                    ));
                } else {
                    plugin.getPlayerManager().setLives(player.getUniqueId(), lives);
                    handleJoin(player, lives, startingLives);
                }
            });
        });
    }

    private void handleJoin(Player player, int lives, int startingLives) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        if (lives <= 0 && !banList.isBanned(player.getName())) {
            plugin.getPlayerManager().setLives(player, startingLives);
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("auto-revive", "&aYou were revived and restored to &e%lives% &alives!"),
                    player.getName(), null, startingLives
            ));
            plugin.getLogger().info("[LivesSMP] Auto-restored " + player.getName() + " to " + startingLives + " lives after unban.");
            return;
        }

        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("join-return", "&7Welcome back! You currently have &e%lives% &7lives."),
                player.getName(), null, lives
        ));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().unload(event.getPlayer().getUniqueId());
    }
}