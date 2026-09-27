# Smart Backpacks

Smart Backpacks is a NeoForge 1.21.1 mod by Team SmartStreamLabs that adds four backpack tiers with inventory stored directly on the item stack.

## Curios Support

- Curios is required and the backpacks are registered for the `back` slot.
- Sneak-right-click while holding a backpack to equip it on your back.
- Sneak-left-click while holding a backpack places it on the ground as a backpack block.
- Normal right-click still opens the backpack while it is in your hand.
- Press `B` to open the backpack while wearing it.
- Click the middle mouse button while the backpack screen is open to sort its contents.

## Architecture

- `SmartBackpacks` wires the mod together and registers items, menus, and the creative tab.
- `BackpackItem` is the item entry point. It opens the menu on right click and exposes tier-specific tooltip text.
- `BackpackMenu` owns the inventory UI, keeps the backpack's carrier slot locked while open, and handles shift-click routing.
- `BackpackInventory` is the persistence layer. It loads and saves contents from the backpack item stack using the built-in `DataComponents.CONTAINER` component.
- `BackpackTier` keeps version 1 tier data centralized so upgrades and extra backpack capabilities can slot in later without rewriting the menu logic.

## Version 2 Readiness

The code is intentionally split so later features can extend the same foundations:

- upgrades can attach extra data and logic around `BackpackTier` and `BackpackInventory`
- placed backpacks can reuse the slot rules and tier metadata
- dyes and custom colors can extend item rendering and extra item data
- utility upgrades can hook into the same item-bound storage format without changing existing backpack saves

## Building

Run `gradlew.bat build` from the project root to build the mod jar.
