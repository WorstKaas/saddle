/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.listener;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;
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
    private static final NamespacedKey KEY = new NamespacedKey(PortalPlugin.getInstance().getName(),
            "portal_respawn_location");

    /// Attempt to spawn players to the Spawn portal upon first join
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            withSpawnLocation(exit -> player.setRespawnLocation(exit, true));
        }
    }

    /// If the player dies without a bed set, respawn them at the Respawn portal
    /// [PlayerRespawnEvent] does not work with the current Folia API, so we have to do some funky workarounds:
    /// 1. On death, set the [#KEY] and the respawn location if it is null (=no bed / respawn anchor)
    /// 2. manually setting a bed respawn / respawn anchor clears the flag
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (player.getRespawnLocation() == null) {
            // player has no respawn location, set it to the spawn portal and annotate player with that we have done so
            withSpawnLocation(exit -> player.setRespawnLocation(exit, true));
            player.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, Boolean.TRUE);
        } else if (player.getPersistentDataContainer().has(KEY)) {
            // we have previously annotated the player, set the respawn location to the spawn portal again in case it
            // moved
            withSpawnLocation(exit -> player.setRespawnLocation(exit, true));
        }
    }

    /// Clears the key if the player spawn was overwritten from anywhere else (likely a bed)
    /// assumes no other plugins are meddling with the spawn location
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnChange(PlayerSetSpawnEvent event) {
        if (event.getCause() != PlayerSetSpawnEvent.Cause.PLUGIN) {
            event.getPlayer().getPersistentDataContainer().remove(KEY);
        }
    }

    /// Performs the action with the spawn portal's exit location
    private void withSpawnLocation(Consumer<Location> action) {
        portalManager.getSpawnPortal()
                .map(Portal::getExit)
                .ifPresent(action);
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
