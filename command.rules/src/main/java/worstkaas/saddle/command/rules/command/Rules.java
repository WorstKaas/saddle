/*
 * Copyright (c) ${year} Strelitzia. All rights reserved.
 *
 */

package worstkaas.saddle.command.rules.command;

import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import worstkaas.saddle.command.rules.RulesPlugin;
import worstkaas.saddle.command.rules.config.RulesConfigProvider;
import worstkaas.saddle.command.rules.gui.RulesGuiFactory;

/// Command to display the rules for re-reading
@AllArgsConstructor
public class Rules implements CommandExecutor {

    private final RulesPlugin plugin;

    @Override
    public void run(CommandSender sender, CommandArguments args) {
        if (sender instanceof Player player) {
            var dialog = RulesGuiFactory.getDialog(false, () -> {});
            player.getScheduler().run(plugin, _ -> player.showDialog(dialog), null);
            var message = RulesConfigProvider.getConfig().getWelcomeMessage().replace("{player}", player.getName());
            Bukkit.broadcast(Component.text(message));
        }
    }
}
