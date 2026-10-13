package com.trevorschoeny.simpletotemslot.mixin;

import com.trevorschoeny.simpletotemslot.TotemSlot;

import com.trevlar.slotkit.api.slot.CreatedSlot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The faint totem outline on the empty slot, drawn by vanilla the way it draws the
 * armor and offhand outlines: vanilla asks each empty slot for {@code getNoItemIcon()}
 * in its own slot pass, after SlotKit's slot frame, so the outline lands on top.
 * Creative's slot wrapper asks the slot it wraps, so this covers creative too.
 */
// ponytail: SlotKit has no empty-slot icon on SlotSpec yet; delete this mixin when it does.
@Mixin(Slot.class)
public abstract class TotemSlotIconMixin {

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void simpleTotemSlot$outline(CallbackInfoReturnable<Identifier> cir) {
        CreatedSlot created = CreatedSlot.of((Slot) (Object) this);
        if (created != null && TotemSlot.GROUP_ID.panelId().equals(created.panelId())) {
            cir.setReturnValue(TotemSlot.EMPTY_ICON);
        }
    }
}
