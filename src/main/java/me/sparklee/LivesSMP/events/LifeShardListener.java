package me.sparklee.LivesSMP.events;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.data.PlayerManager;
import me.sparklee.LivesSMP.utils.Keys;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class LifeShardListener implements Listener {

    private final LivesSMP plugin;

    public LifeShardListener(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isLifeShard(item)) return;

        Player player = event.getPlayer();
        PlayerManager playerManager = plugin.getPlayerManager();
        int currentLives = playerManager.getLives(player);

        if (!playerManager.isUnlimitedLives() && currentLives >= playerManager.getMaxLives()) {
            event.setCancelled(true);
            player.sendMessage(MessageManager.formatPlaceholders(
                    MessageManager.get("life-shard-max", "&eYou already have the maximum of &e%lives% &elives!"),
                    player.getName(), null, playerManager.getMaxLives()
            ));
            return;
        }

        event.setCancelled(true);

        int newLives = currentLives + 1;
        playerManager.setLives(player, newLives);

        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }

        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("life-shard-redeem", "&aYou redeemed a Life Shard and gained a life! You now have &e%lives%&a."),
                player.getName(), null, newLives
        ));
    }

    private boolean isLifeShard(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }

        Byte value = item.getItemMeta().getPersistentDataContainer()
                .get(Keys.of(plugin, Keys.LIFE_SHARD), PersistentDataType.BYTE);
        return value != null && value == 1;
    }
}
