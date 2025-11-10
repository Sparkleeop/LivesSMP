package me.sparklee.LivesSMP.utils;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;

public class UpdateChecker implements Listener {

    private final LivesSMP plugin;
    private final int resourceId;
    private String latestVersion;
    private boolean updateAvailable = false;

    public UpdateChecker(LivesSMP plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                latestVersion = reader.readLine().trim();
                reader.close();

                String currentVersion = plugin.getDescription().getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!updateAvailable) return;

        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission("livessmp.admin")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§8§m--------------------------------------------------");
                player.sendMessage("§6§lLivesSMP §7– §eNew update available!");
                player.sendMessage("§7You are running §f" + plugin.getDescription().getVersion() + " §7but §a" + latestVersion + " §7is available.");
                player.sendMessage("§eDownload it here: §fhttps://www.spigotmc.org/resources/" + resourceId);
                player.sendMessage("§8§m--------------------------------------------------");
            }, 60L); // wait 3 seconds after join
        }
    }
}
