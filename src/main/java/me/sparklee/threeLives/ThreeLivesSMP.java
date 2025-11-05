package me.sparklee.threeLives;

import me.sparklee.threeLives.commands.*;
import me.sparklee.threeLives.events.*;
import me.sparklee.threeLives.data.*;
import me.sparklee.threeLives.items.*;
import org.bukkit.plugin.java.JavaPlugin;

public class ThreeLivesSMP extends JavaPlugin {

    private static ThreeLivesSMP instance;
    private PlayerManager playerManager;
    private ReviveItem reviveItem;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        playerManager = new PlayerManager(this);
        reviveItem = new ReviveItem(this);

        // Event registration
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);

        // Command registration
        getCommand("revive").setExecutor(new ReviveCommand(this));
        getCommand("threelives").setExecutor(new LivesCommand(this));
        getCommand("threelives-recipe").setExecutor(new ReviveRecipeCommand(this));
        getCommand("threelives-reload").setExecutor(new ReloadCommand(this));

        getLogger().info("✅ 3LivesSMP enabled successfully!");
    }

    @Override
    public void onDisable() {
        playerManager.saveData();
        databaseManager.close();
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
