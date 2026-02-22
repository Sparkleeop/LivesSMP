package me.sparklee.LivesSMP.utils;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageManager {

    private static FileConfiguration config;

    public static void load() {
        config = LivesSMP.getInstance().getConfig();
    }

    public static String get(String path, String fallback) {
        if (config == null) load();
        String value = config.getString("messages." + path, fallback);
        return Messages.format(value);
    }

    public static String formatPlaceholders(String message, String player, String target, int lives) {
        return message
                .replace("%player%", player == null ? "" : player)
                .replace("%target%", target == null ? "" : target)
                .replace("%lives%", String.valueOf(lives));
    }
}
