package com.trevorschoeny.simpletotemslot.checks;

import com.trevorschoeny.simpletotemslot.TotemSearch;
import com.trevorschoeny.simpletotemslot.TotemSlotSettings;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import testgallery.SceneWorld;

import java.util.function.Function;

/**
 * Self-checks for "Use totems anywhere" (test-gallery/scenes/simple-totem-slot/anywhere.json):
 * with the totem slot empty, a lethal hit uses a totem from the inventory, then from inside a
 * shulker box, a bundle, and a bundle in a shulker box in the ender chest, nearest first.
 * Settings are set directly; the run's config folder is thrown away after.
 */
public class TotemAnywhereChecks implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext ctx) {
        try (SceneWorld.Opened scene = SceneWorld.open(ctx, "simple-totem-slot", "anywhere")) {
            scene.standAt(scene.station("simpletotemslot:anywhere"), 0);

            // Off: nothing outside the slot is touched.
            TotemSlotSettings.reset();
            boolean untouched = scene.world().getServer().computeOnServer(s ->
                    TotemSearch.take(player(s)).isEmpty() && totemsIn(player(s).getInventory()) == 2);
            if (!untouched) throw new AssertionError("with Use totems anywhere off, the search should take nothing");

            TotemSlotSettings.setAnywhere(true);
            TotemSlotSettings.setInShulkers(true);
            TotemSlotSettings.setInBundles(true);
            TotemSlotSettings.setInEnderChest(true);

            // Nearest first: the loose inventory totem (inventory.10 = slot 19), not the shulker's.
            survives(ctx, scene, "the inventory totem", p -> p.getInventory().getItem(19).isEmpty()
                    && totemsInside(p.getInventory().getItem(20)) == 1);
            // Then the one inside the shulker box.
            survives(ctx, scene, "the shulker box's totem", p -> totemsInside(p.getInventory().getItem(20)) == 0);

            // A bundle in the inventory (inventory.5 = slot 14).
            scene.world().getServer().runCommand("item replace entity @p inventory.5 with minecraft:bundle"
                    + "[bundle_contents=[{id:\"minecraft:totem_of_undying\",count:1}]]");
            survives(ctx, scene, "the bundle's totem", p -> totemsInside(p.getInventory().getItem(14)) == 0);

            // All the way down: a bundle in a shulker box in the ender chest.
            scene.world().getServer().runCommand("item replace entity @p enderchest.4 with minecraft:shulker_box"
                    + "[container=[{slot:2,item:{id:\"minecraft:bundle\",count:1,components:{\"minecraft:bundle_contents\":"
                    + "[{id:\"minecraft:totem_of_undying\",count:1}]}}}]]");
            survives(ctx, scene, "the ender chest's nested totem", p -> totemsIn(p.getEnderChestInventory()) == 0);
        } finally {
            TotemSlotSettings.reset();
        }
    }

    /** A lethal hit is survived and {@code used} holds afterwards (the right totem went). */
    private static void survives(ClientGameTestContext ctx, SceneWorld.Opened scene, String which,
                                 Function<ServerPlayer, Boolean> used) {
        // Past the hurt cooldown, so the hit lands in full.
        ctx.waitTicks(25);
        scene.world().getServer().runCommand("damage @p 40");
        ctx.waitTicks(5);
        String result = scene.world().getServer().computeOnServer(s -> {
            ServerPlayer p = player(s);
            if (p.isDeadOrDying()) return "the player died";
            if (!used.apply(p)) return "a different totem was used";
            return "";
        });
        if (!result.isEmpty()) throw new AssertionError("expected " + which + " to save a lethal hit: " + result);
    }

    /** Totems in a container, counting inside shulker boxes and bundles all the way down. */
    private static int totemsIn(Container container) {
        int n = 0;
        for (int i = 0; i < container.getContainerSize(); i++) n += totems(container.getItem(i));
        return n;
    }

    private static int totems(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return (stack.has(DataComponents.DEATH_PROTECTION) ? stack.getCount() : 0) + totemsInside(stack);
    }

    private static int totemsInside(ItemStack stack) {
        int n = 0;
        ItemContainerContents box = stack.get(DataComponents.CONTAINER);
        if (box != null) n += box.itemCopies().mapToInt(TotemAnywhereChecks::totems).sum();
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) n += bundle.itemCopies().mapToInt(TotemAnywhereChecks::totems).sum();
        return n;
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }
}
