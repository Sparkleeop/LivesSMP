package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public MainCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(" ");
        sender.sendMessage("§6§lLivesSMP §7v" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Created by §eSparklee");
        sender.sendMessage(" ");
        sender.sendMessage("§6§lCommands:");
        sender.sendMessage("§7----------------------------------");
        sender.sendMessage("§e/lives §7– Check your remaining lives");
        sender.sendMessage("§e/revive <player> §7– Revive a banned player using a Revive Crystal");
        sender.sendMessage("§e/addlives <player> <amount> §7– Add lives to a player");
        sender.sendMessage("§e/removelives <player> <amount> §7– Remove lives from a player");
        sender.sendMessage("§e/setlives <player> <amount> §7– Set a player's lives");
        sender.sendMessage("§e/checklives <player> §7– Check another player’s lives");
        sender.sendMessage("§e/toplives §7– View the leaderboard of top players");
        sender.sendMessage("§e/livessmpreload §7– Reload the plugin configuration");
        sender.sendMessage(" ");
        sender.sendMessage("§7All commands also work via §e/livessmp <subcommand>§7!");
        sender.sendMessage("§7----------------------------------");
        sender.sendMessage(" ");
        return true;
    }
}
