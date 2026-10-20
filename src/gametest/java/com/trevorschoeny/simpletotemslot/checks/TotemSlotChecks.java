package com.trevorschoeny.simpletotemslot.checks;

import com.trevorschoeny.simpletotemslot.TotemSlot;

import com.trevlar.slotkit.api.slot.CreatedSlot;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import testgallery.SceneWorld;

import java.lang.reflect.Method;

/**
 * Self-checks for the slot scene (test-gallery/scenes/simple-totem-slot/slot.json), plus the
 * survival and creative inventory screenshots. Runs standalone by default; with
 * {@code -PwithElytra} Simple Elytra Slot is loaded too, and the checks also hold the elytra
 * slot directly above the totem slot. Screenshots carry a "-with-elytra" suffix then.
 */
public class TotemSlotChecks implements FabricClientGameTest {

    private static final boolean ELYTRA = FabricLoader.getInstance().isModLoaded("simpleelytraslot");
    private static final String SUFFIX = ELYTRA ? "-with-elytra" : "";

    @Override
    public void runTest(ClientGameTestContext ctx) {
        try (SceneWorld.Opened scene = SceneWorld.open(ctx, "simple-totem-slot", "slot")) {
            scene.standAt(scene.station("simpletotemslot:slot_above_offhand"), 0);
            slotAboveOffhand(ctx, scene);
            shiftClickIn(ctx, scene);
            save(ctx, scene);
        }
        try (SceneWorld.Opened scene = SceneWorld.open(ctx, "simple-totem-slot", "creative")) {
            creativeSlot(ctx, scene);
        }
    }

    /**
     * Station "simpletotemslot:slot_above_offhand": the survival inventory has one totem slot,
     * on the 18 px grid directly above the offhand, and it refuses anything but a totem.
     */
    private static void slotAboveOffhand(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        ctx.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
        ctx.waitForScreen(InventoryScreen.class);
        ctx.waitTicks(5);
        ctx.takeScreenshot("totem-survival-empty" + SUFFIX);
        ctx.runOnClient(mc -> assertColumn(mc, mc.player.inventoryMenu.getSlot(InventoryMenu.SHIELD_SLOT)));

        boolean refusesDirt = scene.world().getServer().computeOnServer(server -> {
            Slot slot = totemSlot(player(server).inventoryMenu.slots);
            return !slot.mayPlace(new ItemStack(Items.DIRT)) && slot.mayPlace(new ItemStack(Items.TOTEM_OF_UNDYING));
        });
        if (!refusesDirt) throw new AssertionError("the totem slot should take a totem and refuse dirt");
    }

    /** Station "simpletotemslot:shift_click_in": a shift-clicked totem lands in the slot, not the offhand. */
    private static void shiftClickIn(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        // The scene's first hotbar slot holds a totem: menu slot 36 on the player's inventory menu.
        ctx.runOnClient(mc -> mc.gameMode.handleContainerInput(
                mc.player.inventoryMenu.containerId, 36, 0, ContainerInput.QUICK_MOVE, mc.player));
        ctx.waitTicks(5);
        ctx.takeScreenshot("totem-survival-filled" + SUFFIX);
        String where = scene.world().getServer().computeOnServer(server -> {
            ServerPlayer p = player(server);
            if (!TotemSlot.isTotem(TotemSlot.get(p))) return "the totem slot is empty";
            if (!p.getOffhandItem().isEmpty()) return "the offhand took it";
            return "";
        });
        if (!where.isEmpty()) throw new AssertionError("shift-clicked totem: " + where);
        ctx.setScreen(() -> null);
        hudScreenshots(ctx, scene);
    }

