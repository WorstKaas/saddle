/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.nether.portal.blocker.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import strelitzia.saddle.nether.portal.blocker.PortalBlockerPlugin;

/// Listener for portal creation events
/// Blocks portal creation if it would be outside the world border or in a claimed homestead chunk
@RequiredArgsConstructor
public class PortalCreateListener implements Listener {

    private final PortalBlockerPlugin plugin;

    /// Handles portal creation events
    /// Cancels the event if the portal would be outside the world border or in a claimed homestead chunk
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        plugin.getLogger().info("PortalCreateEvent: " + event.getBlocks());
        if (event.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (isOutsideWorldBorder(event)) {
            event.setCancelled(true);
            notifyNearbyPlayers(event, plugin.getConfig().getString("blocked_border"));
            return;
        }

        if (isInClaimedHomesteadChunk(event)) {
            event.setCancelled(true);
            notifyNearbyPlayers(event, plugin.getConfig().getString("blocked_claim"));
        }
    }

    /// Checks if the portal location is outside the world border
    private boolean isOutsideWorldBorder(PortalCreateEvent event) {
        var world = event.getWorld();
        var worldBorder = world.getWorldBorder();
        var size = worldBorder.getSize() / 2.0;
        var center = worldBorder.getCenter();

        for (var block : event.getBlocks()) {
            var x = block.getX();
            var z = block.getZ();

            if (x < center.getX() - size || x + 1 > center.getX() + size || z < center.getZ() - size ||
                    z + 1 > center.getZ() + size) {
                return true;
            }
        }

        return false;
    }

    /// Checks if the portal location is in a claimed homestead chunk
    private boolean isInClaimedHomesteadChunk(PortalCreateEvent event) {
        var homesteadPlugin = plugin.getServer().getPluginManager().getPlugin("Homestead");
        if (homesteadPlugin == null || !homesteadPlugin.isEnabled()) {
            return false;
        }

        try {
            var getClaimMethod = homesteadPlugin.getClass().getMethod("getClaimAt", Chunk.class);

            for (var block : event.getBlocks()) {
                var chunk = block.getWorld().getChunkAt(block.getX() >> 4, block.getZ() >> 4);
                var claim = getClaimMethod.invoke(null, chunk);
                if (claim != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check Homestead claims: " + e.getMessage());
        }

        return false;
    }

    /// Notifies nearby players in the nether if the portal creation was blocked
    /// Broadcasts the message to players within 64 blocks of the corresponding nether location if the player is not
    /// linked to the event due to Folia
    private void notifyNearbyPlayers(PortalCreateEvent event, String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        // Prefer notifying the player directly.
        if (event.getEntity() instanceof Player player) {
            player.getScheduler().run(plugin, _ -> player.sendMessage(message), null);
        }

        // Find the source location safely.
        var blocks = event.getBlocks();
        if (blocks.isEmpty()) {
            return;
        }

        var sourceLocation = blocks.getFirst().getLocation();
        var netherX = sourceLocation.getX() / 8.0;
        var netherZ = sourceLocation.getZ() / 8.0;
        var radiusSquared = 64.0 * 64.0;

        // Schedule player access on each player's owning region.
        Bukkit.getGlobalRegionScheduler().run(plugin, _ -> {
            for (var nearbyPlayer : Bukkit.getOnlinePlayers()) {
                nearbyPlayer.getScheduler().run(plugin, _ -> {
                    if (!nearbyPlayer.isOnline() ||
                            nearbyPlayer.getWorld().getEnvironment() != World.Environment.NETHER) {
                        return;
                    }

                    var playerLocation = nearbyPlayer.getLocation();
                    double dx = playerLocation.getX() - netherX;
                    double dz = playerLocation.getZ() - netherZ;

                    // Only horizontal distance matters for this radius check.
                    if (dx * dx + dz * dz <= radiusSquared) {
                        nearbyPlayer.sendMessage(message);
                    }
                }, null);
            }
        });
    }
}
