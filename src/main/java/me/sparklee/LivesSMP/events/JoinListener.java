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
        int lives = plugin.getPlayerManager().getLives(player);

        if (!plugin.getPlayerManager().hasData(player)) {
            plugin.getPlayerManager().setLives(player, 3);
            player.sendMessage(MessageManager.get("join-new", "&aWelcome to Lives SMP! You have &e3 lives&a."));
        } else {
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("join-return", "&7Welcome back! You currently have &e%lives% &7lives."),
                    player.getName(), null, lives
            ));
        }
    }
}
