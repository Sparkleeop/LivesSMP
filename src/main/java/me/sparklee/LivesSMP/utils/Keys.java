package me.sparklee.LivesSMP.utils;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.NamespacedKey;

/**
 * Central place for the plugin's persistent data keys.
 */
public final class Keys {

    public static final String LIFE_SHARD = "life_shard";
    public static final String REVIVE_CRYSTAL = "revive_crystal";

    private Keys() {
    }

    public static NamespacedKey of(LivesSMP plugin, String key) {
        return new NamespacedKey(plugin, key);
    }
}
