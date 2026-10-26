package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Scheduler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public ReloadCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("livessmp.admin")) {
            sender.sendMessage(MessageManager.get("no-permission", "&cYou don't have permission to do that!"));
            return true;
        }

        // Config + recipe changes touch global state, so run them on the global region thread.
        Scheduler.global(() -> {
            if (plugin.getConfigManager() != null) {
                plugin.getConfigManager().load();
            } else {
                plugin.reloadConfig();
            }

            MessageManager.load();
            plugin.getReviveItem().reloadRecipe();
            plugin.startActionBarTask();

            sender.sendMessage(MessageManager.get("reload-success", "&aConfiguration reloaded successfully!"));
            plugin.getLogger().info("Config and revive recipe reloaded by " + sender.getName());
        });
        return true;
    }
}
