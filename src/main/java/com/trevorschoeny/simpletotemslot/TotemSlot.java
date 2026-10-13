package com.trevorschoeny.simpletotemslot;

import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.menukit.api.panel.ScreenOrigin;
import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.slot.ContainerPanel;
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

import java.util.function.Supplier;

/**
 * The totem slot: its storage, what it accepts, and its SlotKit panel.
 *
 * <p>One real synced slot backed by a 1-stack player attachment, declared as a SlotKit
 * {@link ContainerPanel} at common init, so SlotKit builds it on every container menu
 * on both sides with no mixin of ours. Vanilla's equipment loops never see it, so the
 * one vanilla mechanic it needs, the death save, is added in {@code TotemSlotSaveMixin}.
 * Curse of Binding is SlotKit's (declared on the group), and the death drop is SlotKit's
 * attachment default, which follows vanilla and keepInventory.
 *
 * <p>Kept the same shape as Simple Elytra Slot's {@code ElytraSlot}: the two mods share
 * one column above the offhand and differ only in the item and the order number.
 */
public final class TotemSlot {

    private TotemSlot() {}

    /** The panel id; SlotKit derives the group's id from it ({@link #GROUP_ID}). */
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
     * elytra 20, room for a firework slot (beryl-split.md, decision 5).
     */
    public static final int COLUMN_ORDER = 10;

    /** The slot's group, for SlotKit lookups (operations, Reach, locks). */
    public static final com.trevlar.slotkit.api.group.SlotGroupId.Created GROUP_ID =
            ContainerPanel.groupId(PANEL_ID, GROUP);

    /** The faint totem outline on the empty slot ({@code textures/gui/sprites/container/slot/totem.png}). */
    public static final Identifier EMPTY_ICON =
            Identifier.fromNamespaceAndPath(SimpleTotemSlot.MOD_ID, "container/slot/totem");

    /** The totem, saved with the player on the server. Drops on death as vanilla gear does. */
    private static PlayerStorageAttachment<NonNullList<ItemStack>> STORAGE;

    /**
     * Where the panel sits each frame, set by the client (its placement code names
     * client classes, which this common class can't). Null until then, and on a server,
     * where nothing asks.
     */
    // ponytail: pixel placement until SlotKit 1.1's ContainerPanel.Builder.on(PLAYER_OFFHAND) lands.
    public static volatile Supplier<ScreenOrigin> placement = () -> null;

    /** Call once from the main entrypoint, on both sides. */
    public static void register() {
        STORAGE = StorageAttachment.playerAttached(SimpleTotemSlot.MOD_ID, "totem", 1);

        ContainerPanel.define(PANEL_ID)
                .at(() -> placement.get(), 0)
                .visibleWhen(TotemSlotSettings::enabled)
                // A slot isn't solid, so a transparent panel eats no clicks around it.
                .opaque(false)
                .style(PanelStyle.NONE)
                .addSlot(SlotSpec.at(GROUP, CATEGORY)
                        .storage(p -> STORAGE.bind(p))
                        .label("Totem slot")
                        .gate(GATE)
                        .binding(true)
                        // Shift-clicked totems land here first (SlotKit 1.1 routes it).
                        .shiftClickIn(true))
                .register();
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
