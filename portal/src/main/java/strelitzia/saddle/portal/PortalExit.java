/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;
import java.util.UUID;

/// Exit point of a [Portal], mostly as [Location#world] is not serializable
public record PortalExit(double x, double y, double z, float pitch, float yaw) implements Serializable {

    public PortalExit(Location location) {
        this(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    public Location toLocation(UUID worldId) {
        return new Location(Bukkit.getWorld(worldId), x, y, z, pitch, yaw);
    }
}
