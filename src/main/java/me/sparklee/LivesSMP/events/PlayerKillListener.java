package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerKillListener implements Listener {

    private final LivesSMP plugin;

    public PlayerKillListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;

        Player victim = event.getEntity();

        // Bypass check
        if (killer.hasPermission("livessmp.bypass")) return;
        if (victim.hasPermission("livessmp.bypass")) return;

        // Config toggle
        if (!plugin.getConfig().getBoolean("life-steal.enabled", true)) return;
        int amount = plugin.getConfig().getInt("life-steal.amount", 1);

        int victimLives = plugin.getPlayerManager().getLives(victim);
        int killerLives = plugin.getPlayerManager().getLives(killer);

        // Victim loses life
        victimLives = Math.max(0, victimLives - amount);
        plugin.getPlayerManager().setLives(victim, victimLives);

        // Killer gains life
        killerLives += amount;
        plugin.getPlayerManager().setLives(killer, killerLives);

        // Messages
        killer.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("life-steal-gain", "&aYou stole &e%amount% &alife(s) from &e%target%! &7(You now have &e%lives%&7)"),
                killer.getName(), victim.getName(), killerLives).replace("%amount%", String.valueOf(amount))
        );

        victim.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("life-steal-loss", "&cYou lost &e%amount% &clife(s) to &e%player%! &7(You now have &e%lives%&7)"),
                killer.getName(), victim.getName(), victimLives).replace("%amount%", String.valueOf(amount))
        );

        // Optional broadcast
        if (plugin.getConfig().getBoolean("life-steal.broadcast", true)) {
            Bukkit.broadcastMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("life-steal-broadcast", "&#FF9F68⚔ &e%player% &7stole &c%amount% &7life(s) from &e%target%!"),
                    killer.getName(), victim.getName(), 0
            ).replace("%amount%", String.valueOf(amount)));
        }
    }
}
