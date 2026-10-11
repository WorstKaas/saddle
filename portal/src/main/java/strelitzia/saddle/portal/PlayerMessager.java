/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class PlayerMessager {

    public static void send(Player player, String msg, Object... args) {
        player.getScheduler().run(PortalPlugin.getInstance(),_ -> player.sendMessage(String.format(msg, args)), null);
    }

}
