package com.trevorschoeny.simpletotemslot;

import com.trevlar.slotkit.api.item.ItemScope;
import com.trevlar.slotkit.api.item.ItemSources;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * "Use totems anywhere": after the hands and the totem slot, finds one totem elsewhere on
 * the player and uses it up, nearest first, per SlotKit's {@link ItemSources} (the
 * inventory, hotbar first; then the ender chest; shulker boxes and bundles searched to
 * any depth where their flags are on). A totem is any item with vanilla's
 * {@code DEATH_PROTECTION} component, the same test vanilla uses for the hands.
 */
public final class TotemSearch {

    private TotemSearch() {}

    /**
     * Uses up one totem found per the settings and returns a one-count copy of it, or EMPTY
     * when "Use totems anywhere" is off or there's none.
     */
    public static ItemStack take(Player player) {
        if (!TotemSlotSettings.anywhere() || !(player instanceof ServerPlayer serverPlayer)) return ItemStack.EMPTY;
        ItemScope scope = ItemScope.inventory()
                .enderChest(TotemSlotSettings.inEnderChest())
                .shulkers(TotemSlotSettings.inShulkers())
                .bundles(TotemSlotSettings.inBundles());
        return ItemSources.take(serverPlayer, scope, s -> s.has(DataComponents.DEATH_PROTECTION), 1);
    }
}
