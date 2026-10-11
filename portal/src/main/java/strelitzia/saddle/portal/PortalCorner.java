/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import org.bukkit.Location;

import java.io.Serializable;

/// simplified [Location] for portal corners
public record PortalCorner(int x, int y, int z) implements Serializable {
}
