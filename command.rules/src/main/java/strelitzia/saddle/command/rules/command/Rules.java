/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.rules.command;

import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import strelitzia.saddle.command.rules.RulesPlugin;
import strelitzia.saddle.command.rules.config.RulesConfigProvider;
import strelitzia.saddle.command.rules.gui.RulesGuiFactory;

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
