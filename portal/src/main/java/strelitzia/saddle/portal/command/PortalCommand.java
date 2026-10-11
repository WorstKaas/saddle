/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.command;

import com.crimsonwarpedcraft.cwcommons.command.BaseCommand;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import strelitzia.saddle.portal.Portal;
import strelitzia.saddle.portal.PortalCorner;
import strelitzia.saddle.portal.PortalManager;
import strelitzia.saddle.portal.effect.PortalEffect;

import java.util.List;
import java.util.Objects;

/// Command handler for portal management
/// Provides commands for creating, editing, and managing portals
public class PortalCommand extends BaseCommand {


    /// Creates a new PortalCommand with all required dependencies
    ///
    /// @param portalManager manager for portal data
    public PortalCommand(PortalManager portalManager) {
        super(createCommand(portalManager));
    }

    /// Creates the command structure for all portal subcommands
    ///
    /// @return the configured CommandAPICommand
    private static CommandAPICommand createCommand(PortalManager pm) {
        var create = createCommand("create", "portal.create", createPortal(pm))
                .withArguments(new StringArgument("name"))
                .withArguments(getPosArgs());

        // /portal delete <name>
        var delete = createCommand("delete", "portal.delete", deletePortal(pm))
                .withArguments(portalNameArgument(pm));

        // One shared /portal edit <name> node. Its actions are subcommands attached after <name>,
        // so every action sees the name argument. Sibling nodes with the same literal name conflict.
        var position = createCommand("location", "portal.edit.location", movePortal(pm))
                .withArguments(getPosArgs());
        var rename = createCommand("rename", "portal.edit.name", renamePortal(pm))
                .withArguments(new StringArgument("newName"));
        var spawn = createCommand("setAsSpawn", "portal.setasspawn", setPortalAsSpawn(pm));
        var exit = createCommand("exit", "portal.setexit", setPortalExit(pm));
        var effect = createCommand("effect", "portal.seteffect", setPortalEffect(pm))
                .withArguments(new StringArgument("effectType").replaceSuggestions(
                        ArgumentSuggestions.strings(PortalEffect.TYPES)));
        // NamespacedKeyArgument is needed because a plain StringArgument can't contain ':' (minecraft:ender_pearl)
        var icon = createCommand("icon", "portal.seticon", setPortalIcon(pm))
                .withArguments(new NamespacedKeyArgument("icon"));

        var edit = new CommandAPICommand("edit")
                .withPermission("portal.edit")
                .withArguments(portalNameArgument(pm))
                .withSubcommands(position, rename, spawn, exit, effect, icon);

        var list = createCommand("list", "portal.list", listPortals(pm));
        var help = createCommand("help", "portal.use", portalHelp());

        return createCommand("portal", "portal.use", portalHelp())
                .withSubcommands(create, list, edit, help, delete);
    }

    private static CommandAPICommand createCommand(String name, String permissions, PlayerCommandExecutor executor) {
        return new CommandAPICommand(name).withPermission(permissions).executesPlayer(executor);
    }

    /// A `name` argument that suggests the names of all existing portals
    private static Argument<String> portalNameArgument(PortalManager pm) {
        return new StringArgument("name")
                .replaceSuggestions(ArgumentSuggestions.stringsAsync(_ ->
                        pm.getPortals().thenApply(portals ->
                                portals.stream()
                                        .map(Portal::getName)
                                        .toArray(String[]::new)
                        )
                ));
    }

    private static List<Argument<?>> getPosArgs() {
        return List.of(new IntegerArgument("x1"), new IntegerArgument("y1"), new IntegerArgument("z1"),
                new IntegerArgument("x2"), new IntegerArgument("y2"), new IntegerArgument("z2"));
    }

