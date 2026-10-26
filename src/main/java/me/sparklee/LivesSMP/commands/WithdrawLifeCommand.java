package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.data.PlayerManager;
import me.sparklee.LivesSMP.utils.Keys;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WithdrawLifeCommand implements CommandExecutor {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    // 36 inventory slots * 64 items — a hard upper bound for a single withdrawal.
    private static final int MAX_WITHDRAW = 2304;

    private final LivesSMP plugin;

    public WithdrawLifeCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageManager.get("only-player", "&cOnly players can use this command!"));
            return true;
        }

        if (!player.hasPermission("livessmp.withdraw")) {
            player.sendMessage(MessageManager.get("no-permission", "&cYou don't have permission to do that!"));
            return true;
        }

        if (!plugin.getConfig().getBoolean("life-withdraw.enabled", true)) {
            player.sendMessage(MessageManager.get("withdraw-disabled", "&cLife withdrawing is disabled."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageManager.get("withdraw-usage", "&eUsage: /withdrawlife <amount>"));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageManager.get("invalid-amount", "&cPlease provide a valid number for amount!"));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(MessageManager.get("invalid-amount", "&cPlease provide a valid number for amount!"));
            return true;
        }

        if (amount > MAX_WITHDRAW) {
            player.sendMessage(MessageManager.get("withdraw-too-many", "&cYou cannot withdraw that many lives at once!"));
            return true;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        int currentLives = playerManager.getLives(player);
        int minLives = Math.max(0, plugin.getConfig().getInt("life-withdraw.min-lives", 1));

        if (currentLives - amount < minLives) {
            player.sendMessage(MessageManager.get("withdraw-too-low", "&cYou cannot withdraw that many lives!"));
            return true;
        }

        Material material = resolveMaterial();
        List<ItemStack> shards = createShards(material, amount);

        // Add first, then deduct only what actually fit. This avoids refunding lives
        // when the inventory is full or only partially free.
        Map<Integer, ItemStack> leftover = player.getInventory()
                .addItem(shards.toArray(new ItemStack[0]));
        int leftoverAmount = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
        int added = amount - leftoverAmount;

        if (added <= 0) {
            player.sendMessage(MessageManager.get("withdraw-inventory-full", "&cYour inventory is full! Lives were not withdrawn."));
            return true;
        }

        playerManager.setLives(player, currentLives - added);
        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("withdraw-success", "&aYou withdrew &e%lives% &alives into Life Shards!"),
                player.getName(), null, added
        ));
        return true;
    }

    private Material resolveMaterial() {
        String matName = plugin.getConfig().getString("life-withdraw.item.material", "HEART_OF_THE_SEA");
        Material material = matName == null ? null : Material.matchMaterial(matName);
        if (material == null || !material.isItem()) {
            return Material.HEART_OF_THE_SEA;
        }
        return material;
    }

    private List<ItemStack> createShards(Material material, int amount) {
        String rawName = plugin.getConfig().getString("life-withdraw.item.name", "&#FF4C4C❤ Life Shard");
        String name = Messages.color(rawName);

        List<Component> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("life-withdraw.item.lore")) {
            lore.add(LEGACY.deserialize(Messages.color(line)));
        }

        int maxStack = Math.max(1, material.getMaxStackSize());
        List<ItemStack> stacks = new ArrayList<>();
        int remaining = amount;

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStack);
            ItemStack item = new ItemStack(material, stackSize);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                if (name != null && !name.isBlank()) {
                    meta.displayName(LEGACY.deserialize(name));
                }
                if (!lore.isEmpty()) {
                    meta.lore(lore);
                }
                meta.getPersistentDataContainer().set(
                        Keys.of(plugin, Keys.LIFE_SHARD), PersistentDataType.BYTE, (byte) 1
                );
                item.setItemMeta(meta);
            }

            stacks.add(item);
            remaining -= stackSize;
        }

        return stacks;
    }
}
