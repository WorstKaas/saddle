/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import org.bukkit.Particle;
import strelitzia.saddle.portal.Portal;

/// Portal effect that displays a beacon-like structure
public class BeaconEffect extends PortalEffect {

    @Override
    public void display(Portal portal) {
        ParticleEmitter.emit(portal, Particle.END_ROD, .25);
    }
}
