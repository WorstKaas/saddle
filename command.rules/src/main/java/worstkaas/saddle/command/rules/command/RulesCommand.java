/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package worstkaas.saddle.command.rules.command;

import com.crimsonwarpedcraft.cwcommons.command.BaseCommand;
import com.crimsonwarpedcraft.cwcommons.store.Repository;
import dev.jorel.commandapi.CommandAPICommand;
import worstkaas.saddle.command.rules.RulesPlugin;

import java.util.UUID;

/// Registers and handles the `/rules` command
/// sub commands:
/// - reload : re-reads the config file
///
public class RulesCommand extends BaseCommand {

    /// creates the `/rules` command tree
    public RulesCommand(RulesPlugin plugin, Repository<UUID, Boolean> playerDataRepository) {
        super(createCommand(plugin, playerDataRepository));
    }

    private static CommandAPICommand createCommand(RulesPlugin plugin, Repository<UUID, Boolean> playerDataRepository) {
        var rulesReload = new CommandAPICommand("reload")
                .withPermission("rules.reload")
                .executes(new RulesReload());

        var rulesReset = new CommandAPICommand("reset")
                .withPermission("rules.reset")
                .executes(new RulesReset(playerDataRepository, plugin));

        return new CommandAPICommand("rules")
                .withPermission("rules.use")
                .executes(new Rules(plugin))
                .withSubcommands(rulesReload, rulesReset);
    }


}
