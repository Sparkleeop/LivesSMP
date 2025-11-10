package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
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

        // Configurable starting lives (defaults to 3 if missing)
        int startingLives = plugin.getConfig().getInt("starting-lives", 3);

        // If the player has no data yet
        if (!plugin.getPlayerManager().hasData(player)) {
            plugin.getPlayerManager().setLives(player, startingLives);

            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("join-new", "&aWelcome to Lives SMP! You have &e%lives% &alives."),
                    player.getName(), null, startingLives
            ));
        } else {
            int lives = plugin.getPlayerManager().getLives(player);
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("join-return", "&7Welcome back! You currently have &e%lives% &7lives."),
                    player.getName(), null, lives
            ));
        }
    }
}
