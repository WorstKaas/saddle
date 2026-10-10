/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import com.crimsonwarpedcraft.cwcommons.store.Repository;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import strelitzia.saddle.portal.effect.PortalEffect;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/// Manager for portal operations and shared portal logic
@RequiredArgsConstructor
public class PortalManager {

    /// Repository containing all portals
    private final Repository<String, Portal> portalRepository;
    /// Repository containing player favourite sets
    private final Repository<UUID, FavouritePortals> favouriteRepository;

    /// @return The current main spawn portal, or empty if none exists
    public CompletableFuture<Optional<Portal>> getSpawnPortal() {
        return portalRepository
                .getAll()
                .thenApply(portals -> portals.values().stream().filter(Portal::isSpawn).findFirst());
    }

    /// Sets the icon for a portal
    public CompletableFuture<Boolean> setIcon(String portalName, String icon, Player player) {
        return portalRepository.get(portalName).thenApply(portal -> {
            if (portal.isPresent()) {
                portal.get().setIcon(icon);
                portalRepository.put(portalName, portal.get());
                player.sendMessage("Portal '" + portalName + "' icon set to '" + icon + "'.");
                return true;
            } else {
                player.sendMessage("Portal '" + portalName + "' not found.");
                return false;
            }
        });
    }

    /// Sets the icon for a portal
    public CompletableFuture<Void> setEffect(String portalName, String effect, Player player) {
        return portalRepository.get(portalName).thenAccept(portal -> {
            if (portal.isPresent()) {
                portal.get().setEffect(PortalEffect.fromType(effect));
                portalRepository.put(portalName, portal.get());
                player.sendMessage("Portal '" + portalName + "' effect set to '" + effect + "'.");
            } else {
                player.sendMessage("Portal '" + portalName + "' not found.");
            }
        });
    }

    /// Sets the icon for a portal
    public CompletableFuture<Void> setExit(String portalName, Player player) {
        return portalRepository.get(portalName).thenAccept(portal -> {
            if (portal.isPresent()) {
                portal.get().setExit(player.getLocation());
                portalRepository.put(portalName, portal.get());
                player.sendMessage("Portal '" + portalName + "' effect set to your current position");
            } else {
                player.sendMessage("Portal '" + portalName + "' not found.");
            }
        });
    }

    /// Sets a portal as the spawn portal, ensuring only one spawn exists
    /// Removes spawn status from any existing spawn portal before setting the new one
    ///
    /// @param portalName the name of the portal to set as spawn
    /// @return CompletableFuture that completes when the operation is done
    public CompletableFuture<Void> setSpawnPortal(String portalName, Player player) {
        return portalRepository.getAll().thenAccept(portals -> {
            var portal = portals.get(portalName);
            if (portal == null) {
                player.sendMessage("Portal '" + portalName + "' not found.");
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
            player.sendMessage(msg);
        });
    }

    public CompletableFuture<Boolean> setPortalName(String portalName, String newName, Player player) {
        return portalRepository.getAll().thenApply(portals -> {
            if (portals.containsKey(newName)) {
                player.sendMessage("Portal '" + newName + "' already exists.");
                return false;
            }
            var portal = portals.get(newName);
            if (portal == null) {
                player.sendMessage("Portal '" + portalName + "' not found.");
                return false;
            }
            portalRepository.delete(newName).join();
            portal.setName(newName);
            portalRepository.put(newName, portal).join();
            player.sendMessage("Portal '" + newName + "' renamed to '" + newName + "'.");
            return true;
        });
    }

    public CompletableFuture<Boolean> deletePortal(String portalName, Player player) {
        return portalRepository.get(portalName).thenApply(portal -> {
            if (portal.isPresent()) {
                portalRepository.delete(portalName).join();
                player.sendMessage("Portal '" + portalName + "' deleted.");
                return true;
            } else {
                player.sendMessage("Portal '" + portalName + "' not found.");
                return false;
            }
        });
    }

    /// Creates a new location. Assumes the world the caller is in
    public CompletableFuture<Boolean> createPortal(String name, PortalCorner p1, PortalCorner p2, Player player) {
        return portalRepository.getAll().thenApply(portals -> {
            if (portals.containsKey(name)) {
                player.sendMessage(name + " already exists.");
                return false;
            } else {
                var portal = new Portal(p1, p2, name, player.getWorld());
                portalRepository.put(name, portal).join();
                player.sendMessage("Portal '" + name + "' created.");
                return true;
            }
        });
    }

    public CompletableFuture<Boolean> movePortal(String name, PortalCorner p1, PortalCorner p2, Player player) {
        return portalRepository.get(name).thenApply(portal -> {
            if (portal.isPresent()) {
                portal.get().setLocation(p1, p2, player.getWorld());
                portalRepository.put(name, portal.get());
                player.sendMessage("Portal '" + name + "' moved to " + p1 + ", " + p2 + ".");
                return true;
            } else {
                player.sendMessage("Portal '" + name + "' not found.");
                return false;
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
