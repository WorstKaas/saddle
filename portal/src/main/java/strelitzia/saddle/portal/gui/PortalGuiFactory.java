/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package strelitzia.saddle.portal.gui;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import strelitzia.saddle.portal.Portal;
import strelitzia.saddle.portal.PortalManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/// Factory for creating portal selection dialogs using Paper's Dialog API
/// Provides dialogs for portal selection and favourite management
@UtilityClass
public class PortalGuiFactory {

    private static final ClickCallback.Options SINGLE_USE = ClickCallback.Options.builder().uses(1).build();
    @Setter(AccessLevel.PRIVATE)
    private @Nullable PortalManager portalManager;

    /// initializes the factory
    public void init(PortalManager portalManager) {
        setPortalManager(portalManager);
    }

    /// Creates the portal selection dialog
    /// Shows favourite portals at the top, followed by all portals alphabetically
    ///
    /// @param source         the portal that opened the dialog
    /// @param onPortalSelect callback when a portal is selected
    /// @return the configured Dialog
    public static Dialog getDialog(Portal source, Consumer<Portal> onPortalSelect, Player player) {
        if (portalManager == null) {
            throw new IllegalStateException("PortalManager not initialized");
        }

        var body = new ArrayList<DialogBody>();
        body.add(DialogBody.plainMessage(Component.text("Choose a destination")));

        var portals = portalManager.getPortals().join();
        var favourites = portalManager.getFavourites(player.getUniqueId()).join();

        var buttons = new ArrayList<ActionButton>();

        // Cancel / Exit action button
        var cancelButton = ActionButton.create(Component.text("Cancel"), null, 200,
                DialogAction.customClick((_, _) -> {}, SINGLE_USE));

        // Edit Favourites button transitions to the favorites editor
        buttons.add(ActionButton.create(Component.text("Edit Favourites"), null, 200,
                DialogAction.customClick((_, _) -> {
                    var favouritesDialog = getFavouritesDialog(source, onPortalSelect, updatedFavourites -> {
                        portalManager.setFavourites(player, updatedFavourites).thenRunAsync(() -> player.showDialog(getDialog(source, onPortalSelect, player)));
                    }, player);

                    player.showDialog(favouritesDialog);
                }, SINGLE_USE)));

        portals.stream()
                .filter(portal -> !portal.equals(source))
                .sorted(Comparator.comparing((Portal p) -> favourites.contains(p.getId())).reversed())
                .map(portal -> createPortalButton(portal, onPortalSelect, favourites.contains(portal.getId())))
                .forEach(buttons::add);

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase
                        .builder(Component.text("Portal: " + source.getName()))
                        .canCloseWithEscape(true)
                        .body(body)
                        .build())
                .type(DialogType.multiAction(buttons, cancelButton, 2)));
    }

    /// Creates a button for selecting a portal
    private static ActionButton createPortalButton(Portal portal, Consumer<Portal> onPortalSelect, boolean favourite) {
        var buttonText = Component.text(portal.getName(), favourite ? NamedTextColor.GOLD : NamedTextColor.WHITE);
        var buttonAction = DialogAction.customClick((_, _) -> onPortalSelect.accept(portal), SINGLE_USE);
        return ActionButton.create(buttonText, null, 200, buttonAction);
    }


    public static Dialog getFavouritesDialog(Portal source, Consumer<Portal> onPortalSelect, Consumer<Map<UUID, Boolean>> onSave, Player player) {
        if (portalManager == null) {
            throw new IllegalStateException("PortalManager not initialized");
        }

        var body = new ArrayList<DialogBody>();
        body.add(DialogBody.plainMessage(Component.text("Select your favorite portals")));

        var portals = portalManager.getPortals().join();
        var favourites = portalManager.getFavourites(player.getUniqueId()).join();

        var inputs = new ArrayList<DialogInput>();
        for (var portal : portals) {
            boolean isFavourite = favourites.contains(portal.getId());
            inputs.add(DialogInput.bool(inputKey(portal), Component.text(portal.getName()))
                    .initial(isFavourite)
                    .build());
        }

        // Save button processes input states and triggers save callback
        var saveButton = ActionButton.create(Component.text("Save"), null, 200,
                DialogAction.customClick((response, _) -> {
                    var updatedFavourites = new HashMap<UUID, Boolean>();
                    for (var portal : portals) {
                        updatedFavourites.put(portal.getId(), response.getBoolean(inputKey(portal)));
                    }
                    onSave.accept(updatedFavourites);
                }, SINGLE_USE));

        // Cancel button takes the player back to the portal overview dialog
        var cancelButton = ActionButton.create(Component.text("Cancel"), null, 200,
                DialogAction.customClick((_, audience) -> {
                    if (audience instanceof Player p) {
                        p.showDialog(getDialog(source, onPortalSelect, p));
                    }
                }, SINGLE_USE));

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase
                        .builder(Component.text("Edit Favourites"))
                        .canCloseWithEscape(true)
                        .body(body)
                        .inputs(inputs)
                        .build())
                .type(DialogType.confirmation(saveButton, cancelButton)));
    }

    /// Dialog input keys may only contain letters, digits, and underscores, so strip the UUID hyphens. Cannot use names
    /// as those can change
    private static String inputKey(Portal portal) {
        return "portal_" + portal.getId().toString().replace("-", "");
    }
}
