package me.sparklee.threeLives.commands;

import me.sparklee.threeLives.ThreeLivesSMP;
import me.sparklee.threeLives.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ReviveRecipeCommand implements CommandExecutor {

    private final ThreeLivesSMP plugin;

    public ReviveRecipeCommand(ThreeLivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageManager.get("only-player", "&cOnly players can view recipes!"));
            return true;
        }

        if (!plugin.getConfig().getBoolean("revive-crystal.enabled")) {
            player.sendMessage(MessageManager.get("revive-recipe-disabled", "&cThe Revive Crystal recipe is currently disabled."));
            return true;
        }

        Inventory inv = Bukkit.createInventory(null, 9, "§dRevive Crystal Recipe");

        List<String> shape = plugin.getConfig().getStringList("revive-crystal.shape");
        for (int row = 0; row < 3; row++) {
            String line = shape.get(row);
            for (int col = 0; col < 3; col++) {
                char c = line.charAt(col);
                String matName = plugin.getConfig().getString("revive-crystal.ingredients." + c);
                if (matName == null) continue;

                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    inv.setItem(row * 3 + col, new ItemStack(mat));
                }
            }
        }

        ItemStack result = plugin.getReviveItem().getItem();
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName("§dRevive Crystal");
        result.setItemMeta(meta);
        inv.setItem(8, result);

        player.openInventory(inv);
        return true;
    }
}
