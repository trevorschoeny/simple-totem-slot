package com.trevorschoeny.simpletotemslot;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Totem Slot: a slot above the offhand for a totem of undying, used after any
 * totem in your hands. Common init loads the settings and declares the slot (both
 * sides); the save is the {@code TotemSlotSaveMixin}.
 */
public final class SimpleTotemSlot implements ModInitializer {

    public static final String MOD_ID = "simpletotemslot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        TotemSlotSettings.load();
        TotemSlot.register();
    }
}
