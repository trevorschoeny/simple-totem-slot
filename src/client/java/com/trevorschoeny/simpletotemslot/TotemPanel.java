package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.panel.OutsideRegion;
import com.trevlar.menukit.api.panel.Panel;
import com.trevlar.menukit.api.panel.PanelPosition;
import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.group.SlotGroupPanelAdapter;
import com.trevlar.slotkit.api.slot.ClientSlots;
import com.trevlar.slotkit.api.slot.SlotFlowElement;

/**
 * The panel the totem slot shows in: directly above the offhand, in the column it
 * shares with Simple Elytra Slot (order {@link TotemSlot#COLUMN_ORDER}, lower sits
 * nearer the offhand). On a screen without the offhand (a chest) there is no box to
 * anchor to, so the panel is skipped.
 */
final class TotemPanel {

    private TotemPanel() {}

    static void register() {
        Panel panel = Panel.builder(TotemSlot.PANEL_ID)
                .add(SlotFlowElement.of(TotemSlot.TOTEM))
                .style(PanelStyle.NONE)
                // gap(0) keeps stacked slot panels on vanilla's 18 px pitch; the host's
                // default 2 px gap would set the slot off the grid.
                .position(PanelPosition.region(OutsideRegion.TOP_CENTER).priority(TotemSlot.COLUMN_ORDER).gap(0))
                .build()
                // A slot isn't solid, so a transparent panel eats no clicks around it.
                .opaque(false)
                .visibleWhen(() -> TotemSlotSettings.enabled() && ClientSlots.isShown(TotemSlot.GROUP_ID));
        new SlotGroupPanelAdapter(panel, 0).on(SlotGroupCategory.PLAYER_OFFHAND);
    }
}
