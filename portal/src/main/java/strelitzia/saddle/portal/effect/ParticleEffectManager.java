/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import com.crimsonwarpedcraft.cwcommons.store.Repository;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import strelitzia.saddle.portal.Portal;

import java.util.Map;

/// Manages the display of particle effects for all portals
/// Folia-compatible scheduler that runs on region schedulers for optimal performance
@RequiredArgsConstructor
public class ParticleEffectManager {

    /// The plugin instance for scheduling
    private final Plugin plugin;
    /// Repository containing all portals
    private final Repository<String, Portal> portalRepository;

    /// Starts the particle effect display task
    /// @return the ScheduledTask that can be cancelled if needed
    public ScheduledTask start() {
        return plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, _ -> displayEffects(), 1L, 5L);
    }

    /// Displays particle effects for all portals
    /// Runs asynchronously and schedules particle display on appropriate region schedulers
    private void displayEffects() {
        portalRepository.getAll().thenAccept(portals -> {
            for (Map.Entry<String, Portal> entry : portals.entrySet()) {
                var portal = entry.getValue();
                plugin.getServer().getRegionScheduler().execute(plugin, portal.getExit(), () ->
                    portal.getEffect().display(portal)
                );
            }
        }).exceptionally(_ -> null);
    }
}
