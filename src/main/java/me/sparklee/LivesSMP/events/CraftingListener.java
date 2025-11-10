package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    private final LivesSMP plugin;

    public CraftingListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (plugin.getReviveItem().isReviveCrystal(item)) {
                // Cancel the recipe output
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
