package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final LivesSMP plugin;

    public JoinListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        int startingLives = plugin.getConfig().getInt("starting-lives", 3);
        int lives = plugin.getPlayerManager().getLives(player);

        // If the player has no data yet
        if (!plugin.getPlayerManager().hasData(player)) {
            plugin.getPlayerManager().setLives(player, startingLives);

            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("join-new", "&aWelcome to Lives SMP! You have &e%lives% &alives."),
                    player.getName(), null, startingLives
            ));
            return;
        }

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

        // Normal join message
        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("join-return", "&7Welcome back! You currently have &e%lives% &7lives."),
                player.getName(), null, lives
        ));
    }
}
