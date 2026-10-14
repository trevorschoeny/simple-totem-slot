package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.element.ItemDisplay;
import com.trevlar.menukit.api.hud.HudPanel;
import com.trevlar.menukit.api.panel.OutsideRegion;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A small totem icon left of the hotbar while the slot holds one. Icon only, no
 * count. Priority 10, so Simple Elytra Slot's icon (priority 20) stacks above it
 * in the same region (menukit docs/recipes.md, "Put an icon beside the hotbar").
 */
final class TotemHud {

    private TotemHud() {}

    private static final int ICON_SIZE = 12;

    static void register() {
        HudPanel.builder(TotemSlot.PANEL_ID + "_hud")
                .region(OutsideRegion.LEFT_ALIGN_BOTTOM)
                .priority(10)
                .offset(0, -3)
                .visibleWhen(() -> TotemSlotSettings.hudIcon() && !shown().isEmpty())
                .element(ItemDisplay.builder().item(TotemHud::shown)
                        .size(ICON_SIZE, ICON_SIZE).hideCount().hideDurability().build())
                .build();
    }

    /** The slot's totem, or EMPTY (mod off, no player, empty slot). */
    private static ItemStack shown() {
        Player p = Minecraft.getInstance().player;
        return p == null || !TotemSlotSettings.enabled() ? ItemStack.EMPTY : TotemSlot.get(p);
    }
}
