package me.sparklee.LivesSMP.utils;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class UpdateChecker implements Listener {

    private final LivesSMP plugin;
    private final int resourceId;
    private volatile String latestVersion;
    private volatile boolean updateAvailable = false;

    public UpdateChecker(LivesSMP plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkForUpdates() {
        Scheduler.async(plugin, () -> {
            try {
                URLConnection connection = URI.create(
                        "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId
                ).toURL().openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                String latest;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    latest = reader.readLine();
                }

                if (latest == null || latest.isBlank()) {
                    return;
                }
                latestVersion = latest.trim();

                String currentVersion = plugin.getPluginMeta().getVersion();

                if (isNewer(latestVersion, currentVersion)) {
                    updateAvailable = true;
                    plugin.getLogger().warning("========================================");
                    plugin.getLogger().warning("  A new version of LivesSMP is available!");
                    plugin.getLogger().warning("  Current: " + currentVersion + "  |  Latest: " + latestVersion);
                    plugin.getLogger().warning("  Download: https://www.spigotmc.org/resources/" + resourceId);
                    plugin.getLogger().warning("========================================");
                } else {
                    plugin.getLogger().info("LivesSMP is up to date! (v" + currentVersion + ")");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Could not check for updates: " + e.getMessage());
            }
        });
    }

    /**
     * Compares dotted version strings such as {@code 2.3.0} and {@code 2.2.2}.
     * Non-numeric suffixes are ignored, so {@code 2.3.0-SNAPSHOT} equals {@code 2.3.0}.
     */
    private static boolean isNewer(String latest, String current) {
        String[] latestParts = latest.split("[.\\-+]");
        String[] currentParts = current.split("[.\\-+]");
        int length = Math.max(latestParts.length, currentParts.length);

        for (int i = 0; i < length; i++) {
            int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
            if (latestPart != currentPart) {
                return latestPart > currentPart;
            }
        }
        return false;
    }

    private static int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!updateAvailable) return;

        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission("livessmp.admin")) {
            Scheduler.entityLater(player, () -> {
                player.sendMessage("§8§m--------------------------------------------------");
                player.sendMessage("§6§lLivesSMP §7– §eNew update available!");
                player.sendMessage("§7You are running §f" + plugin.getPluginMeta().getVersion()
                        + " §7but §a" + latestVersion + " §7is available.");
                player.sendMessage("§eDownload it here: §fhttps://www.spigotmc.org/resources/" + resourceId);
                player.sendMessage("§8§m--------------------------------------------------");
            }, 60L);
        }
    }
}
