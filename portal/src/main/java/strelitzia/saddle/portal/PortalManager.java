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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/// Manager for portal operations and shared portal logic
@RequiredArgsConstructor
public class PortalManager {

    private final Repository<String, Portal> portalRepository;
    private final Repository<UUID, FavouritePortals> favouriteRepository;
    private final BlueMapIntegration blueMap;
    private final Map<String, Portal> portalCache = new ConcurrentHashMap<>();

    /// @return The current main spawn portal, or empty if none exists
    public CompletableFuture<Optional<Portal>> getSpawnPortal() {
        return getPortals().thenApplyAsync(portals -> portals.stream().filter(Portal::isSpawn).findFirst());
    }

    /// Sets the icon for a portal
    public void setIcon(String portalName, String icon, Player player) {
        portalRepository.get(portalName).thenAcceptAsync(portal -> {
            if (portal.isPresent()) {
                portal.get().setIcon(icon);
                portalRepository.put(portalName, portal.get());
                portalCache.put(portalName, portal.get());
                blueMap.refreshMarkers();
                PlayerMessager.send(player, "Portal %s icon set to %s", portalName, icon);
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Sets the effect for a portal
    public void setEffect(String portalName, String effect, Player player) {
        portalRepository.get(portalName).thenAcceptAsync(portal -> {
            if (portal.isPresent()) {
                portal.get().applyEffect(PortalEffect.fromType(effect));
                portalRepository.put(portalName, portal.get());
                portalCache.put(portalName, portal.get());
                PlayerMessager.send(player, "Portal %s effect set to %s", portalName, portal.get().effect());
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Sets the icon for a portal
    public void setExit(String portalName, Player player) {
        portalRepository.get(portalName).thenAcceptAsync(portal -> {
            if (portal.isPresent()) {
                portal.get().updateExitLocation(player.getLocation());
                portalRepository.put(portalName, portal.get());
                portalCache.put(portalName, portal.get());
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
        getPortals().thenAcceptAsync(portals -> {
            var portal = portalCache.get(portalName);
            if (portal == null) {
                PlayerMessager.send(player, "Portal %s not found", portalName);
                return;
            }

            String oldSpawn = null;

            // Remove spawn status from existing spawn portal
            for (var oldPortal : portals) {
                if (oldPortal.isSpawn() && !oldPortal.getName().equals(portalName)) {
                    oldPortal.setSpawn(false);
                    oldSpawn = oldPortal.getName();
                    portalRepository.put(oldPortal.getName(), oldPortal);
                    portalCache.put(oldPortal.getName(), oldPortal);
                }
            }
            // Set spawn status on the target portal
            portal.setSpawn(true);
            portalRepository.put(portalName, portal);
            portalCache.put(portalName, portal);

            var msg = oldSpawn == null ? "Set spawn portal to '" + portalName + "'." :
                    "Spawn portal changed from '" + oldSpawn + "' to '" + portalName + "'.";
            PlayerMessager.send(player, msg);
        });
    }

    public void setPortalName(String portalName, String newName, Player player) {
        getPortals().thenAcceptAsync(portals -> {
            if (portalCache.containsKey(newName)) {
                PlayerMessager.send(player, "Portal %s already exists", newName);
                return;
            }

            var portal = portalCache.get(portalName);
            if (portal == null) {
                PlayerMessager.send(player, "Portal %s not found", portalName);
                return;
            }

            portalRepository.delete(portalName);
            portalCache.remove(portalName);
            portal.setName(newName);
            portalRepository.put(newName, portal);
            portalCache.put(newName, portal);
            blueMap.refreshMarkers();
            PlayerMessager.send(player, "Portal %s renamed to %s", portalName, newName);
        });
    }

    public void deletePortal(String portalName, Player player) {
        portalRepository.get(portalName).thenAcceptAsync(portal -> {
            if (portal.isPresent()) {
                portalRepository.delete(portalName);
                portalCache.remove(portalName);
                blueMap.refreshMarkers();
                PlayerMessager.send(player, "Portal %s has been deleted", portalName);
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Creates a new location. Assumes the world the caller is in
    public void createPortal(String portalName, PortalCorner p1, PortalCorner p2, Player player) {
        if (portalCache.containsKey(portalName)) {
            PlayerMessager.send(player, "Portal %s already exists", portalName);
            return;
        }
        portalRepository.get(portalName).thenComposeAsync(existing -> {
            PortalPlugin.getInstance().getLogger().info("Creating portal " + portalName);
            if (existing.isPresent()) {
                PlayerMessager.send(player, "Portal %s already exists", portalName);
                return CompletableFuture.completedFuture(null);
            }
            var portal = new Portal(p1, p2, portalName, player.getWorld());
            PortalPlugin.getInstance().getLogger().info("Created portal " + portalName);
            portalCache.put(portalName, portal);
            return portalRepository.put(portalName, portal);
        }).thenAcceptAsync(_ -> {
            PortalPlugin.getInstance().getLogger().info("Saved portal " + portalName);
            PlayerMessager.send(player, "Created portal %s", portalName);
            blueMap.refreshMarkers();
        }).exceptionally(e -> {
            PortalPlugin.getInstance().getLogger().severe("Failed to create portal: " + e.getMessage());
            e.printStackTrace();
            return null;
        });
    }

    public void movePortal(String portalName, PortalCorner p1, PortalCorner p2, Player player) {
        portalRepository.get(portalName).thenAcceptAsync(portal -> {
            if (portal.isPresent()) {
                portal.get().setLocation(p1, p2, player.getWorld());
                portalRepository.put(portalName, portal.get());
                portalCache.put(portalName, portal.get());
                blueMap.refreshMarkers();
                PlayerMessager.send(player, "Portal %s has been moved to %s", p1, p2);
            } else {
                PlayerMessager.send(player, "Portal %s not found", portalName);
            }
        });
    }

    /// Check if the player moved into the area of a portal
    public CompletableFuture<Optional<Portal>> checkMovedIntoPortal(PlayerMoveEvent event) {
        return getPortals()
                .thenApplyAsync(portals -> portals.stream().filter(portal -> portal.movedIntoZone(event)).findFirst());
    }


    /// @return All portals in the repository
    public CompletableFuture<Collection<Portal>> getPortals() {
        if (!portalCache.isEmpty()) {
            return CompletableFuture.completedFuture(portalCache.values());
        }
        return portalRepository.getAll().thenApplyAsync(portals -> {
            portalCache.putAll(portals);
            return portals.values();
        });
    }

    /// The players favourite portals
    public CompletableFuture<FavouritePortals> getFavourites(UUID playerId) {
        return favouriteRepository.get(playerId).thenApplyAsync(it -> it.orElse(new FavouritePortals()));
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
