package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.element.RenderContext;
import com.trevlar.menukit.api.hud.HudPanel;
import com.trevlar.menukit.api.panel.ModeScope;
import com.trevlar.menukit.api.panel.OutsideRegion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The slot's totem on the HUD while it holds one, in a hotbar slot scaled down: left of the
 * hotbar, bottom-aligned with it, in a row with Simple Elytra Slot's box (Trev, 2026-10-20).
 * The totem is nearest the hotbar; the elytra sits to its left, or in its place when the
 * totem isn't showing. While vanilla draws the offhand slot on the left, the row lifts into
 * the hearts and XP band instead of being pushed sideways.
 *
 * <p>The row is MenuKit's {@code TOP_ALIGN_RIGHT} region around the hotbar, moved left of
 * it by a fixed offset: that region flows leftward by priority and a hidden panel takes no
 * room, which is the row. Its resting height is the lifted one, just above the hotbar; the
 * box draws itself {@link #DROP} lower while the offhand slot isn't showing. Simple Elytra
 * Slot uses the same region, offset, size and drop at priority 20, so the two line up.
 */
final class TotemHud {

    private TotemHud() {}

    /** One hotbar slot cut from vanilla's sprite (see {@link #drawBox}) is 22 x 22. */
    private static final int CELL = 22;
    /** The box on screen: the cell scaled to 18 px, a little smaller than the hotbar's own. */
    static final int SIZE = 18;
    private static final float SCALE = SIZE / (float) CELL;
    /** Hotbar width, and the 2 px gap kept between the row and the hotbar. */
    private static final int HOTBAR_WIDTH = 182;
    private static final int GAP = 2;
    /** From the lifted height down to bottom-aligned with the hotbar: its 22 px plus the gap. */
    private static final int DROP = 22 + GAP;

    private static final Identifier HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");

    static void register() {
        HudPanel.builder(TotemSlot.PANEL_ID + "_hud", ModeScope.BOTH)
                .region(OutsideRegion.TOP_ALIGN_RIGHT)
                .priority(10)
                .gap(GAP)
                // From the hotbar's right end to just left of its left end.
                .offset(-(HOTBAR_WIDTH + GAP), 0)
                .visibleWhen(() -> TotemSlotSettings.hudIcon() && !shown().isEmpty())
                .custom(0, 0, SIZE, SIZE, ctx -> drawBox(ctx, shown(), offhandOnLeft() ? 0 : DROP))
                .build();
    }

    /**
     * A hotbar slot holding {@code stack}, {@code dy} below the element's origin. The cell
     * is the hotbar sprite's first 21 columns (its left border, the slot's 2 px frame, the
     * 16 px inside, the frame again) plus the sprite's last column as the right border, so
     * the 16 px item at (3, 3) sits exactly in the middle. Drawn at 22 px and scaled.
     */
    private static void drawBox(RenderContext ctx, ItemStack stack, int dy) {
        var g = ctx.graphics();
        g.pose().pushMatrix();
        g.pose().translate(ctx.originX(), ctx.originY() + dy);
        g.pose().scale(SCALE, SCALE);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, HOTBAR_WIDTH, CELL, 0, 0, 0, 0, CELL - 1, CELL);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, HOTBAR_WIDTH, CELL, HOTBAR_WIDTH - 1, 0, CELL - 1, 0, 1, CELL);
        g.item(stack, 3, 3);
        g.pose().popMatrix();
    }

    /** Whether vanilla draws the offhand slot left of the hotbar: something in the offhand of a right-handed player. */
    private static boolean offhandOnLeft() {
        Player p = Minecraft.getInstance().player;
        return p != null && !p.getOffhandItem().isEmpty() && p.getMainArm() == HumanoidArm.RIGHT;
    }

    /** The slot's totem, or EMPTY (mod off, no player, empty slot). */
    private static ItemStack shown() {
        Player p = Minecraft.getInstance().player;
        return p == null || !TotemSlotSettings.enabled() ? ItemStack.EMPTY : TotemSlot.get(p);
    }
}
