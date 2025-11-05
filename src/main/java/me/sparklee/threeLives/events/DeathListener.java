package me.sparklee.threeLives.events;

import me.sparklee.threeLives.ThreeLivesSMP;
import me.sparklee.threeLives.utils.MessageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Date;

public class DeathListener implements Listener {

    private final ThreeLivesSMP plugin;

    public DeathListener(ThreeLivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        int lives = plugin.getPlayerManager().decrementLife(player);

        if (lives <= 0) {
            String reason = MessageManager.get("no-lives-left", "&c☠ You’ve lost all your lives!");
            Date expires = null;

            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            banList.addBan(player.getName(), reason, expires, "3LivesSMP");

            player.kickPlayer(MessageManager.get("no-lives-left", "&c☠ You’ve lost all your lives!\n&7You are now banned until someone revives you."));
            plugin.getLogger().info(player.getName() + " was banned (lost all lives).");
        } else {
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("life-lost", "&cYou lost a life! &7Lives remaining: &e%lives%"),
                    player.getName(), null, lives
            ));
        }
    }
}
