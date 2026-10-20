package com.trevorschoeny.simpletotemslot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * "Use totems anywhere": after the hands and the totem slot, finds one totem elsewhere on
 * the player and uses it up. Nearest first:
 * <ol>
 *   <li>the inventory's own slots, hotbar first;</li>
 *   <li>inside shulker boxes and bundles carried in the inventory (when those are on);</li>
 *   <li>the ender chest's own slots (when that's on);</li>
 *   <li>inside shulker boxes and bundles in the ender chest.</li>
 * </ol>
 * Containers are searched all the way down (a bundle in a shulker box in the ender chest),
 * a container's own totems before the ones nested deeper in it.
 *
 * <p>A totem is any item with vanilla's {@code DEATH_PROTECTION} component, the same test
 * vanilla uses for the hands. Server only: the contents it changes are synced by vanilla
 * (the inventory) or saved with the player (the ender chest).
 *
 * <p>ponytail: this walker belongs in SlotKit once Mend Anywhere or Inventory Plus need the
 * same "every item the player has, containers included" search (SlotKit's inbox has the note).
 */
public final class TotemSearch {

    private TotemSearch() {}

    /** Nesting deeper than this isn't searched: a guard against a pathological item, not a rule players meet. */
    private static final int MAX_DEPTH = 16;

    /** Which places to search, read from the settings at the moment of the save. */
    private record Scope(boolean shulkers, boolean bundles) {}

    /**
     * Uses up one totem found per the settings and returns a one-count copy of it, or EMPTY
     * when "Use totems anywhere" is off or there's none.
     */
    public static ItemStack take(Player player) {
        if (!TotemSlotSettings.anywhere()) return ItemStack.EMPTY;
        Scope scope = new Scope(TotemSlotSettings.inShulkers(), TotemSlotSettings.inBundles());

        // Only the 36 main slots: the hands were vanilla's, and armour can't hold a totem.
        ItemStack used = takeFrom(player.getInventory(), 36, scope);
        if (used.isEmpty() && TotemSlotSettings.inEnderChest()) {
            Container ender = player.getEnderChestInventory();
            used = takeFrom(ender, ender.getContainerSize(), scope);
        }
        return used;
    }

    /** One top-level container: its own totems first, then inside each container item it holds. */
    private static ItemStack takeFrom(Container container, int slots, Scope scope) {
        for (int i = 0; i < slots; i++) {
            ItemStack stack = container.getItem(i);
            if (isTotem(stack)) {
                ItemStack used = stack.copyWithCount(1);
                stack.shrink(1);
                container.setChanged();
                return used;
            }
        }
        for (int i = 0; i < slots; i++) {
            ItemStack used = takeInside(container.getItem(i), scope, 0);
            if (!used.isEmpty()) {
                container.setChanged();
                return used;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Takes a totem from inside {@code holder} (a shulker box or bundle, when in scope),
     * writing its changed contents back onto it. EMPTY when it holds none.
     */
    private static ItemStack takeInside(ItemStack holder, Scope scope, int depth) {
        if (depth >= MAX_DEPTH || holder.isEmpty()) return ItemStack.EMPTY;

        if (scope.shulkers() && isShulkerBox(holder)) {
            ItemContainerContents contents = holder.get(DataComponents.CONTAINER);
            if (contents == null) return ItemStack.EMPTY;
            List<ItemStack> items = new ArrayList<>(contents.itemCopies().toList());
            ItemStack used = takeFromList(items, scope, depth);
            // fromItems keeps each item in its own slot, empties included.
            if (!used.isEmpty()) holder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
            return used;
        }

        if (scope.bundles()) {
            BundleContents contents = holder.get(DataComponents.BUNDLE_CONTENTS);
            if (contents == null) return ItemStack.EMPTY;
            List<ItemStack> items = new ArrayList<>(contents.itemCopies().toList());
            ItemStack used = takeFromList(items, scope, depth);
            // Rebuilt from templates in the same order: copyWithContents would reverse the bundle.
            if (!used.isEmpty()) {
                holder.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items.stream()
                        .filter(s -> !s.isEmpty()).map(ItemStackTemplate::fromNonEmptyStack).toList()));
            }
            return used;
        }
        return ItemStack.EMPTY;
    }

    /** A container's contents as copies: its own totems first, then deeper. Changes {@code items} in place. */
    private static ItemStack takeFromList(List<ItemStack> items, Scope scope, int depth) {
        for (ItemStack stack : items) {
            if (isTotem(stack)) {
                ItemStack used = stack.copyWithCount(1);
                stack.shrink(1);
                return used;
            }
        }
        for (ItemStack stack : items) {
            ItemStack used = takeInside(stack, scope, depth + 1);
            if (!used.isEmpty()) return used;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isTotem(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.DEATH_PROTECTION);
    }

    private static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock;
    }
}
