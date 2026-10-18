/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.integration;

import org.bukkit.plugin.Plugin;
import strelitzia.saddle.portal.PortalManager;

public interface MapIntegration {

    /// create an instance of the map integration if the corresponding map plugin can be found
    ///
    /// @param plugin the plugin instance
    /// @param portalManager the portal manager where the integration should register itself
    void initialize(Plugin plugin, PortalManager portalManager);

    /// Refreshes portal markers if BlueMap is available
    /// Called when portals are created, deleted, or modified
    void refreshMarkers();
}
