/*
 * Copyright 2026 Strelitzia. All rights reserved.
 */

package worstkaas.saddle.command.rules.gui;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import worstkaas.saddle.command.rules.config.RulesConfigProvider;

import java.util.Locale;

/**
 * Factory for creating rules dialogs
 */
@UtilityClass
public class RulesGuiFactory {

    private static final ClickCallback.Options SINGLE_USE = ClickCallback.Options.builder().uses(1).build();

    /**
     * Create a new rules dialog.
     *
     * @param firstOpen whether the player is seeing the rules for the first time
     * @return the rules dialog
     */
    public static Dialog getDialog(boolean firstOpen, Runnable onAccept) {
        var config = RulesConfigProvider.getConfig();
        var body = config.getBody().stream().map(rule -> {
            var colour = rule.getColor() == null ? NamedTextColor.WHITE :
                    NamedTextColor.NAMES.value(rule.getColor().toLowerCase(Locale.ROOT));
            return DialogBody.plainMessage(Component.text(rule.getParagraph(), colour));
        }).toList();

        var buttonText = Component.text(firstOpen ? config.getAcceptButton() : config.getCloseButton());
        var buttonAction = DialogAction.customClick((_, _) -> onAccept.run(), SINGLE_USE);
        var button = ActionButton.create(buttonText, null, 400, buttonAction);

        return Dialog.create(builder -> builder
                .empty()
                .base(DialogBase
                        .builder(Component.text(config.getTitle(), NamedTextColor.GOLD))
                        .canCloseWithEscape(!firstOpen)
                        .body(body)
                        .build())
                .type(DialogType.notice(button)));
    }
}
