package me.sparklee.LivesSMP.utils;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {

    // Matches &#RRGGBB or <#RRGGBB>
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)(<#[0-9A-F]{6}>|&#[0-9A-F]{6})");

    /**
     * Converts all hex colors in the message (&#RRGGBB or <#RRGGBB>) to the Bukkit §x§R§R§G§G§B§B format.
     */
    private static String applyHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String color = matcher.group()
                    .replace("<", "")
                    .replace(">", "")
                    .replace("&", "")
                    .replace("#", "");

            // Build §x§R§R§G§G§B§B format manually
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : color.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Fetches prefix from config.yml
     */
    public static String prefix() {
        String rawPrefix = LivesSMP.getInstance().getConfig().getString("prefix", "&6[LivesSMP] &f");
        return format(rawPrefix, false);
    }

    /**
     * Formats a message with prefix and color translation.
     */
    public static String format(String message) {
        return format(message, true);
    }

    private static String format(String message, boolean includePrefix) {
        // 1. Apply custom hex codes
        String processed = applyHexColors(message);

        // 2. Translate normal & codes
        processed = ChatColor.translateAlternateColorCodes('&', processed);

        // 3. Optionally add prefix
        return includePrefix ? prefix() + processed : processed;
    }
}
