/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */
package strelitzia.saddle.portal.integration;

import lombok.RequiredArgsConstructor;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.layer.CustomLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import strelitzia.saddle.portal.Portal;
import strelitzia.saddle.portal.PortalManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class Pl3xMapIntegration implements MapIntegration, Listener {

    private static final String LAYER_KEY = "strelitzia_portals";
    private static final String ICON_KEY = "strelitzia_enderpearl";

    private final PortalManager portalManager;


    private final AtomicLong refreshGeneration = new AtomicLong();

    private final Map<String, CustomLayer> registeredLayers = new ConcurrentHashMap<>();

    @Override
    public void initialize(Plugin plugin, PortalManager portalManager) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Pl3xMap")) {
            return;
        }

        if (!Pl3xMap.api().isEnabled()) {
            return;
        }


        registerIcon(plugin);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        portalManager.registerMapIntegration(this);

        refreshMarkers();
    }

    private void registerIcon(Plugin plugin) {
        try (InputStream input = plugin.getResource("ender_pearl.png")) {
            if (input == null) {
                throw new IllegalStateException("Missing ender_pearl.png in plugin resources");
            }

            BufferedImage image = ImageIO.read(input);

            if (image == null) {
                throw new IllegalStateException("Failed to decode ender_pearl.png");
            }

            IconImage icon = new IconImage(ICON_KEY, image, "image/png");

            Pl3xMap.api().getIconRegistry().register(ICON_KEY, icon);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load ender_pearl.png", e);
        }
    }

    @Override
    public void refreshMarkers() {
        if (!Pl3xMap.api().isEnabled()) {
            return;
        }

        long generation = refreshGeneration.incrementAndGet();

        portalManager.getPortals().thenAcceptAsync((portals) -> {
            if (generation != refreshGeneration.get()) {
                return;
            }
            // Clear existing markers in every registered world.
            registeredLayers.values().forEach(CustomLayer::clearMarkers);

            for (Portal portal : portals) {
                var location = portal.getExit();

                if (location == null || location.getWorld() == null) {
                    continue;
                }

                var world = Pl3xMap.api().getWorldRegistry().get(location.getWorld().getName());
                if (world == null) {
                    continue;
                }
                var layer = registeredLayers.computeIfAbsent(world.getName(), ignored -> createLayer(world));

                layer.addMarker(
                        Marker.icon("portal_" + portal.hashCode(), location.getX(), location.getZ(), ICON_KEY, 24));
            }
        });
    }

    private CustomLayer createLayer(World world) {
        CustomLayer layer = new CustomLayer(LAYER_KEY, world, () -> "Portals");

        layer.setLiveUpdate(true);

        world.getLayerRegistry().register(layer);

        return layer;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if ("Pl3xMap".equals(event.getPlugin().getName())) {
            unregister();
        }
    }

    /**
     * Call this from your plugin's onDisable().
     */
    public void unregister() {
        refreshGeneration.incrementAndGet();
        registeredLayers.keySet().stream().map(Pl3xMap.api().getWorldRegistry()::get)
                .filter(Objects::nonNull)
                .forEach(world -> world.getLayerRegistry().unregister(LAYER_KEY));
        registeredLayers.clear();
        HandlerList.unregisterAll(this);
        portalManager.deregisterMapIntegration(this);
    }
}