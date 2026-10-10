/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.map;

import com.crimsonwarpedcraft.cwcommons.command.BaseCommand;
import dev.jorel.commandapi.CommandAPICommand;

/// Registers and handles the `/map` command
///
/// @author strelitzia
public class MapCommand extends BaseCommand {

    public MapCommand(MapCommandPluginConfig config) {
        super(new CommandAPICommand("map")
                .withPermission("map.use")
                .executes(new MapResponse(config.getWebmapUrl())));
    }

    @Override
    public MapCommand register() {
        super.register();
        return this;
    }
}
