/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import org.bukkit.Particle;
import strelitzia.saddle.portal.Portal;

public class NetherPortalEffect extends PortalEffect {

    @Override
    void display(Portal portal) {
        super.displayParticles(portal, Particle.PORTAL);
    }

    @Override
    public String getType() {
        return "portal";
    }
}
