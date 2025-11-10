package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CheckLivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public CheckLivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Permission check (optional)
        if (!sender.hasPermission("livessmp.check")) {
            sender.sendMessage("§cYou don’t have permission to do that!");
            return true;
        }

        // Usage: /checklives <player>
        if (args.length != 1) {
            sender.sendMessage("§eUsage: /checklives <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage("§cThat player has never joined before!");
            return true;
        }

        int lives;
        if (target.isOnline() && target.getPlayer() != null) {
            lives = plugin.getPlayerManager().getLives(target.getPlayer());
        } else {
            lives = plugin.getPlayerManager().getLives(Bukkit.getPlayer(target.getUniqueId()));
        }

        String display;
        switch (lives) {
            case 3 -> display = "§a❤❤❤";
            case 2 -> display = "§e❤❤";
            case 1 -> display = "§c❤";
            default -> display = "§7☠";
        }

        sender.sendMessage(" ");
        sender.sendMessage("§6§l[LivesSMP] §f" + target.getName() + " currently has: " + display);
        sender.sendMessage("§7Lives remaining: §e" + lives);
        sender.sendMessage(" ");

        return true;
    }
}
