package me.sparklee.LivesSMP.commands;

import me.sparklee.LivesSMP.LivesSMP;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Messages;
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

        String message = MessageManager.formatPlaceholders(
                MessageManager.get("lives-display", "&7You currently have &e%lives% &7lives remaining. %hearts%"),
                player.getName(), null, lives
        ).replace("%hearts%", Messages.hearts(lives));

        player.sendMessage(" ");
        player.sendMessage(message);
        player.sendMessage(" ");
        return true;
    }
}
