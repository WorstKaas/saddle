/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import org.bukkit.Particle;
import org.bukkit.Sound;
import strelitzia.saddle.portal.Portal;

import javax.annotation.Nullable;
import java.util.Map;

/// Interface for portal particle effects
/// Implementations define how particles are displayed around a portal zone
public abstract class PortalEffect {


    /// supported effect types mapped to their implementations
    public static final Map<String, PortalEffect> TYPES;

    static {
        TYPES = Map.ofEntries(
                Map.entry("beacon", new ParticlePortal(Particle.END_ROD, 0.25, Sound.BLOCK_BEACON_POWER_SELECT)),
                Map.entry("leaves", new ParticlePortal(Particle.CHERRY_LEAVES, 0.25, Sound.BLOCK_LEAF_LITTER_FALL)),
                Map.entry("portal", new ParticlePortal(Particle.PORTAL, 1, Sound.BLOCK_PORTAL_TRAVEL)),
                Map.entry("end_rod", new ParticlePortal(Particle.END_ROD, 0.25, Sound.BLOCK_BEACON_POWER_SELECT)),
                Map.entry("flame", new ParticlePortal(Particle.FLAME, 0.25, Sound.BLOCK_FIRE_AMBIENT)),
                Map.entry("soul_fire_flame",
                        new ParticlePortal(Particle.SOUL_FIRE_FLAME, 0.25, Sound.ENTITY_GHAST_SCREAM)),
                Map.entry("lava", new ParticlePortal(Particle.LAVA, 0.15, Sound.ITEM_FIRECHARGE_USE)),
                Map.entry("water", new ParticlePortal(Particle.FALLING_WATER, 1, Sound.ITEM_BUCKET_EMPTY)),
                Map.entry("snow", new ParticlePortal(Particle.SNOWFLAKE, 1, Sound.ENTITY_SNOW_GOLEM_DEATH)),
                Map.entry("sand", new ParticlePortal(Particle.CLOUD, 1, Sound.ENTITY_CAMEL_HUSK_DASH)),
                Map.entry("smoke",
                        new ParticlePortal(Particle.CAMPFIRE_SIGNAL_SMOKE, 0.25, Sound.BLOCK_FIRE_EXTINGUISH)),
                Map.entry("firefly", new ParticlePortal(Particle.FIREFLY, 2, Sound.BLOCK_FIREFLY_BUSH_IDLE)),
                Map.entry("spore", new ParticlePortal(Particle.SPORE_BLOSSOM_AIR, 1, Sound.BLOCK_SPORE_BLOSSOM_STEP)),
                Map.entry("squid", new ParticlePortal(Particle.SQUID_INK, 0.3, Sound.ENTITY_GLOW_SQUID_SQUIRT)),
                Map.entry("sculk", new ParticlePortal(Particle.SCULK_CHARGE, 0.3, Sound.ENTITY_ALLAY_ITEM_THROWN)),
                Map.entry("none", new NoEffect()));
    }


    /// Displays particles around the portal zone
    ///
    /// @param portal portal at which to render the particles
    public abstract void display(Portal portal);

    /// Returns the type identifier for this effect
    ///
    /// @return the effect type string
    public final String getType() {
        return TYPES.entrySet().stream().filter(e -> e.getValue() == this).findFirst().get().getKey();
    }

    /// Creates a PortalEffect instance from a type string
    ///
    /// @param type the effect type name (case-insensitive)
    /// @return the corresponding PortalEffect implementation, or `null` if unknown
    public static @Nullable PortalEffect fromType(String type) {
        return TYPES.get(type.toLowerCase());
    }


    /// @return the sound to play when someone warps through this portal
    public abstract @Nullable Sound getSound();
}
