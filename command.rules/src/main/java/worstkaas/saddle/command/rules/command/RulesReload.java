/*
 * Copyright (c) ${year} Strelitzia. All rights reserved.
 *
 */

package worstkaas.saddle.command.rules.command;

import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;
import worstkaas.saddle.command.rules.config.RulesConfigProvider;

/// Command to reload the rules config YAML cache
public class RulesReload implements CommandExecutor {

    @Override
    public void run(CommandSender commandSender, CommandArguments commandArguments) {
        RulesConfigProvider.reload();
    }
}
