package me.sparklee.LivesSMP.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.sparklee.LivesSMP.LivesSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Region-aware scheduler helper.
 * <p>
 * Paper and Folia both expose the region scheduler API. On regular Paper the tasks are
 * mapped to the main thread, while on Folia they run on the correct region / global /
 * entity thread. Using these methods means no {@code Bukkit.getScheduler()} calls remain,
 * which Folia does not support.
 */
public final class Scheduler {

    private static final long MILLIS_PER_TICK = 50L;

    private Scheduler() {
    }

    // ------------------------------------------------------------------
    // Async (database, HTTP, file I/O)
    // ------------------------------------------------------------------

    public static ScheduledTask async(Plugin plugin, Runnable task) {
        return Bukkit.getAsyncScheduler().runNow(plugin, scheduled -> task.run());
    }

    public static ScheduledTask asyncLater(Plugin plugin, Runnable task, long delayTicks) {
        return Bukkit.getAsyncScheduler().runDelayed(
                plugin,
                scheduled -> task.run(),
                Math.max(1L, delayTicks) * MILLIS_PER_TICK,
                TimeUnit.MILLISECONDS
        );
    }

    public static ScheduledTask asyncTimer(Plugin plugin, Runnable task, long initialTicks, long periodTicks) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                scheduled -> task.run(),
                Math.max(1L, initialTicks) * MILLIS_PER_TICK,
                Math.max(1L, periodTicks) * MILLIS_PER_TICK,
                TimeUnit.MILLISECONDS
        );
    }

    // ------------------------------------------------------------------
    // Global region (broadcasts, recipes, reloads, global timers)
    // ------------------------------------------------------------------

    public static void global(Runnable task) {
        Bukkit.getGlobalRegionScheduler().execute(LivesSMP.getInstance(), task);
    }

    public static ScheduledTask globalLater(Runnable task, long delayTicks) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(
                LivesSMP.getInstance(),
                scheduled -> task.run(),
                Math.max(1L, delayTicks)
        );
    }

    public static ScheduledTask globalTimer(Runnable task, long initialTicks, long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                LivesSMP.getInstance(),
                scheduled -> task.run(),
                Math.max(1L, initialTicks),
                Math.max(1L, periodTicks)
        );
    }

    // ------------------------------------------------------------------
    // Entity region (player-specific work)
    // ------------------------------------------------------------------

    /**
     * Runs on the entity's owning region thread. It is skipped automatically when the
     * entity has been removed (for example a player who logged out).
     */
    public static void entity(Entity entity, Runnable task) {
        entity.getScheduler().run(LivesSMP.getInstance(), scheduled -> task.run(), null);
    }

    public static void entityLater(Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().runDelayed(
                LivesSMP.getInstance(),
                scheduled -> task.run(),
                null,
                Math.max(1L, delayTicks)
        );
    }

    /**
     * Runs the task on a player's region thread, or globally for console senders.
     */
    public static void forSender(CommandSender sender, Runnable task) {
        if (sender instanceof Player player) {
            entity(player, task);
        } else {
            global(task);
        }
    }
}
