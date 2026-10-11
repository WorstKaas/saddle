/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import com.crimsonwarpedcraft.cwcommons.store.DataStore;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/// Folia-compatible flush task for automatically persisting data store changes
/// Uses a dedicated single-thread executor to avoid blocking the repository's async operations
@RequiredArgsConstructor
public class FoliaFlushTask {

    /// The data store to flush
    private final DataStore store;
    /// The plugin instance for scheduling
    private final Plugin plugin;
    /// Dedicated executor for flush operations to prevent deadlocks with repository operations
    private ScheduledExecutorService flushExecutor;

    /// Starts the scheduled flush task
    /// @return the ScheduledTask that can be cancelled if needed
    public ScheduledTask start() {
        flushExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "saddle-portal-flush");
            t.setDaemon(true);
            return t;
        });
        flushExecutor.scheduleAtFixedRate(() -> {
            try {
                store.flush();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to flush data store: " + e.getMessage());
            }
        }, 10, 10, TimeUnit.SECONDS);
        // Return a dummy task for compatibility - actual scheduling is done by flushExecutor
        return plugin.getServer().getAsyncScheduler()
                .runDelayed(plugin, _ -> {}, 999999, TimeUnit.DAYS);
    }

    /// Shuts down the flush executor
    public void stop() {
        if (flushExecutor != null) {
            flushExecutor.shutdown();
        }
    }
}
