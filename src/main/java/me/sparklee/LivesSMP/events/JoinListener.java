package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Scheduler;
import io.papermc.paper.ban.BanListType;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {

    private final LivesSMP plugin;

    public JoinListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int startingLives = plugin.getPlayerManager().getDefaultLives();

        // Already cached (for example after a quick rejoin) — the cache is the freshest value.
        if (plugin.getPlayerManager().hasData(uuid)) {
            handleJoin(player, plugin.getPlayerManager().getLives(uuid), startingLives, true);
            return;
        }

        if (!plugin.getDatabaseManager().isConfigured()) {
            // YAML path — synchronous, simple
            boolean existed = plugin.getPlayerManager().loadPlayer(uuid);
            handleJoin(player, plugin.getPlayerManager().getLives(uuid), startingLives, existed);
            return;
        }

        // MySQL path — read asynchronously, then apply on the player's region thread
        Scheduler.async(plugin, () -> {
            Integer stored = plugin.getDatabaseManager().getLives(uuid.toString());

            Scheduler.entity(player, () -> {
                if (!player.isOnline()) return;

                int lives;
                boolean existed;

                if (stored == null) {
                    // Database error — do not overwrite anything, just use a safe value.
                    plugin.getLogger().warning("[LivesSMP] Could not load lives for " + player.getName()
                            + " — using the default value for this session.");
                    lives = plugin.getPlayerManager().getLives(uuid);
                    existed = true;
                } else if (stored < 0) {
                    // First time player — create the row.
                    lives = startingLives;
                    existed = false;
                    plugin.getPlayerManager().setLives(uuid, lives);
                } else {
                    lives = stored;
                    existed = true;
                    plugin.getPlayerManager().cacheLives(uuid, lives);
                }

                handleJoin(player, lives, startingLives, existed);
            });
        });
    }

    private void handleJoin(Player player, int lives, int startingLives, boolean existed) {
        if (lives <= 0) {
            var profile = player.getPlayerProfile();

            // Read the shared ban list on the global region thread.
            Scheduler.global(() -> {
                ProfileBanList banList = Bukkit.getBanList(BanListType.PROFILE);
                if (banList.isBanned(profile)) {
                    // A banned player should not be able to join; let the ban system handle it.
                    return;
                }

                // Unbanned externally — restore the player's starting lives.
                plugin.getPlayerManager().setLives(player, startingLives);
                String message = MessageManager.formatPlaceholders(
                        MessageManager.get("auto-revive", "&aYou were revived and restored to &e%lives% &alives!"),
                        player.getName(), null, startingLives
                );
                Scheduler.entity(player, () -> player.sendMessage(message));
                plugin.getLogger().info("[LivesSMP] Auto-restored " + player.getName() + " to " + startingLives + " lives after unban.");
            });
            return;
        }

        if (!existed) {
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("join-new", "&aWelcome to Lives SMP! You have &e%lives% &alives."),
                    player.getName(), null, lives
            ));
            return;
        }

        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("join-return", "&7Welcome back! You currently have &e%lives% &7lives."),
                player.getName(), null, lives
        ));
    }
}
