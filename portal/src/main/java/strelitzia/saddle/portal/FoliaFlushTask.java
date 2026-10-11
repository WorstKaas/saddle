/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import com.crimsonwarpedcraft.cwcommons.store.DataStore;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/// Folia-compatible flush task for automatically persisting data store changes
/// Runs on the global region scheduler to flush data every second
@RequiredArgsConstructor
public class FoliaFlushTask {

    /// The data store to flush
    private final DataStore store;
    /// The plugin instance for scheduling
    private final Plugin plugin;

    /// Starts the scheduled flush task
    /// @return the ScheduledTask that can be cancelled if needed
    public ScheduledTask start() {
        return plugin.getServer().getAsyncScheduler()
                .runAtFixedRate(plugin,_ -> store.flush(),10, 10, TimeUnit.SECONDS);
    }
}
