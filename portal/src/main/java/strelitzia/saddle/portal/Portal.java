/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerMoveEvent;
import strelitzia.saddle.portal.config.PortalConfigProvider;
import strelitzia.saddle.portal.effect.PortalEffect;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/// Represents a portal that players can use to warp between locations
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) //required for jackson deserialization
public class Portal implements Serializable {

    private UUID id = UUID.randomUUID();
    private UUID world;
    private PortalCorner min;
    private PortalCorner max;
    private PortalExit exit;
    private String effectType;
    private String icon;
    private boolean isSpawn;
    private String name;

    public Portal(PortalCorner location1, PortalCorner location2, String name, World world) {
        this.name = name;
        setLocation(location1, location2, world);
        var config = PortalConfigProvider.get();
        this.effectType = config.getDefaultEffect();
        this.icon = config.getDefaultIcon();
        this.exit = new PortalExit(
                (double) (min.x() + max.x()) / 2,
                (double) (min.y() + max.y()) / 2,
                (double) (min.z() + max.z()) / 2, 0, 0);
    }

    public Location getExit() {
        return exit.toLocation(world);
    }

    /// Gets the portal effect based on the stored effect type
    /// Named to avoid Jackson treating it as a property getter
    public PortalEffect effect() {
        return PortalEffect.fromType(effectType);
    }

    public void emitParticles() {
        effect().display(this);
    }

    /// Sets the effect by storing its type
    public void applyEffect(PortalEffect effect) {
        this.effectType = effect != null ? effect.getType() : null;
    }

    public void setLocation(PortalCorner location1, PortalCorner location2, World world) {
        this.world = world.getUID();
        int xMin = Math.min(location1.x(), location2.x());
        int yMin = Math.min(location1.y(), location2.y());
        int zMin = Math.min(location1.z(), location2.z());
        int xMax = Math.max(location1.x(), location2.x());
        int yMax = Math.max(location1.y(), location2.y());
        int zMax = Math.max(location1.z(), location2.z());
        this.min = new PortalCorner(xMin, yMin, zMin);
        this.max = new PortalCorner(xMax, yMax, zMax);
        PortalPlugin.getInstance().getLogger().info("Moved portal %s to %s - %s in %s".formatted(name, this.min, this.max,
                world.getName()));
    }

    public void updateExitLocation(Location location) {
        if (!Objects.equals(location.getWorld().getUID(), world)) {
            throw new IllegalArgumentException("Location must be in the same world as the portal");
        }
        this.exit = new PortalExit(location);
        PortalPlugin.getInstance().getLogger().info("Moved portal %s exit to %s".formatted(name, location));
    }

    /// Checks if a movement is from outside to within the portal's activation zone
    ///
    /// @param event the player movement event to check
    /// @return true if the location is within the portal zone, false otherwise
    public boolean movedIntoZone(PlayerMoveEvent event) {
        if (!Objects.equals(event.getTo().getWorld().getUID(), world)) {
            return false;
        }
        return !isInZone(event.getFrom()) && isInZone(event.getTo());
    }

    private boolean isInZone(Location location) {
        return location.getX() >= min.x() && location.getX() <= max.x()
                && location.getY() >= min.y() && location.getY() <= max.y()
                && location.getZ() >= min.z() && location.getZ() <= max.z();
    }
}
