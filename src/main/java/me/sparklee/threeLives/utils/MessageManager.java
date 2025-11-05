package me.sparklee.threeLives.utils;

import me.sparklee.threeLives.ThreeLivesSMP;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageManager {

    private static FileConfiguration config;

    public static void load() {
        config = ThreeLivesSMP.getInstance().getConfig();
    }

    /**
     * Get a message by path from config.yml (under "messages.")
     * If missing, returns a default fallback.
     */
    public static String get(String path, String fallback) {
        if (config == null) load();
        String value = config.getString("messages." + path, fallback);
        return Messages.format(value);
    }

    /**
     * Replaces placeholders like %player%, %target%, %lives%, etc.
     */
    public static String formatPlaceholders(String message, String player, String target, int lives) {
        return message
                .replace("%player%", player == null ? "" : player)
                .replace("%target%", target == null ? "" : target)
                .replace("%lives%", String.valueOf(lives));
    }
}
