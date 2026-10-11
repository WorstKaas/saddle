/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import strelitzia.saddle.portal.Portal;

import java.util.concurrent.ThreadLocalRandom;

/// Utility class for displaying particles around a portal
@UtilityClass
public class ParticleEmitter {

    private static final int MAX_PARTICLES = 2000;

    @SuppressFBWarnings(
            value = "PREDICTABLE_RANDOM",
            justification = "False positive: ThreadLocalRandom is used for non-security-sensitive purposes."
    )
    static void emit(Portal portal, Particle particle, double density) {
        var world = Bukkit.getWorld(portal.getWorld());
        if (world == null) {
            return;
        }

        var random = ThreadLocalRandom.current();

        double minX = portal.getMin().x();
        double minY = portal.getMin().y();
        double minZ = portal.getMin().z();
        double maxX = portal.getMax().x() + 1.0;
        double maxY = portal.getMax().y() + 1.0;
        double maxZ = portal.getMax().z() + 1.0;

        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;

        double interiorExpected = width * height * depth * density;
        double wallExpected = 2.0 * (width * height + depth * height) * density * 2.0;

        // Cap the total, shrinking both groups by the same factor so the 1:2 ratio is preserved
        double total = interiorExpected + wallExpected;
        if (total > MAX_PARTICLES) {
            double scale = MAX_PARTICLES / total;
            interiorExpected *= scale;
            wallExpected *= scale;
        }

        int interiorCount = roundStochastic(interiorExpected, random);
        int wallCount = roundStochastic(wallExpected, random);

        for (int i = 0; i < interiorCount; i++) {
            world.spawnParticle(particle,
                    random.nextDouble(minX, maxX),
                    random.nextDouble(minY, maxY),
                    random.nextDouble(minZ, maxZ),
                    1, 0.5, 0.5, 0.5, 0);
        }

        double xWallChance = depth / (width + depth);

        for (int i = 0; i < wallCount; i++) {
            double x, z;
            double y = random.nextDouble(minY, maxY);

            if (random.nextDouble() < xWallChance) {
                x = random.nextBoolean() ? minX : maxX;
                z = random.nextDouble(minZ, maxZ);
            } else {
                x = random.nextDouble(minX, maxX);
                z = random.nextBoolean() ? minZ : maxZ;
            }

            world.spawnParticle(particle, x, y, z, 1, 0.5, 0.5, 0.5, 0);
        }
    }

    /// Rounds randomly so fractional expected counts average out (e.g. 2.3 -> 2 or 3, mean 2.3).
    private static int roundStochastic(double value, ThreadLocalRandom random) {
        int whole = (int) value;
        return random.nextDouble() < (value - whole) ? whole + 1 : whole;
    }
}
