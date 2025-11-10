package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Date;

public class DeathListener implements Listener {

    private final LivesSMP plugin;

    public DeathListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.hasPermission("livessmp.bypass")) return;

        int lives = plugin.getPlayerManager().decrementLife(player);

        if (lives <= 0) {
            String reason = "You’ve lost all your lives!";
            Date expires = null;

            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            banList.addBan(player.getName(), reason, expires, "LivesSMP");

            player.kickPlayer("§c☠ You’ve lost all 3 of your lives!\n§7You are now banned until someone revives you.");
        } else {
            player.sendMessage("§cYou lost a life! §7Lives remaining: §e" + lives);
        }
    }
}
