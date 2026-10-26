package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.data.PlayerManager;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Messages;
import me.sparklee.LivesSMP.utils.Scheduler;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathListener implements Listener {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([mhd])", Pattern.CASE_INSENSITIVE);
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final LivesSMP plugin;

    public DeathListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        if (victim.hasPermission("livessmp.bypass")) return;

        PlayerManager playerManager = plugin.getPlayerManager();
        int livesBefore = playerManager.getLives(victim);
        int livesAfter = playerManager.removeLives(victim.getUniqueId(), 1);

        // Life steal is handled here so it cannot double-decrement the victim
        // through a second PlayerDeathEvent listener.
        int stolen = stealLife(victim, victim.getKiller(), livesBefore);

        if (livesAfter <= 0) {
            ban(victim);
            return;
        }

        if (stolen > 0) {
            victim.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("life-steal-loss", "&cYou lost &e%amount% &clife(s) to &e%player%! &7(You now have &e%lives%&7)"),
                    victim.getKiller() == null ? "" : victim.getKiller().getName(),
                    victim.getName(), livesAfter, stolen
            ));
        } else {
            victim.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("life-lost", "&cYou lost a life! &7Lives remaining: &e%lives%"),
                    victim.getName(), null, livesAfter
            ));
        }
    }

    /**
     * Transfers lives to the killer without taking a second life from the victim.
     *
     * @return the number of lives actually transferred
     */
    private int stealLife(Player victim, Player killer, int victimLivesBefore) {
        if (killer == null || killer.equals(victim)) return 0;
        if (killer.hasPermission("livessmp.bypass")) return 0;
        if (!plugin.getConfig().getBoolean("life-steal.enabled", true)) return 0;
        if (victimLivesBefore <= 0) return 0;

        int amount = Math.max(1, plugin.getConfig().getInt("life-steal.amount", 1));

        PlayerManager playerManager = plugin.getPlayerManager();
        int killerBefore = playerManager.getLives(killer);
        int killerAfter = playerManager.addLives(killer.getUniqueId(), amount);
        int gained = killerAfter - killerBefore;

        if (gained <= 0) return 0;

        killer.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("life-steal-gain", "&aYou stole &e%amount% &alife(s) from &e%target%! &7(You now have &e%lives%&7)"),
                killer.getName(), victim.getName(), killerAfter, gained
        ));

        if (plugin.getConfig().getBoolean("life-steal.broadcast", true)) {
            Component broadcast = LEGACY.deserialize(MessageManager.formatPlaceholders(
                    MessageManager.get("life-steal-broadcast", "&#FF9F68⚔ &e%player% &7stole &c%amount% &7life(s) from &e%target%!"),
                    killer.getName(), victim.getName(), killerAfter, gained
            ));
            Scheduler.global(() -> Bukkit.broadcast(broadcast));
        }

        return gained;
    }

    private void ban(Player player) {
        boolean temporary = plugin.getConfig().getBoolean("temporary-ban.enabled", false);
        Date expires = null;

        if (temporary) {
            String duration = plugin.getConfig().getString("temporary-ban.duration", "1h");
            expires = parseDuration(duration);
            if (expires == null) {
                plugin.getLogger().warning("[LivesSMP] Invalid temporary-ban duration '" + duration + "'. Using 1h instead.");
                expires = parseDuration("1h");
            }
        }

        Date finalExpires = expires;
        String banReason = Messages.strip(
                MessageManager.get("no-lives-left", "&c☠ You've lost all of your lives!")
        );
        String kickMessage = temporary
                ? MessageManager.get("ban-temp-message", "&c☠ You've lost all your lives!\n&7You are banned for a limited time.")
                : MessageManager.get("ban-permanent-message", "&c☠ You've lost all your lives!\n&7You are now banned until someone revives you.");

        var profile = player.getPlayerProfile();
        Component kickComponent = LEGACY.deserialize(kickMessage);
        String playerName = player.getName();

        // The ban list is shared global state, so mutate it on the global region thread.
        Scheduler.global(() -> {
            ProfileBanList banList = Bukkit.getBanList(BanListType.PROFILE);
            if (!banList.isBanned(profile)) {
                banList.addBan(profile, banReason, finalExpires, "LivesSMP");
            }

            // The player is banned already; run the kick on the entity's own region thread.
            Scheduler.entityLater(player, () -> {
                if (player.isOnline()) {
                    player.kick(kickComponent);
                }
            }, 1L);

            plugin.getLogger().info(playerName + " was "
                    + (temporary ? "temporarily" : "permanently")
                    + " banned for losing all lives.");
        });
    }

    /**
     * Parses duration strings like "30m", "2h", "1d" into a Date.
     */
    private Date parseDuration(String input) {
        if (input == null) return null;

        Matcher matcher = DURATION_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            return null;
        }

        int value;
        try {
            value = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
        if (value <= 0) return null;

        char unit = matcher.group(2).toLowerCase().charAt(0);
        Calendar calendar = Calendar.getInstance();
        switch (unit) {
            case 'm' -> calendar.add(Calendar.MINUTE, value);
            case 'h' -> calendar.add(Calendar.HOUR, value);
            case 'd' -> calendar.add(Calendar.DAY_OF_MONTH, value);
            default -> {
                return null;
            }
        }

        return calendar.getTime();
    }
}