    private static PlayerCommandExecutor portalHelp() {
        return (player, _) -> {
            player.sendMessage("§6=== Portal Commands ===");
            player.sendMessage("§e/portal list §7- Lists all portals");
            player.sendMessage("§e/portal create <name> <x1> <y1> <z1> <x2> <y2> <z2> §7- Creates a portal");
            player.sendMessage("§e/portal delete <name> §7- Deletes a portal");
            player.sendMessage(
                    "§e/portal edit <name> location <x1> <y1> <z1> <x2> <y2> <z2> §7- Updates portal boundaries");
            player.sendMessage("§e/portal edit <name> rename <newName> §7- Renames a portal");
            player.sendMessage("§e/portal edit <name> setAsSpawn §7- Sets the spawn portal");
            player.sendMessage("§e/portal edit <name> exit §7- Sets the exit point");
            player.sendMessage("§e/portal edit <name> effect <effectType> §7- Sets the particle effect");
            player.sendMessage("§e/portal edit <name> icon <icon> §7- Sets the map icon");
            player.sendMessage("§7Effects: beacon, leaves, portal, end_rod, flame, soul_fire_flame, lava, water");
            player.sendMessage("§7Example icons: minecraft:ender_pearl, minecraft:beacon, minecraft:ender_eye");
        };
    }

    /// Handles the `/portal edit <name> icon <icon>` command
    private static PlayerCommandExecutor setPortalIcon(PortalManager portalManager) {
        return (player, args) -> {
            var name = getName(args);
            var icon = (NamespacedKey) Objects.requireNonNull(args.get("icon"));
            portalManager.setIcon(name, icon.toString(), player);
        };
    }

    /// Handles the `/portal edit <name> effect <effectType>` command
    private static PlayerCommandExecutor setPortalEffect(PortalManager portalManager) {
        return (player, args) -> {
            var name = getName(args);
            var effect = (String) Objects.requireNonNull(args.get("effectType"));
            portalManager.setEffect(name, effect, player);
        };
    }

    /// Handles the `/portal edit <name> exit` command
    private static PlayerCommandExecutor setPortalExit(PortalManager portalManager) {
        return (player, args) -> portalManager.setExit(getName(args), player);
    }

    /// Handles the `/portal edit <name> setAsSpawn` command
    private static PlayerCommandExecutor setPortalAsSpawn(PortalManager portalManager) {
        return (player, args) -> portalManager.setSpawnPortal(getName(args), player);
    }

    /// Handles the `/portal edit <name> rename <newName>` command
    private static PlayerCommandExecutor renamePortal(PortalManager portalManager) {
        return (player, args) -> {
            var name = getName(args);
            var newName = (String) Objects.requireNonNull(args.get("newName"));
            portalManager.setPortalName(name, newName, player);
        };
    }

    private static PlayerCommandExecutor listPortals(PortalManager portalManager) {
        return (player, _) -> portalManager.getPortals().thenAcceptAsync(portals -> {
            player.sendMessage("§6=== Portals ===");
            for (var portal : portals) {
                var world = Bukkit.getWorld(portal.getWorld());
                var worldName = world == null ? "UNKNOWN_WORLD" : world.getName();

                player.sendMessage(
                        "[%s] §e%s §7- X: %s Y: %s, Z: %s [%s]".formatted(portal.isSpawn() ? "X" : " ",
                                portal.getName(),
                                portal.getExit().x(), portal.getExit().y(), portal.getExit().z(), worldName));
            }
        });
    }

    /// Handles the `/portal edit <name> location` command
    private static PlayerCommandExecutor movePortal(PortalManager portalManager) {
        return (player, args) -> {
            var name = getName(args);
            var p1 = parsePosition(args, "x1", "y1", "z1");
            var p2 = parsePosition(args, "x2", "y2", "z2");
            portalManager.movePortal(name, p1, p2, player);
        };
    }

    /// Handles the `/portal delete <name>` command
    private static PlayerCommandExecutor deletePortal(PortalManager portalManager) {
        return (player, args) -> portalManager.deletePortal(getName(args), player);
    }

    /// Handles the `/portal create <name> ...` command
    private static PlayerCommandExecutor createPortal(PortalManager portalManager) {
        return (player, args) -> {
            var name = getName(args);
            var p1 = parsePosition(args, "x1", "y1", "z1");
            var p2 = parsePosition(args, "x2", "y2", "z2");
            portalManager.createPortal(name, p1, p2, player);
        };
    }

    /// Reads the parsed `name` argument (getRaw would keep quotes around quoted names)
    private static String getName(CommandArguments args) {
        return (String) Objects.requireNonNull(args.get("name"));
    }

    /// parse the 3 positional arguments
    private static PortalCorner parsePosition(CommandArguments args, String x, String y, String z) {
        return new PortalCorner((Integer) Objects.requireNonNull(args.get(x)),
                (Integer) Objects.requireNonNull(args.get(y)), (Integer) Objects.requireNonNull(args.get(z)));
    }
}