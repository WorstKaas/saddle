/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import com.crimsonwarpedcraft.cwcommons.store.Repository;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import strelitzia.saddle.portal.effect.PortalEffect;
import strelitzia.saddle.portal.integration.BlueMapIntegration;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/// Manager for portal operations and shared portal logic
@RequiredArgsConstructor
public class PortalManager {

    private final Repository<String, Portal> portalRepository;
    private final Repository<UUID, FavouritePortals> favouriteRepository;
    private final BlueMapIntegration blueMap;

    /// @return The current main spawn portal, or empty if none exists
    public CompletableFuture<Optional<Portal>> getSpawnPortal() {
        return portalRepository
                .getAll()
                .thenApply(portals -> portals.values().stream().filter(Portal::isSpawn).findFirst());
    }

    /// Sets the icon for a portal
    public void setIcon(String portalName, String icon, Player player) {
        portalRepository.get(portalName).thenAccept(portal -> {
            if (portal.isPresent()) {
                portal.get().setIcon(icon);
                portalRepository.put(portalName, portal.get());
                blueMap.refreshMarkers();
                PlayerMessager.send(player, "Portal %s icon set to %s", portalName, icon);
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Sets the icon for a portal
    public void setEffect(String portalName, String effect, Player player) {
        portalRepository.get(portalName).thenAccept(portal -> {
            if (portal.isPresent()) {
                portal.get().setEffect(PortalEffect.fromType(effect));
                portalRepository.put(portalName, portal.get());
                PlayerMessager.send(player, "Portal %s effect set to %s", portalName, portal.get().getEffect());
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Sets the icon for a portal
    public void setExit(String portalName, Player player) {
        portalRepository.get(portalName).thenAccept(portal -> {
            if (portal.isPresent()) {
                portal.get().setExit(player.getLocation());
                portalRepository.put(portalName, portal.get());
                PlayerMessager.send(player, "Portal %s exit moved to your current position", portalName);
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Sets a portal as the spawn portal, ensuring only one spawn exists
    /// Removes spawn status from any existing spawn portal before setting the new one
    ///
    /// @param portalName the name of the portal to set as spawn
    public void setSpawnPortal(String portalName, Player player) {
        portalRepository.getAll().thenAccept(portals -> {
            var portal = portals.get(portalName);
            if (portal == null) {
                PlayerMessager.send(player, "Portal %s not found", portalName);
                return;
            }

            String oldSpawn = null;

            // Remove spawn status from existing spawn portal
            for (var oldPortal : portals.values()) {
                if (oldPortal.isSpawn() && !oldPortal.getName().equals(portalName)) {
                    oldPortal.setSpawn(false);
                    oldSpawn = oldPortal.getName();
                    portalRepository.put(oldPortal.getName(), oldPortal);
                }
            }
            // Set spawn status on the target portal
            portal.setSpawn(true);
            portalRepository.put(portalName, portal);

            var msg = oldSpawn == null ? "Set spawn portal to '" + portalName + "'." :
                    "Spawn portal changed from '" + oldSpawn + "' to '" + portalName + "'.";
            PlayerMessager.send(player, msg);
        });
    }

    public void setPortalName(String portalName, String newName, Player player) {
        portalRepository.getAll().thenAccept(portals -> {
            if (portals.containsKey(newName)) {
                PlayerMessager.send(player, "Portal %s already exists", portalName);
            }

            var portal = portals.get(portalName);
            if (portal == null) {
                PlayerMessager.send(player, "Portal %s not found", portalName);
                return;
            }

            portalRepository.delete(portalName);
            portal.setName(newName);
            portalRepository.put(newName, portal);
            blueMap.refreshMarkers();
            PlayerMessager.send(player, "Portal %s renamed to %s", portalName, newName);
        });
    }

    public void deletePortal(String portalName, Player player) {
        portalRepository.get(portalName).thenAccept(portal -> {
            if (portal.isPresent()) {
                portalRepository.delete(portalName);
                blueMap.refreshMarkers();
                PlayerMessager.send(player, "Portal %s has been deleted", portalName);
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Creates a new location. Assumes the world the caller is in
    public void createPortal(String portalName, PortalCorner p1, PortalCorner p2, Player player) {
        portalRepository.getAll().thenAccept(portals -> {
            PortalPlugin.getInstance().getLogger().info("Creating portal " + portalName);
            if (portals.containsKey(portalName)) {
                PlayerMessager.send(player, "Portal %s already exists", portalName);
            }
            var portal = new Portal(p1, p2, portalName, player.getWorld());
            PortalPlugin.getInstance().getLogger().info("Created portal " + portalName);
            portalRepository.put(portalName, portal).join();
            PortalPlugin.getInstance().getLogger().info("Saved portal " + portalName);
            PlayerMessager.send(player, "Created portal %s", portalName);
            blueMap.refreshMarkers();
        });
    }

    public void movePortal(String portalName, PortalCorner p1, PortalCorner p2, Player player) {
        portalRepository.get(portalName).thenAccept(portal -> {
            if (portal.isPresent()) {
                portal.get().setLocation(p1, p2, player.getWorld());
                portalRepository.put(portalName, portal.get());
                blueMap.refreshMarkers();
                PlayerMessager.send(player, "Portal %s has been moved to %s", p1, p2);
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Check if the player moved into the area of a portal
    public CompletableFuture<Optional<Portal>> checkMovedIntoPortal(PlayerMoveEvent event) {
        return portalRepository
                .getAll()
                .thenApply(it -> it.values().stream().filter(portal -> portal.movedIntoZone(event)).findFirst());
    }


    /// @return All portals in the repository
    public CompletableFuture<Collection<Portal>> getPortals() {
        return portalRepository.getAll().thenApply(Map::values);
    }

    /// The players favourite portals
    public CompletableFuture<FavouritePortals> getFavourites(UUID playerId) {
        return favouriteRepository.get(playerId).thenApply(it -> it.orElse(new FavouritePortals()));
    }

    /// override or put the new favourite portal list of a player
    public CompletableFuture<Void> setFavourites(Player player, Map<UUID, Boolean> updatedFavourites) {
        var favourites = updatedFavourites.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(FavouritePortals::new));
        return favouriteRepository.put(player.getUniqueId(), favourites);
    }
}
