package com.trevorschoeny.simpletotemslot.ip;

import com.trevorschoeny.simpletotemslot.SimpleTotemSlot;
import com.trevorschoeny.simpletotemslot.TotemSlotSettings;

import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;

import com.trevlar.menukit.api.element.Button;
import com.trevlar.menukit.api.element.Checkbox;
import com.trevlar.menukit.api.element.Divider;
import com.trevlar.menukit.api.element.Flow;
import com.trevlar.menukit.api.element.PanelElement;
import com.trevlar.menukit.api.element.Tabs;
import com.trevlar.menukit.api.element.TextLabel;
import com.trevlar.menukit.api.element.TextRole;
import com.trevlar.menukit.api.element.Toggle;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The Totem Slot tab in Inventory Plus's settings menu: the frame IP's tabs have (title,
 * description, Reset to Defaults, On/Off, a line), then the two settings, greyed while
 * the mod is off. Reads {@link TotemSlotSettings} every frame and writes through its
 * setters, which save.
 *
 * <p>ponytail: the frame is laid out by hand, as Hotbar Pockets' tab does. With a third
 * suite mod doing the same, it belongs in MenuKit or IP's API instead.
 */
final class TotemSettingsTab {

    private TotemSettingsTab() {}

    /** The tab's id: IP's column switch opens it on Ctrl-click. */
    static final String TAB_ID = SimpleTotemSlot.MOD_ID;

    /** A divider's colour on the inventory look, as on IP's own tabs. */
    private static final int LINE_COLOR = 0xFF8B8B8B;

    /** Call once from client init, before the menu can be opened. */
    static void register() {
        Tabs.addTo(InventoryPlusApi.SETTINGS_MENU,
                Tabs.tab(TAB_ID).label(Component.literal("Totem Slot")).body(TotemSettingsTab::body));
        // General's Reset everything resets this tab too.
        InventoryPlusApi.registerSettingsReset(TotemSlotSettings::reset);
    }

    private static List<PanelElement> body() {
        BooleanSupplier off = () -> !TotemSlotSettings.enabled();
        return List.of(
                TextLabel.builder().at(0, 0)
                        .text(Component.literal("Totem Slot").withStyle(ChatFormatting.BOLD))
                        .role(TextRole.HEADING).build(),
                TextLabel.builder().at(0, 14)
                        .text(Component.literal("A slot for a totem of undying, so you can keep one ready "
                                + "and still use your offhand."))
                        .role(TextRole.BODY).build(),
                Flow.builder().at(0, 30).gap(10, 4)
                        .add(Button.builder().label(Component.literal("Reset to Defaults")).size(0, 16)
                                .onClick(() -> InventoryPlusApi.confirmInSettings("Reset Totem Slot to defaults?",
                                        "Every setting on this tab goes back to how a fresh install has it.",
                                        TotemSlotSettings::reset))
                                .build())
                        .add(Toggle.builder().size(40, 16)
                                .state(TotemSlotSettings::enabled, TotemSlotSettings::setEnabled)
                                .label(() -> Component.literal(TotemSlotSettings.enabled() ? "On" : "Off")).build())
                        .build(),
                Divider.horizontal().at(0, 50).color(LINE_COLOR).build(),
                checkbox(56, "Show a totem icon beside the hotbar",
                        TotemSlotSettings::hudIcon, TotemSlotSettings::setHudIcon, off),
                checkbox(72, "Show the Totem Slot button",
                        TotemSlotSettings::showButton, TotemSlotSettings::setShowButton, off));
    }

    private static PanelElement checkbox(int y, String label, BooleanSupplier get, Consumer<Boolean> set,
                                         BooleanSupplier off) {
        return Checkbox.builder().at(0, y).label(Component.literal(label)).state(get, set)
                .disabledWhen(off).build();
    }
}
