package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.panel.Contribution;
import com.trevlar.menukit.api.panel.ModeScope;
import com.trevlar.menukit.api.panel.OutsideRegion;
import com.trevlar.menukit.api.panel.PanelPosition;
import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.menukit.api.panel.SharedPanels;
import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.slot.ClientSlots;
import com.trevlar.slotkit.api.slot.SlotFlowElement;
import com.trevlar.slotkit.api.slot.SlotPlacement;

/**
 * The totem slot's part of {@code trevlar:gear}, the panel it shares with Simple Elytra Slot:
 * one column, elytra on top (order 10), totem below it (order 20), nearest the offhand.
 * Above the offhand in survival; right of the armor in creative, where the offhand has no
 * room above it under the tab row, centered on the armor's height (Trev, 2026-10-20). A screen without
 * the offhand or the armor (a chest, a creative item tab) has no group to place it on, so it
 * doesn't show.
 *
 * <p>Everything after {@code visibleWhen} is the panel's own declaration and matches Simple
 * Elytra Slot's exactly; MenuKit warns if the two differ.
 */
final class TotemPanel {

    private TotemPanel() {}

    static final String SHARED_ID = "trevlar:gear";
    private static final int ORDER = 20;

    static void register() {
        SharedPanels.join(SHARED_ID, Contribution.builder(ORDER, ModeScope.BOTH)
                .add(SlotFlowElement.of(TotemSlot.TOTEM))
                .visibleWhen(() -> TotemSlotSettings.enabled() && TotemSlotSettings.showSlot()
                        && ClientSlots.isShown(TotemSlot.GROUP_ID))
                .in(ModeScope.SURVIVAL, SlotPlacement.group(SlotGroupCategory.PLAYER_OFFHAND,
                        PanelPosition.region(OutsideRegion.TOP_CENTER).gap(0)))
                .in(ModeScope.CREATIVE, SlotPlacement.group(SlotGroupCategory.PLAYER_ARMOR,
                        PanelPosition.region(OutsideRegion.RIGHT_CENTER).gap(0)))
                .style(PanelStyle.NONE)
                .vertical()
                .gap(0)
                .build());
    }
}
