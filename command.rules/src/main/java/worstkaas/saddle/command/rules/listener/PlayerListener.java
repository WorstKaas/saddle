// Copyright (c) 2026 WorstKaas. All rights reserved.

package worstkaas.saddle.command.rules.listener;

import com.crimsonwarpedcraft.cwcommons.store.bukkit.PlayerDataManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import worstkaas.saddle.command.rules.RulesPlugin;
import worstkaas.saddle.command.rules.gui.RulesGuiFactory;

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
            var dialog = RulesGuiFactory.getDialog(true, () -> manager.save(player, true));
            player.getScheduler().runDelayed(plugin, _ -> player.showDialog(dialog), null, 20L);
        }).exceptionally(_ -> null);
    }
}
