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
 * one file; on a dedicated server the server's own file decides the save.
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

    // Off hides the slot and stops its save; the totem stays in the slot.
    private static volatile boolean enabled = DEFAULT_ENABLED;
    // The totem icon beside the hotbar while the slot holds one.
    private static volatile boolean hudIcon = DEFAULT_HUD_ICON;
    // Inventory Plus's column switch for this mod. Hidden by default.
    private static volatile boolean showButton = DEFAULT_SHOW_BUTTON;

    public static boolean enabled()    { return enabled; }
    public static boolean hudIcon()    { return hudIcon; }
    public static boolean showButton() { return showButton; }

    public static void setEnabled(boolean v)    { enabled = v; save(); }
    public static void setHudIcon(boolean v)    { hudIcon = v; save(); }
    public static void setShowButton(boolean v) { showButton = v; save(); }

    /** Every setting back to its default, and saves: Reset to Defaults and IP's Reset everything. */
    public static void reset() {
        enabled = DEFAULT_ENABLED;
        hudIcon = DEFAULT_HUD_ICON;
        showButton = DEFAULT_SHOW_BUTTON;
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
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException ex) {
            SimpleTotemSlot.LOGGER.error("[simpletotemslot] failed to write {}, settings won't persist", path, ex);
        }
    }
}
