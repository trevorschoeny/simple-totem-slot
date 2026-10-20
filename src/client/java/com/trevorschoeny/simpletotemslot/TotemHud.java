package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.hud.HudPanel;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The slot's totem on the HUD while it holds one: in a hotbar slot scaled to fit MenuKit's
 * status column, between the hearts and the hunger bar, just above the XP level number.
 * Priority 10, nearest the number, so Simple Elytra Slot's (priority 20) stacks above it,
 * as the two slots stack in the inventory. No count, no durability.
 */
final class TotemHud {

    private TotemHud() {}

    static void register() {
        HudPanel.builder(TotemSlot.PANEL_ID + "_hud")
                .statusColumn()
                .priority(10)
                .visibleWhen(() -> TotemSlotSettings.hudIcon() && !shown().isEmpty())
                // The column's shared scale, so every mod's icons match.
                .slot(0, 0).item(TotemHud::shown).hideCount().hideDurability().scale(HudPanel.STATUS_SCALE).done()
                .build();
    }

    /** The slot's totem, or EMPTY (mod off, no player, empty slot). */
    private static ItemStack shown() {
        Player p = Minecraft.getInstance().player;
        return p == null || !TotemSlotSettings.enabled() ? ItemStack.EMPTY : TotemSlot.get(p);
    }
}
