/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.rules;

import com.crimsonwarpedcraft.cwcommons.store.DataStore;
import com.crimsonwarpedcraft.cwcommons.store.bukkit.AutoFlushTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

/// Folia compatible flush task based on [AutoFlushTask]
@RequiredArgsConstructor
public class FoliaFlushTask {

    /**
     * Default flush interval: 5 minutes at 20 TPS.
     */

    private final DataStore store;
    private final Plugin plugin;

    public ScheduledTask start() {
        return plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin,_ -> store.flush(),1000L, 1000L);
    }

}
