package me.sparklee.threeLives.commands;

import me.sparklee.threeLives.ThreeLivesSMP;
import me.sparklee.threeLives.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {

    private final ThreeLivesSMP plugin;

    public MainCommand(ThreeLivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(" ");
        sender.sendMessage("§6§l3LivesSMP §7v" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Created by §eSparklee");
        sender.sendMessage(" ");
        sender.sendMessage("§e/3lives §7– Check your remaining lives");
        sender.sendMessage("§e/revive <player> §7– Revive a banned player using a Revive Crystal");
        sender.sendMessage("§e/threelivesreload §7– Reload the plugin configuration");
        sender.sendMessage(" ");
        sender.sendMessage("§7All commands also work via §e/threelives <subcommand>§7!");
        sender.sendMessage(" ");
        return true;
    }
}