    /**
     * The HUD row left of the hotbar with the totem in the slot (and, with Simple Elytra Slot,
     * an elytra in its slot beside it), lifted above the offhand slot when that shows, and
     * with the held-item name up. Looks are Trev's call, so these are screenshots only.
     */
    private static void hudScreenshots(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        // A level above 0, so vanilla draws the number the column sits on.
        scene.world().getServer().runCommand("xp set @p 12 levels");
        if (ELYTRA) {
            // An elytra in hotbar slot 5 (menu slot 41), shift-clicked into the elytra slot.
            scene.world().getServer().runCommand("item replace entity @p hotbar.5 with minecraft:elytra");
            ctx.waitTicks(5);
            ctx.runOnClient(mc -> mc.gameMode.handleContainerInput(
                    mc.player.inventoryMenu.containerId, 41, 0, ContainerInput.QUICK_MOVE, mc.player));
        }
        scene.world().getServer().runCommand("gamerule sendCommandFeedback false");
        ctx.waitTicks(10);
        ctx.takeScreenshot("totem-hud" + SUFFIX);
        // With the offhand slot showing, the row lifts above it.
        scene.world().getServer().runCommand("item replace entity @p weapon.offhand with minecraft:shield");
        ctx.waitTicks(5);
        ctx.takeScreenshot("totem-hud-offhand" + SUFFIX);
        scene.world().getServer().runCommand("item replace entity @p weapon.offhand with minecraft:air");
        // Selecting the dirt brings up its name above the hotbar.
        ctx.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(1));
        ctx.waitTicks(5);
        ctx.takeScreenshot("totem-hud-item-name" + SUFFIX);
        ctx.runOnClient(mc -> mc.player.getInventory().setSelectedSlot(0));
    }

    /** Station "simpletotemslot:save": a lethal hit with the totem in the slot and empty hands is survived. */
    private static void save(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        scene.world().getServer().runCommand("damage @p 40");
        ctx.waitTicks(10);
        String result = scene.world().getServer().computeOnServer(server -> {
            ServerPlayer p = player(server);
            if (p.isDeadOrDying()) return "the player died";
            if (!TotemSlot.get(p).isEmpty()) return "the slot's totem was not used up";
            return "";
        });
        if (!result.isEmpty()) throw new AssertionError("the slot's totem should save a lethal hit: " + result);
    }

    /**
     * The creative inventory's Survival Inventory tab shows the slot right of the armor, centred
     * on its height (above the offhand there's no room under the tab row). Not a scene station check: how it looks stays in Trev's walk.
     */
    private static void creativeSlot(ClientGameTestContext ctx, SceneWorld.Opened scene) {
        // Put the scene's totem in the slot first, so the screenshot shows it filled.
        ctx.runOnClient(mc -> mc.gameMode.handleContainerInput(
                mc.player.inventoryMenu.containerId, 36, 0, ContainerInput.QUICK_MOVE, mc.player));
        ctx.waitTicks(5);
        ctx.setScreen(() -> {
            Minecraft mc = Minecraft.getInstance();
            return new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), false);
        });
        ctx.waitForScreen(CreativeModeInventoryScreen.class);
        ctx.runOnClient(mc -> selectInventoryTab((CreativeModeInventoryScreen) mc.gui.screen()));
        ctx.waitTicks(5);
        ctx.takeScreenshot("totem-creative" + SUFFIX);
        ctx.runOnClient(mc -> {
            var slots = ((AbstractContainerScreen<?>) mc.gui.screen()).getMenu().slots;
            // The tab wraps the inventory menu's slots in order, by menu index: 7 and 8 are the
            // leggings and boots, the right-hand armor column.
            Slot legs = slots.get(7), feet = slots.get(8);
            Slot totem = totemSlot(slots);
            if (totem == null) throw new AssertionError("creative inventory tab: no totem slot");
            // Right of the armor, centred on its height: alone level with the middle of the
            // armor, or the lower of a pair straddling it.
            int middle = (legs.y + feet.y + 1) / 2;   // 19.5 rounds to 20, level with the offhand
            int expectY = ELYTRA ? middle + 9 : middle;
            if (totem.x != legs.x + 18 || totem.y != expectY) {
                throw new AssertionError("creative totem slot at (" + totem.x + ", " + totem.y + "), expected ("
                        + (legs.x + 18) + ", " + expectY + "): right of the armor, centred on it");
            }
            if (ELYTRA) {
                Slot elytra = createdSlot(slots, "simpleelytraslot:");
                if (elytra == null) throw new AssertionError("creative inventory tab: no elytra slot");
                assertAbove(elytra, totem, "elytra slot", "totem slot");
            }
        });
        ctx.setScreen(() -> null);
    }

    // The creative screen opens on its last tab; the Survival Inventory tab is chosen through
    // the screen's own (private) selectTab, the same call a click on the tab makes.
    private static void selectInventoryTab(CreativeModeInventoryScreen screen) {
        try {
            CreativeModeTab inventory = CreativeModeTabs.allTabs().stream()
                    .filter(t -> t.getType() == CreativeModeTab.Type.INVENTORY).findFirst().orElseThrow();
            Method select = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
            select.setAccessible(true);
            select.invoke(screen, inventory);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("could not select the creative Survival Inventory tab", e);
        }
    }

    private static void assertColumn(Minecraft mc, Slot offhand) {
        assertColumnIn(((AbstractContainerScreen<?>) mc.gui.screen()).getMenu().slots, offhand);
    }

    /** Totem directly above the offhand; with Simple Elytra Slot, elytra directly above the totem. */
    private static void assertColumnIn(java.util.List<Slot> slots, Slot offhand) {
        Slot totem = totemSlot(slots);
        if (totem == null) throw new AssertionError("no totem slot on this screen");
        assertAbove(totem, offhand, "totem slot", "offhand");
        if (ELYTRA) {
            Slot elytra = createdSlot(slots, "simpleelytraslot:");
            if (elytra == null) throw new AssertionError("Simple Elytra Slot is loaded but its slot isn't on this screen");
            assertAbove(elytra, totem, "elytra slot", "totem slot");
        }
    }

    private static void assertAbove(Slot upper, Slot lower, String upperName, String lowerName) {
        if (upper.x != lower.x || upper.y != lower.y - 18) {
            throw new AssertionError(upperName + " at (" + upper.x + ", " + upper.y + ") should sit directly above the "
                    + lowerName + " at (" + lower.x + ", " + lower.y + "), at (" + lower.x + ", " + (lower.y - 18) + ")");
        }
    }

    private static Slot totemSlot(java.util.List<Slot> slots) {
        return createdSlot(slots, TotemSlot.PANEL_ID);
    }

    private static Slot createdSlot(java.util.List<Slot> slots, String panelIdPrefix) {
        for (Slot s : slots) {
            CreatedSlot c = CreatedSlot.of(s);
            if (c != null && c.panelId().startsWith(panelIdPrefix)) return s;
        }
        return null;
    }

    private static ServerPlayer player(net.minecraft.server.MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }
}
