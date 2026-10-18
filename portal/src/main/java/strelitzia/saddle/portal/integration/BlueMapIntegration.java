/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.integration;

import com.crimsonwarpedcraft.cwcommons.store.Repository;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import strelitzia.saddle.portal.Portal;
import strelitzia.saddle.portal.PortalManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/// Integration with BlueMap web map for displaying portal markers
/// Adds POI markers for all portals on the web map and updates them when portals change
@RequiredArgsConstructor
public class BlueMapIntegration implements MapIntegration {

    private boolean isEnabled = false;

    /// The plugin instance
    private final Plugin plugin;
    private final PortalManager portalManager;
    private static final String MARKER_SET_ID = "portals";
    private static final String MARKER_SET_LABEL = "Portals";
    private String enderPearlIcon;

    /// Creates a BlueMapIntegration instance if BlueMap is available
    /// Returns null if BlueMap plugin is not installed or API classes are not available
    ///
    /// @param plugin the plugin instance
    /// @param portalRepository the portal repository
    @Override
    public void initialize(Plugin plugin, Repository<String, Portal> portalRepository) {
        if (plugin.getServer().getPluginManager().getPlugin("BlueMap") == null) {
            plugin.getLogger().info("BlueMap not found - integration disabled");
            return;
        }

        try {
            // Test if BlueMap API classes are available by attempting to load the class
            Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
            BlueMapAPI.onEnable(api -> {
                isEnabled = true;
                plugin.getLogger().info("BlueMap API detected, adding portal markers...");
                updateMarkers(api);
                portalManager.registerMapIntegration(this);
                try {
                    registerEnderPearlIcon(api);
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to register ender pearl icon: " + e.getMessage());
                }
            });

            BlueMapAPI.onDisable(api -> {
                isEnabled = false;
                plugin.getLogger().info("BlueMap API disabled, removing portal markers...");
                removeMarkers(api);
                portalManager.deregisterMapIntegration(this);
            });

            BlueMapAPI.getInstance().ifPresent(api -> {
                isEnabled = true;
                plugin.getLogger().info("BlueMap API already enabled, adding portal markers...");
                updateMarkers(api);
            });
            plugin.getLogger().info("BlueMap integration enabled");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("BlueMap plugin found but API classes not available - BlueMap integration disabled");
        }
    }

    /// Initializes the BlueMap integration
    /// Registers listeners for BlueMap API enable/disable events
    /// Adds markers immediately if BlueMap is already enabled
    private void initialize() {

    }


    private void registerEnderPearlIcon(BlueMapAPI api) throws IOException {
        Path iconPath = api.getWebApp()
                .getWebRoot()
                .resolve("assets/ender_pearl.png");

        Files.createDirectories(iconPath.getParent());

        try (var input = getClass().getResourceAsStream("/icons/ender_pearl.png")) {
            if (input == null) {
                throw new IOException("Missing resource: /icons/ender_pearl.png");
            }

            Files.copy(input, iconPath, StandardCopyOption.REPLACE_EXISTING);
        }

        enderPearlIcon = "assets/ender_pearl.png";
    }

    /// Updates portal markers on the BlueMap web map
    /// Creates POI markers for all portals with their configured icons
    ///
    /// @param api the BlueMap API instance
    private void updateMarkers(BlueMapAPI api) {
        if (!isEnabled) {
            return;
        }

        portalManager.getPortals().thenAcceptAsync(portals -> {

            for (var portal : portals) {
                if (portal.getExit() == null) {
                    continue;
                }
                api.getWorld(portal.getExit().getWorld()).ifPresent(world -> {
                    var marker = POIMarker
                            .builder()
                            .label(portal.getName())
                            .position(new Vector3d(portal.getExit().x(), portal.getExit().y(), portal.getExit().z()))
                            .icon(enderPearlIcon, -8, -8)
                            .maxDistance(99999999)
                            .build();

                    for (var map : world.getMaps()) {
                        var markerSet = map
                                .getMarkerSets()
                                .computeIfAbsent(MARKER_SET_ID,
                                        _ -> MarkerSet.builder().label(MARKER_SET_LABEL).build());

                        markerSet.getMarkers().put(portal.getName(), marker);
                    }
                });
            }
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to update BlueMap markers: " + e.getMessage());
            return null;
        });
    }

    /// Removes portal markers from the BlueMap web map
    /// Called when BlueMap is disabled
    ///
    /// @param api the BlueMap API instance
    private void removeMarkers(BlueMapAPI api) {
        for (var world : api.getMaps()) {
            for (BlueMapMap map : world.getWorld().getMaps()) {
                map.getMarkerSets().remove(MARKER_SET_ID);
            }
        }
    }

    @Override
    public void refreshMarkers() {
        BlueMapAPI.getInstance().ifPresent(this::updateMarkers);
    }
}
