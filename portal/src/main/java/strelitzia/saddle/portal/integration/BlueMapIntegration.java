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

/// Integration with BlueMap web map for displaying portal markers
/// Adds POI markers for all portals on the web map and updates them when portals change
@RequiredArgsConstructor
public class BlueMapIntegration {

    private boolean isEnabled = false;

    /// The plugin instance
    private final Plugin plugin;
    private final Repository<String, Portal> portalRepository;
    private static final String MARKER_SET_ID = "portals";
    private static final String MARKER_SET_LABEL = "Portals";

    /// Initializes the BlueMap integration
    /// Registers listeners for BlueMap API enable/disable events
    /// Adds markers immediately if BlueMap is already enabled
    public void initialize() {
        BlueMapAPI.onEnable(api -> {
            isEnabled = true;
            plugin.getLogger().info("BlueMap API detected, adding portal markers...");
            updateMarkers(api);
        });

        BlueMapAPI.onDisable(api -> {
            isEnabled = false;
            plugin.getLogger().info("BlueMap API disabled, removing portal markers...");
            removeMarkers(api);
        });

        BlueMapAPI.getInstance().ifPresent(api -> {
            isEnabled = true;
            plugin.getLogger().info("BlueMap API already enabled, adding portal markers...");
            updateMarkers(api);
        });
    }

    /// Updates portal markers on the BlueMap web map
    /// Creates POI markers for all portals with their configured icons
    ///
    /// @param api the BlueMap API instance
    private void updateMarkers(BlueMapAPI api) {
        if (!isEnabled) {
            return;
        }

        portalRepository.getAll().thenAcceptAsync(portals -> {

            for (var entry : portals.entrySet()) {
                var portal = entry.getValue();

                if (portal.getExit() == null) {
                    continue;
                }

                api.getWorld(portal.getExit().getWorld()).ifPresent(world -> {
                    var marker = POIMarker
                            .builder()
                            .label(portal.getName())
                            .position(new Vector3d(portal.getExit().x(), portal.getExit().y(), portal.getExit().z()))
                            .icon(portal.getIcon(), -8, -8)
                            .maxDistance(1000)
                            .build();

                    for (var map : world.getMaps()) {
                        var markerSet = map
                                .getMarkerSets()
                                .computeIfAbsent(MARKER_SET_ID,
                                        _ -> MarkerSet.builder().label(MARKER_SET_LABEL).build());

                        markerSet.getMarkers().put(entry.getKey(), marker);
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

    /// Refreshes portal markers if BlueMap is available
    /// Called when portals are created, deleted, or modified
    public void refreshMarkers() {
        BlueMapAPI.getInstance().ifPresent(this::updateMarkers);
    }
}
