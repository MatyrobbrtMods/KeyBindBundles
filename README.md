# KeyBind Bundles Rebound

Too many keybinds and not enough keys? KeyBind Bundles Rebound lets you put a bunch of keybinds behind one key and pick between them with a radial menu.

This is a fork of [KeyBind Bundles](https://www.curseforge.com/minecraft/mc-mods/keybind-bundles) by Matyrobbrt, continued with some extra features and ports to more Minecraft versions. It's client-side only, so servers don't need it.

## Making a bundle

1. Open the Key Binds screen and scroll to the KeyBind Bundles category.
2. Click **Add key bundle** and give it a name.
3. Click **Edit** on your new bundle to add entries to its radial menu, then choose which keybind each entry triggers.
4. Give each entry a name and an item icon if you like.

## Using a bundle

- Back out of the edit screen and bind the bundle to a key.
- In game, press the bundle key to open the radial menu, then hover an entry and click it to fire that keybind.
- Right-click an entry to bookmark it. With a bookmark set, pressing the bundle key fires the bookmarked keybind right away. Hold the radial menu modifier (Left Alt by default) to open the menu instead. Bookmarked entries show up red.

## What's new in Rebound

- Toggle mode for the radial menu (on by default), with a config option to go back to hold-to-open.
- Space or Enter activates the hovered entry, and the keybind stays held until you let go of the key.
- Alternating segment colors, orange for the hovered entry and red for the bookmarked one.
- Bundle keybinds show their real names in other mods' keybind lists.
- Builds for Minecraft 1.20.1 (Forge), 1.21.1 (NeoForge), and 26.1 (NeoForge).

## Compatibility

Works with [Controlling](https://www.curseforge.com/minecraft/mc-mods/controlling).

Rebound uses the mod ID `kbbrebound`, so it's a separate mod from the original. Don't install both. Your bundles carry over, since Rebound reads the same `keybind_bundles.json` file, but you'll need to rebind the bundle keys.

## Bugs and suggestions

Open an issue at https://github.com/katubug/KeyBindBundles/issues.

## License

MIT. Original mod by Matyrobbrt, see [LICENSE](LICENSE).
