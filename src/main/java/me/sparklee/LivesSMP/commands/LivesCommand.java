package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LivesCommand implements CommandExecutor {

    private final LivesSMP plugin;

    public LivesCommand(LivesSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageManager.get("only-player", "&cOnly players can use this command!"));
            return true;
        }

        int lives = plugin.getPlayerManager().getLives(player);
        String display;

        // Color-coded hearts display
        switch (lives) {
            case 3 -> display = "§a❤❤❤";
            case 2 -> display = "§e❤❤";
            case 1 -> display = "§c❤";
            default -> display = "§7☠";
        }

        String msg = MessageManager.formatPlaceholders(
                MessageManager.get("lives-display", "&7You currently have &e%lives% &7lives remaining."),
                player.getName(), null, lives
        );

        player.sendMessage(" ");
        player.sendMessage(msg);
        player.sendMessage(" ");

        return true;
    }
}
