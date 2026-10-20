package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.panel.OutsideRegion;
import com.trevlar.menukit.api.panel.Panel;
import com.trevlar.menukit.api.panel.PanelPosition;
import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.group.SlotGroupPanelAdapter;
import com.trevlar.slotkit.api.slot.ClientSlots;
import com.trevlar.slotkit.api.slot.SlotFlowElement;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import java.util.function.BooleanSupplier;

/**
 * The panels the totem slot shows in, one column shared with Simple Elytra Slot (order
 * {@link TotemSlot#COLUMN_ORDER}: lower sits nearer the bottom, so the totem is under the
 * elytra). Both anchor to a vanilla slot group's box, so they move with the screen's frame.
 *
 * <ul>
 *   <li><b>Survival and container screens:</b> directly above the offhand. On a screen
 *       without the offhand (a chest) there is no box to anchor to, so it's skipped.</li>
 *   <li><b>Creative's inventory tab:</b> its offhand sits under the tab row, with no room
 *       above for two slots, so the column moves to the right of the armor instead,
 *       centred on the armor's height (Trev, 2026-10-20).</li>
 * </ul>
 */
final class TotemPanel {

    private TotemPanel() {}

    /**
     * Creative: the column stacks up from the bottom of the armor box, then shifts up to
     * centre on it. The box is the four armor slots' frames, 45 px tall (rows at y 6 and
     * 33), so the shift is (45 - 18 * slots) / 2: 13 for the totem alone, 4 for the pair.
     * Simple Elytra Slot applies the same shift, so the pair moves together.
     *
     * <p>ponytail: "the pair" means Simple Elytra Slot is installed, decided at client
     * start. With it installed but its slot hidden (its own Off, or a server without it),
     * the totem sits half a slot low. A per-frame centred region in MenuKit would fix that.
     */
    private static final int CREATIVE_SHIFT =
            FabricLoader.getInstance().isModLoaded("simpleelytraslot") ? -4 : -13;

    static void register() {
        BooleanSupplier live = () -> TotemSlotSettings.enabled() && TotemSlotSettings.showSlot()
                && ClientSlots.isShown(TotemSlot.GROUP_ID);

        // gap(0) keeps stacked slot panels on vanilla's 18 px pitch; the host's default
        // 2 px gap would set the slot off the grid.
        Panel aboveOffhand = slotPanel(TotemSlot.PANEL_ID,
                PanelPosition.region(OutsideRegion.TOP_CENTER).priority(TotemSlot.COLUMN_ORDER).gap(0))
                .visibleWhen(() -> live.getAsBoolean() && !inCreative());
        new SlotGroupPanelAdapter(aboveOffhand, 0).on(SlotGroupCategory.PLAYER_OFFHAND);

        Panel besideArmor = slotPanel(TotemSlot.PANEL_ID + "_creative",
                PanelPosition.region(OutsideRegion.RIGHT_ALIGN_BOTTOM).priority(TotemSlot.COLUMN_ORDER).gap(0)
                        .offset(0, CREATIVE_SHIFT))
                .visibleWhen(() -> live.getAsBoolean() && inCreative());
        new SlotGroupPanelAdapter(besideArmor, 0).on(SlotGroupCategory.PLAYER_ARMOR);
    }

    /** A transparent panel holding just the totem slot: a slot isn't solid, so it eats no clicks around it. */
    private static Panel slotPanel(String id, PanelPosition position) {
        return Panel.builder(id)
                .add(SlotFlowElement.of(TotemSlot.TOTEM))
                .style(PanelStyle.NONE)
                .position(position)
                .build()
                .opaque(false);
    }

    private static boolean inCreative() {
        return Minecraft.getInstance().gui.screen() instanceof CreativeModeInventoryScreen;
    }
}
