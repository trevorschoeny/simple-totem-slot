package com.trevorschoeny.simpletotemslot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple Totem Slot's settings, the one place they live: plain fields, loaded from and
 * saved to {@code config/simpletotemslot/config.json}, the same shape as Hotbar Pockets'.
 * Every setter saves.
 *
 * <p>On/Off is read on the side whose code runs: the server for the save and the
 * shift-click routing, the client for showing the slot. In singleplayer both read this
 * one file; on a dedicated server the server's own file decides the save, the "use totems
 * anywhere" options included.
 *
 * <p>Changed from the Totem Slot tab in Inventory Plus's settings menu when IP is
 * installed. Without IP there's no menu and the defaults (or the file) apply.
 */
public final class TotemSlotSettings {

    private TotemSlotSettings() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // The defaults, named once so load and reset agree.
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_HUD_ICON = true;
    private static final boolean DEFAULT_SHOW_BUTTON = false;
    // "Use totems anywhere" and where else to look: all off, so out of the box only the
    // hands and the slot save you.
    private static final boolean DEFAULT_ANYWHERE = false;
    private static final boolean DEFAULT_IN_SHULKERS = false;
    private static final boolean DEFAULT_IN_BUNDLES = false;
    private static final boolean DEFAULT_IN_ENDER_CHEST = false;

    // Off hides the slot and stops its save; the totem stays in the slot.
    private static volatile boolean enabled = DEFAULT_ENABLED;
    // The totem icon beside the hotbar while the slot holds one.
    private static volatile boolean hudIcon = DEFAULT_HUD_ICON;
    // Inventory Plus's column switch for this mod. Hidden by default.
    private static volatile boolean showButton = DEFAULT_SHOW_BUTTON;
    // After the hands and the slot, a totem anywhere in the inventory saves you too
    // (TotemSearch). The three below widen it, and do nothing while this is off.
    private static volatile boolean anywhere = DEFAULT_ANYWHERE;
    private static volatile boolean inShulkers = DEFAULT_IN_SHULKERS;
    private static volatile boolean inBundles = DEFAULT_IN_BUNDLES;
    private static volatile boolean inEnderChest = DEFAULT_IN_ENDER_CHEST;

    public static boolean enabled()    { return enabled; }
    public static boolean hudIcon()    { return hudIcon; }
    public static boolean showButton() { return showButton; }
    public static boolean anywhere()     { return anywhere; }
    public static boolean inShulkers()   { return inShulkers; }
    public static boolean inBundles()    { return inBundles; }
    public static boolean inEnderChest() { return inEnderChest; }

    public static void setEnabled(boolean v)    { enabled = v; save(); }
    public static void setHudIcon(boolean v)    { hudIcon = v; save(); }
    public static void setShowButton(boolean v) { showButton = v; save(); }
    public static void setAnywhere(boolean v)     { anywhere = v; save(); }
    public static void setInShulkers(boolean v)   { inShulkers = v; save(); }
    public static void setInBundles(boolean v)    { inBundles = v; save(); }
    public static void setInEnderChest(boolean v) { inEnderChest = v; save(); }

    /** Every setting back to its default, and saves: Reset to Defaults and IP's Reset everything. */
    public static void reset() {
        enabled = DEFAULT_ENABLED;
        hudIcon = DEFAULT_HUD_ICON;
        showButton = DEFAULT_SHOW_BUTTON;
        anywhere = DEFAULT_ANYWHERE;
        inShulkers = DEFAULT_IN_SHULKERS;
        inBundles = DEFAULT_IN_BUNDLES;
        inEnderChest = DEFAULT_IN_ENDER_CHEST;
        save();
    }

    private static Path filePath() {
        return FabricLoader.getInstance().getConfigDir().resolve(SimpleTotemSlot.MOD_ID).resolve("config.json");
    }

    /**
     * Reads the file once, at common init (both sides). A missing file, or a missing or
     * unreadable key, leaves that setting at its default. Nothing is written until a
     * setting changes.
     */
    public static void load() {
        Path path = filePath();
        if (!Files.exists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            if (root.has("enabled")) enabled = root.get("enabled").getAsBoolean();
            if (root.has("hudIcon")) hudIcon = root.get("hudIcon").getAsBoolean();
            if (root.has("showButton")) showButton = root.get("showButton").getAsBoolean();
            if (root.has("anywhere")) anywhere = root.get("anywhere").getAsBoolean();
            if (root.has("inShulkers")) inShulkers = root.get("inShulkers").getAsBoolean();
            if (root.has("inBundles")) inBundles = root.get("inBundles").getAsBoolean();
            if (root.has("inEnderChest")) inEnderChest = root.get("inEnderChest").getAsBoolean();
        } catch (IOException | JsonSyntaxException | IllegalStateException | ClassCastException
                 | UnsupportedOperationException ex) {
            SimpleTotemSlot.LOGGER.error("[simpletotemslot] failed to read {}, using defaults", path, ex);
        }
    }

    private static void save() {
        Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("enabled", enabled);
            root.addProperty("hudIcon", hudIcon);
            root.addProperty("showButton", showButton);
            root.addProperty("anywhere", anywhere);
            root.addProperty("inShulkers", inShulkers);
            root.addProperty("inBundles", inBundles);
            root.addProperty("inEnderChest", inEnderChest);
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException ex) {
            SimpleTotemSlot.LOGGER.error("[simpletotemslot] failed to write {}, settings won't persist", path, ex);
        }
    }
}
