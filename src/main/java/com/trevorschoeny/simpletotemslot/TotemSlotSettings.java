package com.trevorschoeny.simpletotemslot;

/**
 * Simple Totem Slot's settings, in one place so only this class changes when ConfigKit
 * lands. Until then each setting is a plain field at its default. With ConfigKit it becomes:
 * <pre>
 * public static final Settings S = Settings.of("simpletotemslot");
 * public static final Setting&lt;Boolean&gt; ENABLED = S.bool("enabled", true);      // .server() once ConfigKit has it
 * public static final Setting&lt;Boolean&gt; HUD_ICON = S.bool("hud_icon", true);
 * public static final Setting&lt;Boolean&gt; SHOW_BUTTON = S.bool("show_button", false); // listed only with Inventory Plus
 * </pre>
 */
public final class TotemSlotSettings {

    private TotemSlotSettings() {}

    // On/Off is read on the side whose code runs: the server for the save and the
    // shift-click routing, the client for showing the slot. Off hides the slot and
    // stops its save; the totem stays in the slot.
    private static volatile boolean enabled = true;
    // The totem icon beside the hotbar while the slot holds one.
    private static volatile boolean hudIcon = true;
    // Inventory Plus's column switch for this mod. Hidden by default.
    private static volatile boolean showButton = false;

    public static boolean enabled()    { return enabled; }
    public static boolean hudIcon()    { return hudIcon; }
    public static boolean showButton() { return showButton; }

    // Setters are not saved anywhere until ConfigKit, so a toggle lasts the session.
    public static void setEnabled(boolean v)    { enabled = v; }
    public static void setHudIcon(boolean v)    { hudIcon = v; }
    public static void setShowButton(boolean v) { showButton = v; }
}
