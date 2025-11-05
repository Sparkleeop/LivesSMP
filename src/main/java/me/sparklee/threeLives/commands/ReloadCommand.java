package me.sparklee.threeLives.commands;

import me.sparklee.threeLives.ThreeLivesSMP;
import me.sparklee.threeLives.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final ThreeLivesSMP plugin;

    public ReloadCommand(ThreeLivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("threelives.reload")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don't have permission to do that!"));
            return true;
        }

        plugin.reloadConfig();
        plugin.getReviveItem().reloadRecipe();
        MessageManager.load(); // reloads messages

        sender.sendMessage(MessageManager.get("reload-success", "&aConfiguration reloaded successfully!"));
        plugin.getLogger().info("Config and revive recipe reloaded by " + sender.getName());
        return true;
    }
}
