/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import strelitzia.saddle.portal.Portal;
import strelitzia.saddle.portal.PortalManager;
import strelitzia.saddle.portal.PortalPlugin;
import strelitzia.saddle.portal.gui.PortalGuiFactory;

import java.util.function.Consumer;

/// Listener for player events related to portal operations
@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final PortalManager portalManager;
    private final PortalPlugin plugin;

    /// Attempt to spawn players to the Spawn portal upon first join
    ///
    /// @param event the player join event
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            withSpawnLocation(player::teleportAsync);
        }
    }

    /// Attempt to spawn players without a respawn location to the Spawn portal upon respawning after death
    ///
    /// @param event the player respawn event
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer().getRespawnLocation() == null) {
            withSpawnLocation(event::setRespawnLocation);
        }
    }

    /// Performs the action with the spawn portal's exit location
    private void withSpawnLocation(Consumer<Location> action) {
        portalManager.getSpawnPortal()
                .thenAcceptAsync(portal -> portal.map(Portal::getExit).ifPresent(action))
                .exceptionally(_ -> null);
    }

    /// Handles player movement events to detect portal zone entry
    ///
    /// @param event the player move event
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // player hasn't moved blocks, so no need to check
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        portalManager.checkMovedIntoPortal(event).thenAcceptAsync(portal -> {
            if (portal.isPresent()) {
                var player = event.getPlayer();
                var dialog = PortalGuiFactory.getDialog(portal.get(), (target) -> player.teleportAsync(target.getExit()),
                        player);
                player.getScheduler().run(plugin, _ -> player.showDialog(dialog), null);
            }
        });
    }
}
