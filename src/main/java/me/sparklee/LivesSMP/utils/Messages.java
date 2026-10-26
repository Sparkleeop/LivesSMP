package me.sparklee.LivesSMP.utils;

import me.sparklee.LivesSMP.LivesSMP;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Messages {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)(<#[0-9A-F]{6}>|&#[0-9A-F]{6})");
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("(?i)&([0-9A-FK-OR])");
    private static final Pattern SECTION_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");

    private Messages() {
    }

    /**
     * Translates {@code &} color codes and {@code &#RRGGBB} / {@code <#RRGGBB>} hex colors.
     * Does not add the plugin prefix.
     */
    public static String color(String message) {
        if (message == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group()
                    .replace("<", "")
                    .replace(">", "")
                    .replace("&", "")
                    .replace("#", "");

            // Build §x§R§R§G§G§B§B format manually
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(buffer);

        Matcher legacy = AMPERSAND_PATTERN.matcher(buffer.toString());
        StringBuffer translated = new StringBuffer();
        while (legacy.find()) {
            legacy.appendReplacement(translated,
                    Matcher.quoteReplacement("§" + legacy.group(1).toLowerCase()));
        }
        legacy.appendTail(translated);

        return translated.toString();
    }

    /**
     * Removes legacy section-sign color codes from a message.
     */
    public static String strip(String message) {
        return SECTION_PATTERN.matcher(color(message)).replaceAll("");
    }

    public static String prefix() {
        LivesSMP plugin = LivesSMP.getInstance();
        String rawPrefix = plugin == null
                ? "&6[LivesSMP] &f"
                : plugin.getConfig().getString("prefix", "&6[LivesSMP] &f");
        return color(rawPrefix);
    }

    /**
     * Formats a message with the configured prefix.
     */
    public static String format(String message) {
        return prefix() + color(message);
    }

    /**
     * Returns a small colored heart display for the given amount of lives.
     */
    public static String hearts(int lives) {
        if (lives <= 0) return "§7☠";
        if (lives == 1) return "§c❤";
        if (lives == 2) return "§e❤❤";
        if (lives == 3) return "§a❤❤❤";
        return "§a❤ x" + lives;
    }
}
