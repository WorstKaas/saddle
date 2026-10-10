/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.rules.command;

import com.crimsonwarpedcraft.cwcommons.store.Repository;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import strelitzia.saddle.command.rules.RulesPlugin;
import strelitzia.saddle.command.rules.gui.RulesGuiFactory;

import java.util.UUID;

/// resets the has-read rules flag for a player, forcing them to re-read the rules on login, and showing them to all
/// currently online players forcing them to re-accept them
@RequiredArgsConstructor
public class RulesReset implements CommandExecutor {

    private final Repository<UUID, Boolean> playerDataRepository;
    private final RulesPlugin plugin;

    @Override
    public void run(CommandSender commandSender, CommandArguments commandArguments) {
        playerDataRepository.getAll().thenAccept(players -> players.keySet().forEach(uuid -> playerDataRepository.put(uuid, false)));

        for (var player : Bukkit.getOnlinePlayers()) {
            playerDataRepository.get(player.getUniqueId()).thenAccept(accepted -> {
                if (accepted.orElse(false)) {
                    return;
                }
                var dialog =
                        RulesGuiFactory.getDialog(true, () -> playerDataRepository.put(player.getUniqueId(), true));
                player.getScheduler().run(plugin, _ -> player.showDialog(dialog), null);
            }).exceptionally(_ -> null);
        }
    }
}
