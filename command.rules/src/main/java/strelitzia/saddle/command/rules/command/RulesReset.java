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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/// resets the has-read rules flag for a player, forcing them to re-read the rules on login, and showing them to all
/// currently online players forcing them to re-accept them
@RequiredArgsConstructor
public class RulesReset implements CommandExecutor {

    private final Repository<UUID, Boolean> playerDataRepository;
    private final RulesPlugin plugin;

    @Override
    public void run(CommandSender commandSender, CommandArguments commandArguments) {
        var online = List.copyOf(Bukkit.getOnlinePlayers());
        playerDataRepository.getAll()
                .thenCompose(players -> CompletableFuture.allOf(
                        players.keySet().stream()
                                .map(uuid -> playerDataRepository.put(uuid, false))
                                .toArray(CompletableFuture[]::new)))
                .thenRun(() -> {
                    for (var player : online) {
                        var dialog = RulesGuiFactory.getDialog(true,
                                () -> playerDataRepository.put(player.getUniqueId(), true));
                        player.getScheduler().run(plugin, _ -> player.showDialog(dialog), null);
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Failed to reset rules acceptance", ex);
                    return null;
                });
    }
}
