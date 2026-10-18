/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.integration;

import com.crimsonwarpedcraft.cwcommons.store.Repository;
import org.bukkit.plugin.Plugin;
import strelitzia.saddle.portal.Portal;

public interface MapIntegration {

    /// create an instance of the map integration if the corresponding map plugin can be found
    ///
    /// @param plugin the plugin instance
    /// @param portalRepository the portal repository where the integration should register itself
    void initialize(Plugin plugin, Repository<String, Portal> portalRepository);

    /// Refreshes portal markers if BlueMap is available
    /// Called when portals are created, deleted, or modified
    void refreshMarkers();
}
