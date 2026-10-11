/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.rules;

import com.crimsonwarpedcraft.cwcommons.store.DataStore;
import com.crimsonwarpedcraft.cwcommons.store.bukkit.AutoFlushTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/// Folia compatible flush task based on [AutoFlushTask]
@RequiredArgsConstructor
public class FoliaFlushTask {

    /**
     * Default flush interval: 5 minutes at 20 TPS.
     */

    private final DataStore store;
    private final Plugin plugin;

    public ScheduledTask start() {
        return plugin.getServer().getAsyncScheduler()
                .runAtFixedRate(plugin,_ -> store.flush(),10, 10, TimeUnit.SECONDS);
    }

}
