package me.sparklee.threeLives.items;

import me.sparklee.threeLives.ThreeLivesSMP;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ReviveItem {
    private final ThreeLivesSMP plugin;
    private final ItemStack reviveCrystal;

    public ReviveItem(ThreeLivesSMP plugin) {
        this.plugin = plugin;

        reviveCrystal = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = reviveCrystal.getItemMeta();
        meta.setDisplayName("§dRevive Crystal");
        meta.setLore(java.util.List.of("§7Use this to revive a banned player."));

        // Tag item as revive crystal
        NamespacedKey key = new NamespacedKey(plugin, "revive_crystal");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        reviveCrystal.setItemMeta(meta);

        registerRecipe();
    }

    private void registerRecipe() {
        if (!plugin.getConfig().getBoolean("revive-crystal.enabled")) {
            plugin.getLogger().info("Revive Crystal crafting disabled via config.");
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, "revive_crystal_recipe");
        int amount = plugin.getConfig().getInt("revive-crystal.amount", 1);

        ItemStack result = reviveCrystal.clone();
        result.setAmount(amount);

        ShapedRecipe recipe = new ShapedRecipe(key, result);

        // Load shape
        var shape = plugin.getConfig().getStringList("revive-crystal.shape");
        if (shape.size() != 3) {
            plugin.getLogger().severe("Invalid revive-crystal shape in config! Must be 3 lines.");
            return;
        }
        recipe.shape(shape.get(0), shape.get(1), shape.get(2));

        // Load ingredients
        ConfigurationSection ingredients = plugin.getConfig().getConfigurationSection("revive-crystal.ingredients");
        if (ingredients == null) {
            plugin.getLogger().severe("No ingredients found for Revive Crystal in config!");
            return;
        }

        for (String keyChar : ingredients.getKeys(false)) {
            Material mat = Material.getMaterial(ingredients.getString(keyChar, ""));
            if (mat == null) {
                plugin.getLogger().severe("Invalid material in revive-crystal.ingredients: " + ingredients.getString(keyChar));
                continue;
            }

            recipe.setIngredient(keyChar.charAt(0), mat);
        }

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Loaded Revive Crystal recipe from config.");
    }

    public ItemStack getItem() {
        return reviveCrystal.clone();
    }

    public void reloadRecipe() {
        Bukkit.removeRecipe(new NamespacedKey(plugin, "revive_crystal_recipe"));
        registerRecipe();
    }


    public boolean isReviveCrystal(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, "revive_crystal");
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
