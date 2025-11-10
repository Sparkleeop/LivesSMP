package me.sparklee.threeLives;

import me.sparklee.threeLives.commands.LivesCommand;
import me.sparklee.threeLives.commands.ReloadCommand;
import me.sparklee.threeLives.commands.ReviveCommand;
import me.sparklee.threeLives.events.DeathListener;
import me.sparklee.threeLives.events.JoinListener;
import me.sparklee.threeLives.events.CraftingListener;
import me.sparklee.threeLives.items.ReviveItem;
import me.sparklee.threeLives.utils.MessageManager;
import me.sparklee.threeLives.data.DatabaseManager;
import me.sparklee.threeLives.data.PlayerManager;
import me.sparklee.threeLives.commands.MainCommand;
import me.sparklee.threeLives.utils.ConfigManager;

import org.bukkit.plugin.java.JavaPlugin;

public class ThreeLivesSMP extends JavaPlugin {

    private static ThreeLivesSMP instance;
    private PlayerManager playerManager;
    private ReviveItem reviveItem;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        String version = getDescription().getVersion();

        getLogger().info("=======================================");
        getLogger().info("     Enabling 3LivesSMP v" + version);
        getLogger().info("=======================================");

        // Initialize version-aware config manager
        ConfigManager configManager = new ConfigManager(this, "config.yml");
        configManager.load(); // Handles version check, backups, and updates

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        playerManager = new PlayerManager(this);
        reviveItem = new ReviveItem(this);
        MessageManager.load();

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);

        getCommand("revive").setExecutor(new ReviveCommand(this));
        getCommand("lives").setExecutor(new LivesCommand(this));
        getCommand("threelivesreload").setExecutor(new ReloadCommand(this));
        getCommand("threelives").setExecutor(new MainCommand(this));

        getLogger().info("3LivesSMP v" + version + " has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        String version = getDescription().getVersion();

        playerManager.saveData();
        databaseManager.close();

        getLogger().info("=======================================");
        getLogger().info("     Disabling 3LivesSMP v" + version);
        getLogger().info("=======================================");
    }

    public static ThreeLivesSMP getInstance() {
        return instance;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ReviveItem getReviveItem() {
        return reviveItem;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
