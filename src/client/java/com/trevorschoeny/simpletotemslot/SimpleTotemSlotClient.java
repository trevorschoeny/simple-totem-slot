package com.trevorschoeny.simpletotemslot;

import com.trevorschoeny.simpletotemslot.ip.TotemSlotIp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client init: where the slot sits, the HUD icon, and, only with Inventory Plus
 * installed, the refill and IP's column switch. The {@code ip} package is touched
 * nowhere else, so IP's classes never load without IP.
 */
public final class SimpleTotemSlotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TotemSlot.placement = TotemSlotPlacement::origin;
        TotemHud.register();
        if (FabricLoader.getInstance().isModLoaded("inventoryplus")) TotemSlotIp.register();
    }
}
