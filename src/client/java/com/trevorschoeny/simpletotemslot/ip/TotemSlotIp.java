package com.trevorschoeny.simpletotemslot.ip;

import com.trevorschoeny.simpletotemslot.SimpleTotemSlot;
import com.trevorschoeny.simpletotemslot.TotemSlot;
import com.trevorschoeny.simpletotemslot.TotemSlotSettings;

import com.trevorschoeny.inventoryplus.api.HotbarCyclable.ExtraSlot;
import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;
import com.trevorschoeny.inventoryplus.api.InventoryPlusOperations;
import com.trevorschoeny.inventoryplus.api.PlayerMenuSlots;

import com.trevlar.slotkit.api.slot.CreatedSlot;
import com.trevlar.slotkit.api.window.SlotOperations;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Everything Simple Totem Slot does with Inventory Plus, loaded only when IP is:
 * <ul>
 *   <li><b>The refill.</b> When a save uses up the slot's totem, another moves in from
 *       the inventory through IP's restock search, under IP's restock switch and
 *       locks. Restocking is IP's feature, so there is no refill without IP.</li>
 *   <li><b>The settings tab</b> in IP's settings menu ({@code TotemSettingsTab}).</li>
 *   <li><b>The column switch.</b> The mod's On/Off in IP's inventory column, hidden
 *       until "Show the Totem Slot button" is on.</li>
 * </ul>
 * Locks and Reach need nothing here: IP finds the slot through SlotKit.
 */
public final class TotemSlotIp {

    private TotemSlotIp() {}

    public static void register() {
        InventoryPlusApi.addFeatureSwitch(SimpleTotemSlot.MOD_ID, "Totem Slot",
                // Still Inventory Max's equipment button art until the totem button art exists.
                Identifier.fromNamespaceAndPath(SimpleTotemSlot.MOD_ID, "totem_slot_button"),
                TotemSlotSettings::enabled, TotemSlotSettings::setEnabled,
                TotemSlotSettings::showButton, null);
        TotemSettingsTab.register();
        ClientTickEvents.END_CLIENT_TICK.register(TotemSlotIp::tick);
    }

    // A bare totem to match against the inventory, by item. Built on first use: an
    // ItemStack can't be made before the item's components are bound.
    private static ItemStack totemProbe = null;

    // Whether the slot held a totem last tick, to catch it being used up.
    private static boolean filledLastTick = false;

    /**
     * Refills the slot the tick it goes from full to empty during play. Pulling the
     * totem out by hand happens in a screen, so it doesn't refill; a slot that was
     * never filled is left alone.
     */
    private static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (player == null || gameMode == null) return;
        if (player.containerMenu != player.inventoryMenu) return;     // only the player's own menu

        boolean filledNow = !TotemSlot.get(player).isEmpty();
        boolean usedUp = filledLastTick && !filledNow;
        filledLastTick = filledNow;

        if (!usedUp) return;
        if (mc.gui.screen() != null) return;                          // gameplay only
        if (!TotemSlotSettings.enabled()) return;
        // A hidden slot refuses the totem, and the shift-click below would move it elsewhere instead.
        if (!TotemSlotSettings.showSlot()) return;
        if (!InventoryPlusApi.isItemRestockEnabled()) return;

        // A spare may sit in another mod's cycled slots (pockets); offer only the ones
        // that would let a restock move it out.
        List<ExtraSlot> extras = InventoryPlusApi.extraSlotsToQuickMove(player,
                InventoryPlusOperations.RESTOCK_TAKE, InventoryPlusOperations.RESTOCK_PUT);
        if (totemProbe == null) totemProbe = new ItemStack(Items.TOTEM_OF_UNDYING);
        int source = InventoryPlusApi.findRestockSource(player.getInventory(), totemProbe, extras);
        if (source == InventoryPlusApi.NO_SOURCE) return;
        if (!slotAllowsRestock(player)) return;                       // the slot itself refuses (a lock, Reach)

        // A cycled source moves on the server through its cycler and lands here by
        // shift-click routing; false means an ordinary inventory slot, handled below.
        if (InventoryPlusApi.quickMoveOut(source,
                InventoryPlusOperations.RESTOCK_TAKE, InventoryPlusOperations.RESTOCK_PUT)) return;

        // An inventory source: shift-click it, judged as a restock. SlotKit's routing
        // drops the totem into the now-empty slot.
        int menuSlot = PlayerMenuSlots.menuIndexOf(player.inventoryMenu, player, source);
        if (menuSlot < 0) return;
        SlotOperations.as(InventoryPlusOperations.RESTOCK_TAKE, InventoryPlusOperations.RESTOCK_PUT,
                () -> gameMode.handleContainerInput(
                        player.inventoryMenu.containerId, menuSlot, 0, ContainerInput.QUICK_MOVE, player));
    }

    /** Whether a restock may fill the totem slot on the player's inventory menu. */
    private static boolean slotAllowsRestock(LocalPlayer player) {
        AbstractContainerMenu menu = player.inventoryMenu;
        for (Slot s : menu.slots) {
            CreatedSlot created = CreatedSlot.of(s);
            if (created != null && TotemSlot.GROUP_ID.panelId().equals(created.panelId())) {
                return SlotOperations.allows(menu, s, player, InventoryPlusOperations.RESTOCK_PUT);
            }
        }
        return false;
    }
}
