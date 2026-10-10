/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.command.map;

import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/// Response for the [MapCommand]
@RequiredArgsConstructor
public class MapResponse implements CommandExecutor {

    private final String url;

    @Override
    public void run(CommandSender sender, CommandArguments args) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        String message = "Open the web map: <click:open_url:%s><underlined><color:#00aaff>%s</color></underlined></click>".formatted(url, url);
        sender.sendMessage(miniMessage.deserialize(message));
    }
}
