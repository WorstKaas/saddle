/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import strelitzia.saddle.portal.Portal;

import javax.annotation.Nullable;
import java.util.Map;

/// Interface for portal particle effects
/// Implementations define how particles are displayed around a portal zone
public abstract class PortalEffect {


    /// supported effect types mapped to their implementations
    public static final Map<String, PortalEffect> TYPES = Map.of(
            "beacon", new BeaconEffect(),
            "leaves", new LeavesEffect(),
            "portal", new NetherPortalEffect(),
            "end_rod", new EndRodEffect(),
            "flame", new FlameEffect(),
            "soul_fire_flame", new SoulFireFlameEffect(),
            "lava", new LavaEffect(),
            "water", new WaterEffect(),
            "none", new NoEffect()
    );

    /// Displays particles around the portal zone
    /// @param portal portal at which to render the particles
    public abstract void display(Portal portal);

    /// Returns the type identifier for this effect
    /// @return the effect type string
    public final String getType() {
        return TYPES.values().stream().filter(e -> e == this).findFirst().get().getType();
    }

    /// Creates a PortalEffect instance from a type string
    /// @param type the effect type name (case-insensitive)
    /// @return the corresponding PortalEffect implementation, or `null` if unknown
    public static @Nullable PortalEffect fromType(String type) {
        return TYPES.get(type.toLowerCase());
    }


}
