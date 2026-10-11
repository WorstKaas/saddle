/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import strelitzia.saddle.portal.Portal;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/// Interface for portal particle effects
/// Implementations define how particles are displayed around a portal zone
public abstract class PortalEffect {

    /// supported effect types
    public static final List<String> TYPES = List.of(
            "beacon",
            "leaves",
            "portal",
            "end_rod",
            "flame",
            "soul_fire_flame",
            "lava",
            "water"
    );

    /// Displays particles around the portal zone
    /// @param portal portal at which to render the particles
    abstract void display(Portal portal);

    /// Returns the type identifier for this effect
    /// @return the effect type string
    public abstract String getType();

    /// Creates a PortalEffect instance from a type string
    /// @param type the effect type name (case-insensitive)
    /// @return the corresponding PortalEffect implementation, or `null` if unknown
    public static @Nullable PortalEffect fromType(String type) {
        return switch (type.toLowerCase()) {
            case "beacon" -> new BeaconEffect();
            case "leaves" -> new LeavesEffect();
            case "portal" -> new NetherPortalEffect();
            case "end_rod" -> new EndRodEffect();
            case "flame" -> new FlameEffect();
            case "soul_fire_flame" -> new SoulFireFlameEffect();
            case "lava" -> new LavaEffect();
            case "water" -> new WaterEffect();
            default -> null;
        };
    }

    /// displays a particle-based portal effect
    @SuppressFBWarnings(
            value = "PREDICTABLE_RANDOM",
            justification = "False positive: ThreadLocalRandom is used for non-security-sensitive purposes."
    )
    protected void displayParticles(Portal portal, Particle particle) {
        var world = Bukkit.getWorld(portal.getWorld());
        if (world == null) {
            return;
        }

        var random = ThreadLocalRandom.current();

        for (int i = 0; i < 20; i++) {
            double x, z;
            // Random height between min and max Y boundaries
            double y = random.nextDouble(portal.getMin().y(), portal.getMax().y() + 1.0);

            // Randomly choose a perimeter wall along X or Z
            if (random.nextBoolean()) {
                x = random.nextBoolean() ? portal.getMin().x() : portal.getMax().x();
                z = random.nextDouble(portal.getMin().z(), portal.getMax().z() + 1.0);
            } else {
                x = random.nextDouble(portal.getMin().x(), portal.getMax().x() + 1.0);
                z = random.nextBoolean() ? portal.getMin().z() : portal.getMax().z();
            }

            // Spawn particle efficiently without object allocations

            world.spawnParticle(particle, x, y, z, 1, 0.3, 0.3, 0.3, 0);
        }
    }
}
