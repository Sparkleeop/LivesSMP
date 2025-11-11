package me.sparklee.LivesSMP;

import me.sparklee.LivesSMP.commands.LivesCommand;
import me.sparklee.LivesSMP.commands.ReloadCommand;
import me.sparklee.LivesSMP.commands.ReviveCommand;
import me.sparklee.LivesSMP.events.DeathListener;
import me.sparklee.LivesSMP.events.JoinListener;
import me.sparklee.LivesSMP.events.CraftingListener;
import me.sparklee.LivesSMP.items.ReviveItem;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.data.DatabaseManager;
import me.sparklee.LivesSMP.data.PlayerManager;
import me.sparklee.LivesSMP.commands.MainCommand;
import me.sparklee.LivesSMP.utils.ConfigManager;
import me.sparklee.LivesSMP.commands.AddLivesCommand;
import me.sparklee.LivesSMP.commands.RemoveLivesCommand;
import me.sparklee.LivesSMP.commands.SetLivesCommand;
import me.sparklee.LivesSMP.events.PlayerKillListener;
import me.sparklee.LivesSMP.commands.TopLivesCommand;
import me.sparklee.LivesSMP.utils.UpdateChecker;

import org.bukkit.plugin.java.JavaPlugin;

public class LivesSMP extends JavaPlugin {

    private static LivesSMP instance;
    private PlayerManager playerManager;
    private ReviveItem reviveItem;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        String version = getDescription().getVersion();

        getLogger().info("=======================================");
        getLogger().info("     Enabling LivesSMP v" + version);
        getLogger().info("=======================================");

        // Initialize version-aware config manager
        ConfigManager configManager = new ConfigManager(this, "config.yml");
        configManager.load(); // Handles version check, backups, and updates

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        playerManager = new PlayerManager(this);
        reviveItem = new ReviveItem(this);
        MessageManager.load();

        // Check for updates on Spigot
        new me.sparklee.LivesSMP.utils.UpdateChecker(this, 130095).checkForUpdates();
        if (getConfig().getBoolean("check-for-updates", true)) {
            UpdateChecker checker = new UpdateChecker(this, 130095);
            getServer().getPluginManager().registerEvents(checker, this);
            checker.checkForUpdates();
        }


        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerKillListener(this), this);

        getCommand("revive").setExecutor(new ReviveCommand(this));
        getCommand("lives").setExecutor(new LivesCommand(this));
        getCommand("livessmpreload").setExecutor(new ReloadCommand(this));
        getCommand("livessmp").setExecutor(new MainCommand(this));
        getCommand("addlives").setExecutor(new AddLivesCommand(this));
        getCommand("removelives").setExecutor(new RemoveLivesCommand(this));
        getCommand("setlives").setExecutor(new SetLivesCommand(this));
        getCommand("toplives").setExecutor(new TopLivesCommand(this));

        // Start ActionBar life display
        if (getConfig().getBoolean("actionbar.enabled", true)) {
            int interval = getConfig().getInt("actionbar.interval-ticks", 60);
            getServer().getScheduler().runTaskTimerAsynchronously(this, new me.sparklee.LivesSMP.tasks.ActionBarTask(this), 0L, interval);
            getLogger().info("ActionBar life display enabled (interval: " + interval + " ticks)");
        }

        // Register PlaceholderAPI expansion if PAPI is installed
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new me.sparklee.LivesSMP.utils.LivesExpansion(this).register();
            getLogger().info("PlaceholderAPI detected - registered placeholders!");
        } else {
            getLogger().info("PlaceholderAPI not found - skipping placeholder registration.");
        }


        getLogger().info("LivesSMP v" + version + " has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        String version = getDescription().getVersion();

        playerManager.saveData();
        databaseManager.close();

        getLogger().info("=======================================");
        getLogger().info("     Disabling LivesSMP v" + version);
        getLogger().info("=======================================");
    }

    public static LivesSMP getInstance() {
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
