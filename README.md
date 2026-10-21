# Simple Totem Slot

Simple Totem Slot is a simple totem slot made for the Inventory Plus ecosystem. It adds one slot above your offhand for a totem of undying, so you can keep a totem ready and still use your offhand for a shield, a map, or anything else.

It has to be installed on the server as well as the client. In singleplayer, your game is both.

## Features

### The totem slot

- One slot directly above your offhand, with a faint totem outline when it's empty.
- It holds one totem of undying and nothing else.
- Shift-clicking a totem in your inventory puts it in the slot first.

### The death save

- The slot's totem saves you when you'd otherwise die, the same way a totem in your hand does.
- A totem in either hand is used first, then the one in the slot.

### Beside the hotbar

- The slot's totem shows in a small box left of your hotbar.

### Use totems anywhere

- When it's on, a totem anywhere in your inventory can save you after your hands and the slot. It's off by default.
- It can also look inside shulker boxes, inside bundles, and in your ender chest. Each of those is off by default too.

### With Inventory Plus

- Restock refills the slot from your inventory after its totem is used.
- The settings are on a Totem Slot tab in Inventory Plus's settings menu.

## Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MenuKit](https://modrinth.com/mod/menukit) 7.3.0 or newer, below 8.0.0, which draws the slot and the icon beside the hotbar.
- [SlotKit](https://modrinth.com/mod/slotkit) 2.0.0 or newer, below 3.0.0, which adds the slot to your inventory, saves it with your player on the server, and checks that a joining player has the mod.

## Pairs well with

- [Inventory Plus](https://modrinth.com/mod/inventory-plus-trevlar), my mod for restocking, sorting, and locking slots. With it, Restock refills the slot and Simple Totem Slot gets a settings tab.
- [Simple Elytra Slot](https://modrinth.com/mod/simple-elytra-slot), my mod with the same kind of slot for an elytra. Its slot sits above this one, in one column.

## Compatibility

It runs on Minecraft 26.3 with Fabric. Install it on both the client and the server. A server with Simple Totem Slot turns away players who don't have it, with a message naming the mods to install.

## How to use

### The slot

Open your inventory. The totem slot is directly above your offhand.

- Drag a totem of undying into it, or shift-click one in your inventory. Other items can't go in it.
- Shift-click the slot to move its totem into your inventory.
- The slot isn't shown while a chest or other container is open, and the totem stays in it.
- In creative, open the Survival Inventory tab. There the slot sits to the right of your armor.
- When you die, the slot's totem drops with the rest of your inventory, and it's kept when the keepInventory game rule is on.

The slot's totem is saved with your player on the server.

### The death save

When a hit would kill you, the slot's totem is used up instead. You're left with 1 health, you get the totem's effects and animation, and it counts for the Postmortal advancement. A totem in either hand is used before the slot's. Damage that ignores totems, like `/kill`, still kills you.

### Beside the hotbar

While the slot holds a totem, it shows in a small box left of your hotbar. With Simple Elytra Slot, the elytra's box sits to the left of the totem's.

### Use totems anywhere

With Use totems anywhere in your inventory turned on, a totem elsewhere in your inventory saves you after your hands and the slot. Your hotbar is checked first, then the rest of your inventory, then your ender chest if that box is ticked. Shulker boxes and bundles are searched too, at any depth, when their boxes are ticked.

### Settings

The settings are on the Totem Slot tab in Inventory Plus's settings menu. Without Inventory Plus there's no settings menu, and the defaults apply. They're saved in `config/simpletotemslot/config.json`.

| Setting on the Totem Slot tab | Default |
| --- | --- |
| Totem Slot (the switch) | On |
| Show the totem slot | On |
| Show a totem icon beside the hotbar | On |
| Show the Totem Slot button | Off |
| Use totems anywhere in your inventory | Off |
| Also inside shulker boxes | Off |
| Also inside bundles | Off |
| Also in your ender chest | Off |

- Turning Totem Slot off hides the slot and stops it from saving you. The totem stays in the slot.
- Turning off Show the totem slot hides the slot from your inventory, but a totem already in it still saves you. Nothing new goes in the slot, by shift-click or Restock, until you show it again.
- Show the Totem Slot button adds a Totem Slot switch to the column on the right of your inventory.
- The three Also boxes do nothing while Use totems anywhere in your inventory is off.
- Reset to Defaults on the tab, and Reset everything in Inventory Plus, put every setting here back.

## License

MIT.
