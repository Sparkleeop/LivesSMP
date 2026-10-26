package me.sparklee.LivesSMP;

import me.sparklee.LivesSMP.commands.AddLivesCommand;
import me.sparklee.LivesSMP.commands.CheckLivesCommand;
import me.sparklee.LivesSMP.commands.LivesCommand;
import me.sparklee.LivesSMP.commands.MainCommand;
import me.sparklee.LivesSMP.commands.ReloadCommand;
import me.sparklee.LivesSMP.commands.RemoveLivesCommand;
import me.sparklee.LivesSMP.commands.ReviveCommand;
import me.sparklee.LivesSMP.commands.SetLivesCommand;
import me.sparklee.LivesSMP.commands.TopLivesCommand;
import me.sparklee.LivesSMP.commands.WithdrawLifeCommand;
import me.sparklee.LivesSMP.data.DatabaseManager;
import me.sparklee.LivesSMP.data.PlayerManager;
import me.sparklee.LivesSMP.events.CraftingListener;
import me.sparklee.LivesSMP.events.DeathListener;
import me.sparklee.LivesSMP.events.JoinListener;
import me.sparklee.LivesSMP.events.LifeShardListener;
import me.sparklee.LivesSMP.items.ReviveItem;
import me.sparklee.LivesSMP.tasks.ActionBarTask;
import me.sparklee.LivesSMP.utils.ConfigManager;
import me.sparklee.LivesSMP.utils.LivesExpansion;
import me.sparklee.LivesSMP.utils.MessageManager;
import me.sparklee.LivesSMP.utils.Scheduler;
import me.sparklee.LivesSMP.utils.UpdateChecker;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class LivesSMP extends JavaPlugin {

    private static LivesSMP instance;

    private ConfigManager configManager;
    private PlayerManager playerManager;
    private ReviveItem reviveItem;
    private DatabaseManager databaseManager;
    private LivesExpansion livesExpansion;

    private ScheduledTask actionBarTask;
    private final Map<String, CommandExecutor> executors = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        String version = getPluginMeta().getVersion();

        getLogger().info("=======================================");
        getLogger().info("     Enabling LivesSMP v" + version);
        getLogger().info("=======================================");

        configManager = new ConfigManager(this, "config.yml");
        configManager.load();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        playerManager = new PlayerManager(this);
        reviveItem = new ReviveItem(this);
        MessageManager.load();

        // Check for updates on Spigot
        if (getConfig().getBoolean("check-for-updates", true)) {
            UpdateChecker checker = new UpdateChecker(this, 130095);
            getServer().getPluginManager().registerEvents(checker, this);
            checker.checkForUpdates();
        }

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new LifeShardListener(this), this);

        registerCommand("revive", new ReviveCommand(this));
        registerCommand("lives", new LivesCommand(this));
        registerCommand("livessmpreload", new ReloadCommand(this));
        registerCommand("livessmp", new MainCommand(this));
        registerCommand("addlives", new AddLivesCommand(this));
        registerCommand("removelives", new RemoveLivesCommand(this));
        registerCommand("setlives", new SetLivesCommand(this));
        registerCommand("toplives", new TopLivesCommand(this));
        registerCommand("withdrawlife", new WithdrawLifeCommand(this));
        registerCommand("checklives", new CheckLivesCommand(this));

        // Convenient aliases for /livessmp <subcommand>
        executors.put("reload", executors.get("livessmpreload"));
        executors.put("withdraw", executors.get("withdrawlife"));
        executors.put("check", executors.get("checklives"));

        startActionBarTask();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            livesExpansion = new LivesExpansion(this);
            livesExpansion.register();
            getLogger().info("PlaceholderAPI detected - registered placeholders!");
        } else {
            getLogger().info("PlaceholderAPI not found - skipping placeholder registration.");
        }

        getLogger().info("LivesSMP v" + version + " has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        String version = getPluginMeta().getVersion();

        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }

        if (reviveItem != null) {
            reviveItem.unregisterRecipe();
        }

        if (playerManager != null) {
            playerManager.saveData();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        if (livesExpansion != null) {
            try {
                livesExpansion.unregister();
            } catch (Exception ignored) {
                // PlaceholderAPI may already be disabled.
            }
            livesExpansion = null;
        }

        getLogger().info("=======================================");
        getLogger().info("     Disabling LivesSMP v" + version);
        getLogger().info("=======================================");

        instance = null;
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is missing from plugin.yml!");
            return;
        }
        command.setExecutor(executor);
        executors.put(name, executor);
    }

    public void startActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }

        if (!getConfig().getBoolean("actionbar.enabled", true)) {
            return;
        }

        int interval = Math.max(1, getConfig().getInt("actionbar.interval-ticks", 40));
        ActionBarTask task = new ActionBarTask(this);
        actionBarTask = Scheduler.globalTimer(task, 20L, interval);
        getLogger().info("ActionBar life display enabled (interval: " + interval + " ticks)");
    }

    public static LivesSMP getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
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

    public CommandExecutor getExecutor(String name) {
        return executors.get(name);
    }
}
