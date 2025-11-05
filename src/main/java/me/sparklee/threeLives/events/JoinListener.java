package me.sparklee.threeLives.events;

import me.sparklee.threeLives.ThreeLivesSMP;
import me.sparklee.threeLives.utils.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final ThreeLivesSMP plugin;

    public JoinListener(ThreeLivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int lives = plugin.getPlayerManager().getLives(player);

        if (!plugin.getPlayerManager().hasData(player)) {
            plugin.getPlayerManager().setLives(player, 3);
            player.sendMessage(MessageManager.get("join-new", "&aWelcome to 3 Lives SMP! You’ve been given &e3 lives&a."));
        } else {
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("join-return", "&7Welcome back! You currently have &e%lives% &7lives."),
                    player.getName(), null, lives
            ));
        }
    }
}
