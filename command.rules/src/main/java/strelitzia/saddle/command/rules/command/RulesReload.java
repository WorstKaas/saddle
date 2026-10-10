/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.rules.command;

import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;
import strelitzia.saddle.command.rules.config.RulesConfigProvider;

/// Command to reload the rules config YAML cache
public class RulesReload implements CommandExecutor {

    @Override
    public void run(CommandSender commandSender, CommandArguments commandArguments) {
        RulesConfigProvider.reload();
    }
}
