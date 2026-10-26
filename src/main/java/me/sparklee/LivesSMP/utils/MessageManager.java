package me.sparklee.LivesSMP.utils;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.configuration.file.FileConfiguration;

public final class MessageManager {

    private static FileConfiguration config;

    private MessageManager() {
    }

    public static void load() {
        config = LivesSMP.getInstance().getConfig();
    }

    /**
     * Reads {@code messages.<path>}, applies the prefix and color codes.
     */
    public static String get(String path, String fallback) {
        if (config == null) load();

        String value = config.getString("messages." + path, fallback);
        return Messages.format(value == null ? fallback : value);
    }

    public static String formatPlaceholders(String message, String player, String target, int lives) {
        return formatPlaceholders(message, player, target, lives, -1);
    }

    public static String formatPlaceholders(String message, String player, String target, int lives, int amount) {
        String result = message
                .replace("%player%", player == null ? "" : player)
                .replace("%target%", target == null ? "" : target)
                .replace("%lives%", String.valueOf(lives));

        if (amount >= 0) {
            result = result.replace("%amount%", String.valueOf(amount));
        }
        return result;
    }
}
