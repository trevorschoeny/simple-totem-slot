package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.element.ItemDisplay;
import com.trevlar.menukit.api.hud.HudPanel;
import com.trevlar.menukit.api.panel.InsideRegion;
import com.trevlar.menukit.api.panel.PanelStyle;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A small totem icon left of the hotbar while the slot holds one. Icon only, no
 * count. Simple Elytra Slot's icon sits one row above it, so the two stack.
 */
final class TotemHud {

    private TotemHud() {}

    // BOTTOM_CENTER follows the hotbar across GUI scales; the offsets put the 12px
    // icon about 2px left of the hotbar, level with its item row (Inventory Max's spot).
    private static final int ICON_SIZE = 12;
    private static final int OFFSET_X = -99;
    private static final int OFFSET_Y = 1;

    static void register() {
        HudPanel.builder(TotemSlot.PANEL_ID + "_hud")
                .region(InsideRegion.BOTTOM_CENTER).offset(OFFSET_X, OFFSET_Y)
                .padding(0).autoSize()
                .style(PanelStyle.NONE)
                .visibleWhen(() -> TotemSlotSettings.hudIcon() && !shown().isEmpty())
                .element(ItemDisplay.builder().at(0, 0).item(TotemHud::shown)
                        .size(ICON_SIZE, ICON_SIZE).hideCount().hideDurability().build())
                .build();
    }

    /** The slot's totem, or EMPTY (mod off, no player, empty slot). */
    private static ItemStack shown() {
        Player p = Minecraft.getInstance().player;
        return p == null || !TotemSlotSettings.enabled() ? ItemStack.EMPTY : TotemSlot.get(p);
    }
}
