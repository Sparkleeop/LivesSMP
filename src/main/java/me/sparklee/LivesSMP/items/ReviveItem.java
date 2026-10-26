package me.sparklee.LivesSMP.items;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.Keys;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReviveItem {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final LivesSMP plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey recipeKey;
    private final ItemStack reviveCrystal;

    public ReviveItem(LivesSMP plugin) {
        this.plugin = plugin;
        this.itemKey = Keys.of(plugin, Keys.REVIVE_CRYSTAL);
        this.recipeKey = new NamespacedKey(plugin, "revive_crystal_recipe");
        this.reviveCrystal = createCrystal();
        registerRecipe();
    }

    private ItemStack createCrystal() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LEGACY.deserialize("§d§lRevive Crystal"));
            meta.lore(List.of(LEGACY.deserialize("§7Use this to revive a banned player.")));
            meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void registerRecipe() {
        if (!plugin.getConfig().getBoolean("revive-crystal.enabled", true)) {
            plugin.getLogger().info("Revive Crystal crafting disabled via config.");
            return;
        }

        List<String> shape = plugin.getConfig().getStringList("revive-crystal.shape");
        if (shape.size() != 3) {
            plugin.getLogger().severe("[LivesSMP] Invalid revive-crystal shape! Must have exactly 3 lines.");
            return;
        }

        Set<Character> usedSymbols = new HashSet<>();
        for (String line : shape) {
            if (line == null || line.length() != 3) {
                plugin.getLogger().severe("[LivesSMP] Each revive-crystal shape line must be exactly 3 characters long!");
                return;
            }
            for (char symbol : line.toCharArray()) {
                if (symbol != ' ') {
                    usedSymbols.add(symbol);
                }
            }
        }

        if (usedSymbols.isEmpty()) {
            plugin.getLogger().severe("[LivesSMP] The revive-crystal shape cannot be empty!");
            return;
        }

        ConfigurationSection ingredients = plugin.getConfig().getConfigurationSection("revive-crystal.ingredients");
        if (ingredients == null) {
            plugin.getLogger().severe("[LivesSMP] revive-crystal.ingredients missing in config!");
            return;
        }

        Map<Character, Material> resolved = new HashMap<>();
        for (String symbol : ingredients.getKeys(false)) {
            if (symbol.length() != 1) {
                plugin.getLogger().warning("[LivesSMP] Ingredient key '" + symbol + "' must be a single character — skipping.");
                continue;
            }

            char key = symbol.charAt(0);
            if (!usedSymbols.contains(key)) {
                plugin.getLogger().warning("[LivesSMP] Ingredient '" + symbol + "' is not used in the shape — skipping.");
                continue;
            }

            String matName = ingredients.getString(symbol);
            Material material = matName == null ? null : Material.matchMaterial(matName);
            if (material == null || !material.isItem()) {
                plugin.getLogger().severe("[LivesSMP] Invalid material for '" + symbol + "': " + matName);
                continue;
            }

            resolved.put(key, material);
        }

        if (!resolved.keySet().containsAll(usedSymbols)) {
            plugin.getLogger().severe("[LivesSMP] Not every symbol in the revive-crystal shape has a valid ingredient — recipe not registered.");
            return;
        }

        int amount = Math.max(1, Math.min(plugin.getConfig().getInt("revive-crystal.amount", 1), 64));
        ItemStack result = reviveCrystal.clone();
        result.setAmount(amount);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe.shape(shape.get(0), shape.get(1), shape.get(2));
        resolved.forEach(recipe::setIngredient);

        try {
            if (!Bukkit.addRecipe(recipe)) {
                plugin.getLogger().warning("[LivesSMP] Revive Crystal recipe was not registered (it may already exist).");
                return;
            }
            plugin.getLogger().info("Revive Crystal recipe registered successfully.");
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("[LivesSMP] Failed to register Revive Crystal recipe: " + e.getMessage());
        }
    }

    public void reloadRecipe() {
        unregisterRecipe();
        registerRecipe();
    }

    public void unregisterRecipe() {
        Bukkit.removeRecipe(recipeKey);
    }

    public ItemStack getItem() {
        return reviveCrystal.clone();
    }

    public boolean isReviveCrystal(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || item.getItemMeta() == null) {
            return false;
        }

        Byte value = item.getItemMeta().getPersistentDataContainer()
                .get(itemKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }
}
