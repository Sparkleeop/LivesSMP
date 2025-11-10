package me.sparklee.LivesSMP.items;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ReviveItem {

    private final LivesSMP plugin;
    private final ItemStack reviveCrystal;

    public ReviveItem(LivesSMP plugin) {
        this.plugin = plugin;

        // Create the base item
        reviveCrystal = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = reviveCrystal.getItemMeta();
        meta.setDisplayName("§dRevive Crystal");
        meta.setLore(java.util.List.of("§7Use this to revive a banned player."));

        // Add NBT tag
        NamespacedKey key = new NamespacedKey(plugin, "revive_crystal");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        reviveCrystal.setItemMeta(meta);

        // Register recipe safely
        registerRecipe();
    }

    /**
     * Safely registers the custom revive crystal recipe from config.yml
     */
    private void registerRecipe() {
        if (!plugin.getConfig().getBoolean("revive-crystal.enabled")) {
            plugin.getLogger().info("Revive Crystal crafting disabled via config.");
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, "revive_crystal_recipe");
        int amount = plugin.getConfig().getInt("revive-crystal.amount", 1);

        ItemStack result = reviveCrystal.clone();
        result.setAmount(amount);

        var shape = plugin.getConfig().getStringList("revive-crystal.shape");

        // Validate shape
        if (shape == null || shape.size() != 3) {
            plugin.getLogger().severe("[LivesSMP] Invalid revive-crystal shape! Must have exactly 3 lines.");
            return;
        }

        for (String line : shape) {
            if (line == null || line.length() != 3) {
                plugin.getLogger().severe("[LivesSMP] Each revive-crystal shape line must be exactly 3 characters long!");
                return;
            }
        }

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape.get(0), shape.get(1), shape.get(2));

        // Validate ingredients
        ConfigurationSection ingredients = plugin.getConfig().getConfigurationSection("revive-crystal.ingredients");
        if (ingredients == null) {
            plugin.getLogger().severe("[LivesSMP] revive-crystal.ingredients missing in config!");
            return;
        }

        boolean validIngredientFound = false;
        for (String symbol : ingredients.getKeys(false)) {
            String matName = ingredients.getString(symbol);
            if (matName == null) {
                plugin.getLogger().warning("[LivesSMP] Ingredient symbol '" + symbol + "' has no material defined!");
                continue;
            }

            Material mat = Material.matchMaterial(matName);
            if (mat == null) {
                plugin.getLogger().severe("[LivesSMP] Invalid material for '" + symbol + "': " + matName);
                continue;
            }

            validIngredientFound = true;
            recipe.setIngredient(symbol.charAt(0), mat);
        }

        if (!validIngredientFound) {
            plugin.getLogger().severe("[LivesSMP] No valid materials found in revive-crystal.ingredients — recipe not registered.");
            return;
        }

        try {
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info(" Revive Crystal recipe registered successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("[LivesSMP] Failed to register Revive Crystal recipe: " + e.getMessage());
        }
    }

    /**
     * Removes the current recipe and reloads a new one.
     */
    public void reloadRecipe() {
        try {
            Bukkit.removeRecipe(new NamespacedKey(plugin, "revive_crystal_recipe"));
        } catch (Exception ignored) {}
        registerRecipe();
    }

    public ItemStack getItem() {
        return reviveCrystal.clone();
    }

    public boolean isReviveCrystal(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, "revive_crystal");
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
