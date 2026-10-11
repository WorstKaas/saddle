/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import org.bukkit.Particle;
import strelitzia.saddle.portal.Portal;

/// Portal effect that displays a beacon-like structure
public class BeaconEffect extends PortalEffect {

    @Override
    void display(Portal portal) {
        super.displayParticles(portal, Particle.END_ROD);
    }

    @Override
    public String getType() {
        return "beacon";
    }
}
