/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.rules.listener;

import com.crimsonwarpedcraft.cwcommons.store.bukkit.PlayerDataManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import strelitzia.saddle.command.rules.RulesPlugin;
import strelitzia.saddle.command.rules.config.RulesConfigProvider;
import strelitzia.saddle.command.rules.gui.RulesGuiFactory;

/**
 * Listens for new players joining and force them to read the rules
 */
@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final RulesPlugin plugin;
    private final PlayerDataManager<Boolean> manager;

    /**
     * Listens for players joining and prompts them to read the rules
     * if they have not accepted them yet.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        manager.get(player).thenAccept(accepted -> {
            if (accepted.orElse(false)) {
                return;
            }
            var dialog = RulesGuiFactory.getDialog(true, () -> onAccept(player));
            player.getScheduler().runDelayed(plugin, _ -> player.showDialog(dialog), null, 20L);
        }).exceptionally(_ -> null);
    }

    private void onAccept(Player player) {
        manager.save(player, true);
        var message = RulesConfigProvider.getConfig().getWelcomeMessage().replace("{player}", player.getName());
        Bukkit.broadcast(Component.text(message));
    }
}
