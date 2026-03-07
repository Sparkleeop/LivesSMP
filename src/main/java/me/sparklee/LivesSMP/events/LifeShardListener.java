package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class LifeShardListener implements Listener {

    private final LivesSMP plugin;

    public LifeShardListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey(plugin, "life_shard");
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();

        if (!data.has(key, PersistentDataType.INTEGER)) return;

        int currentLives = plugin.getPlayerManager().getLives(player);
        int newLives = currentLives + 1;

        plugin.getPlayerManager().setLives(player, newLives);
        item.setAmount(item.getAmount() - 1);

        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("life-shard-redeem", "&aYou redeemed a Life Shard and gained 1 life!"),
                player.getName(), null, 1
        ));
    }
}
