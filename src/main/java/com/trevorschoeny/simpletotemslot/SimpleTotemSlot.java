package com.trevorschoeny.simpletotemslot;

import net.fabricmc.api.ModInitializer;

/**
 * Simple Totem Slot: a slot above the offhand for a totem of undying, used after any
 * totem in your hands. Common init declares the slot (both sides); the save is the
 * {@code TotemSlotSaveMixin}.
 */
public final class SimpleTotemSlot implements ModInitializer {

    public static final String MOD_ID = "simpletotemslot";

    @Override
    public void onInitialize() {
        TotemSlot.register();
    }
}
