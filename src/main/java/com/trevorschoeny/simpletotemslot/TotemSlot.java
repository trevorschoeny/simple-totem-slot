package com.trevorschoeny.simpletotemslot;

import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.slot.ParityGroup;
import com.trevlar.slotkit.api.slot.SlotSpec;
import com.trevlar.slotkit.api.storage.Storage;
import com.trevlar.slotkit.api.storage.StorageAttachment;
import com.trevlar.slotkit.api.storage.StorageAttachment.PlayerStorageAttachment;
import com.trevlar.slotkit.api.window.GatingContext;
import com.trevlar.slotkit.api.window.SlotGate;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The totem slot: its storage and what it accepts.
 *
 * <p>One real synced slot backed by a 1-stack player attachment, registered as a SlotKit
 * {@link ParityGroup} at common init, so SlotKit builds it on every container menu
 * on both sides with no mixin of ours. Where it shows on screen is the client's
 * {@code TotemPanel}. Vanilla's equipment loops never see it, so the
 * one vanilla mechanic it needs, the death save, is added in {@code TotemSlotSaveMixin}.
 * Curse of Binding is SlotKit's (declared on the group), and the death drop is SlotKit's
 * attachment default, which follows vanilla and keepInventory.
 *
 * <p>Kept the same shape as Simple Elytra Slot's {@code ElytraSlot}: the two mods share
 * one column above the offhand and differ only in the item and the order number.
 */
public final class TotemSlot {

    private TotemSlot() {}

    /** The group's owner id, also the client panel's id; SlotKit derives {@link #GROUP_ID} from it. */
    public static final String PANEL_ID = SimpleTotemSlot.MOD_ID + ":totem";
    public static final String GROUP = "slot";

    /**
     * The slot's category: its own, not {@code PLAYER_INVENTORY} or {@code PLAYER_OFFHAND}.
     * A search for spares (a restock, another mod's inventory scan) must not take the
     * totem a player is keeping ready, and the offhand category means vanilla's one slot.
     */
    public static final SlotGroupCategory CATEGORY = new SlotGroupCategory(SimpleTotemSlot.MOD_ID, "totem");

    /**
     * Order in the column above the offhand: lower sits nearer the offhand. Totem 10,
     * elytra 20, room for a firework slot. The numbers are shared across the mods in
     * that column, so change them together.
     */
    public static final int COLUMN_ORDER = 10;

    /** The slot's group, for SlotKit lookups (operations, Reach, locks). */
    public static final com.trevlar.slotkit.api.group.SlotGroupId.Created GROUP_ID =
            ParityGroup.idOf(PANEL_ID, GROUP);

    /** The slot group, set by {@link #register}. */
    public static ParityGroup TOTEM;

    /** The faint totem outline on the empty slot ({@code textures/gui/sprites/container/slot/totem.png}). */
    public static final Identifier EMPTY_ICON =
            Identifier.fromNamespaceAndPath(SimpleTotemSlot.MOD_ID, "container/slot/totem");

    /** The totem, saved with the player on the server. Drops on death as vanilla gear does. */
    private static PlayerStorageAttachment<NonNullList<ItemStack>> STORAGE;

    /** Call once from the main entrypoint, on both sides. */
    public static void register() {
        STORAGE = StorageAttachment.playerAttached(SimpleTotemSlot.MOD_ID, "totem", 1);

        // The slot's data only. The panel it shows in is the client's (TotemPanel).
        TOTEM = ParityGroup.register(PANEL_ID, SlotSpec.at(GROUP, CATEGORY)
                .storage(p -> STORAGE.bind(p))
                .label("Totem slot")
                .gate(GATE)
                .binding(true)
                // Shift-clicked totems land here first.
                .shiftClickIn(true)
                // The outline is vanilla's own getNoItemIcon pass; no mixin of ours needed.
                .emptyIcon(EMPTY_ICON));
    }

    /**
     * Totems of undying only, one at a time. Placing is refused while the mod is off,
     * which also stops shift-click routing into the hidden slot; taking out is always
     * allowed (binding aside).
     */
    private static final SlotGate GATE = new SlotGate() {
        @Override public boolean mayPlace(ItemStack stack, GatingContext ctx) {
            return TotemSlotSettings.enabled() && isTotem(stack);
        }
        @Override public boolean mayPickup(Player player, GatingContext ctx) { return true; }
        @Override public int maxStackSize(ItemStack stack, int vanillaMax) { return Math.min(1, vanillaMax); }
    };

    /** True for a totem of undying, the only item the slot takes (a literal item check, on purpose). */
    public static boolean isTotem(ItemStack stack) {
        return stack.is(Items.TOTEM_OF_UNDYING);
    }

    /** The totem in the slot, or EMPTY. A copy: change it through {@link #consume}. */
    public static ItemStack get(Player player) {
        return STORAGE.bind(player).getStack(0);
    }

    /**
     * Uses up one totem. The storage read is a detached copy, so the shrink only sticks
     * (and syncs to the client) once {@code setStack} writes it back.
     */
    public static void consume(Player player) {
        Storage storage = STORAGE.bind(player);
        ItemStack totem = storage.getStack(0);
        if (totem.isEmpty()) return;
        totem.shrink(1);
        storage.setStack(0, totem);
    }
}
