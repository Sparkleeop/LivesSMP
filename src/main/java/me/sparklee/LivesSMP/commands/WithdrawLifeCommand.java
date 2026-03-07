package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Messages;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WithdrawLifeCommand implements CommandExecutor {

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
            player.sendMessage(MessageManager.get("invalid-amount", "&cAmount must be a number!"));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(MessageManager.get("invalid-amount", "&cAmount must be a positive number!"));
            return true;
        }

        int currentLives = plugin.getPlayerManager().getLives(player);
        int minLives = plugin.getConfig().getInt("life-withdraw.min-lives", 1);

        if (currentLives - amount < minLives) {
            player.sendMessage(MessageManager.get("withdraw-too-low", "&cYou cannot withdraw that many lives!"));
            return true;
        }

        // Deduct lives
        plugin.getPlayerManager().setLives(player, currentLives - amount);

        // Create item
        String matName = plugin.getConfig().getString("life-withdraw.item.material", "HEART_OF_THE_SEA");
        Material material = Material.matchMaterial(matName);
        if (material == null) material = Material.HEART_OF_THE_SEA;

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        String name = Messages.format(plugin.getConfig().getString("life-withdraw.item.name", "&c❤ Life Shard"));
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("life-withdraw.item.lore")) {
            lore.add(Messages.format(line));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);

        // Handle full inventory — refund lives if no space
        if (player.getInventory().firstEmpty() == -1) {
            plugin.getPlayerManager().setLives(player, currentLives);
            player.sendMessage(MessageManager.get("withdraw-inventory-full", "&cYour inventory is full! Lives were not withdrawn."));
            return true;
        }

        player.getInventory().addItem(item);
        player.sendMessage(MessageManager.formatPlaceholders(
                MessageManager.get("withdraw-success", "&aYou withdrew &e%lives% &alives into Life Shards!"),
                player.getName(), null, amount
        ));
        return true;
    }
}