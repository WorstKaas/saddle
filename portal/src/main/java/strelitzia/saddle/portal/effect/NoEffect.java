/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.effect;

import org.bukkit.Sound;
import strelitzia.saddle.portal.Portal;

public class NoEffect extends PortalEffect {

    @Override
    public void display(Portal portal) {
        // no particles
    }

    @Override
    public Sound getSound() {
        // no sound
        return null;
    }
}
