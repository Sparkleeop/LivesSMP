package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathListener implements Listener {

    private final LivesSMP plugin;

    public DeathListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Skip if player has bypass permission
        if (player.hasPermission("livessmp.bypass")) return;

        int lives = plugin.getPlayerManager().decrementLife(player);

        // Player lost all lives
        if (lives <= 0) {
            boolean tempBanEnabled = plugin.getConfig().getBoolean("temporary-ban.enabled", false);
            String durationStr = plugin.getConfig().getString("temporary-ban.duration", "1h");
            Date expires = tempBanEnabled ? parseDuration(durationStr) : null;

            String banReason = MessageManager.get("no-lives-left", "&c☠ You’ve lost all your lives!");
            String kickMessage;

            if (tempBanEnabled) {
                kickMessage = MessageManager.get("ban-temp-message",
                        "&c☠ You’ve lost all your lives!\n&7You are banned for a limited time.");
            } else {
                kickMessage = MessageManager.get("ban-permanent-message",
                        "&c☠ You’ve lost all 3 of your lives!\n&7You are now banned until someone revives you.");
            }

            // Apply ban
            BanList banList = Bukkit.getBanList(BanList.Type.NAME);
            banList.addBan(player.getName(), banReason, expires, "LivesSMP");

            // Kick player
            player.kickPlayer(kickMessage);

            plugin.getLogger().info(player.getName() + " was "
                    + (tempBanEnabled ? "temporarily" : "permanently")
                    + " banned for losing all lives.");
        } else {
            // Player still has lives remaining
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("life-lost", "&cYou lost a life! &7Lives remaining: &e%lives%"),
                    player.getName(), null, lives
            ));
        }
    }

    /**
     * Parses duration strings like "30m", "2h", "1d" into a Date.
     */
    private Date parseDuration(String input) {
        Pattern pattern = Pattern.compile("(\\d+)([mhd])", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches()) {
            plugin.getLogger().warning("[LivesSMP] Invalid temporary-ban duration format: " + input);
            return null;
        }

        int value = Integer.parseInt(matcher.group(1));
        char unit = matcher.group(2).toLowerCase().charAt(0);

        Calendar calendar = Calendar.getInstance();
        switch (unit) {
            case 'm' -> calendar.add(Calendar.MINUTE, value);
            case 'h' -> calendar.add(Calendar.HOUR, value);
            case 'd' -> calendar.add(Calendar.DAY_OF_MONTH, value);
        }

        return calendar.getTime();
    }
}
