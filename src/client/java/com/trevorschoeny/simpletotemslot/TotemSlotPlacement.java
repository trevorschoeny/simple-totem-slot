package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.panel.ScreenOrigin;
import com.trevlar.slotkit.api.group.VanillaSlotResolver;
import com.trevlar.slotkit.api.slot.CreatedSlots;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Inventory;

/**
 * Where the totem slot sits each frame, in absolute screen pixels:
 * <ul>
 *   <li><b>Survival inventory:</b> directly above the offhand.</li>
 *   <li><b>Creative inventory tab:</b> vanilla shows no offhand there, so beside the
 *       leggings and boots, one column right with a 1px gap, in the lower of the two
 *       rows the totem and elytra slots share (Inventory Max's layout).</li>
 *   <li><b>Anywhere else</b> (chests, creative's other tabs): {@code null}, which skips
 *       the panel.</li>
 * </ul>
 */
// ponytail: interim pixel placement; SlotKit 1.1's on(PLAYER_OFFHAND) replaces this class.
final class TotemSlotPlacement {

    private TotemSlotPlacement() {}

    private static final int PITCH = CreatedSlots.SLOT_PITCH;
    // Vanilla player-inventory container indices of the creative anchors.
    private static final int BOOTS_INDEX = 36;
    private static final int LEGGINGS_INDEX = 37;

    static ScreenOrigin origin() {
        if (!(Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen<?> screen)) return null;
        if (screen instanceof CreativeModeInventoryScreen) {
            var leggings = VanillaSlotResolver.resolve(screen, LEGGINGS_INDEX).orElse(null);
            var boots = VanillaSlotResolver.resolve(screen, BOOTS_INDEX).orElse(null);
            if (leggings == null || boots == null) return null;
            // The two-row column straddles the leggings-to-boots midpoint; totem is the lower row.
            int columnMid = (leggings.frameY() + boots.frameY() + PITCH) / 2;
            return new ScreenOrigin(leggings.frameX() + PITCH + 1, columnMid);
        }
        return VanillaSlotResolver.resolve(screen, Inventory.SLOT_OFFHAND)
                .map(offhand -> new ScreenOrigin(offhand.frameX(), offhand.frameY() - PITCH))
                .orElse(null);
    }
}
