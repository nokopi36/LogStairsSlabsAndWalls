# Log Stairs, Slabs and Walls

<img src="src/main/resources/logo.png" alt="Log Stairs, Slabs and Walls" width="200">

[日本語](README.ja.md)

Log stairs, slabs, walls and panels — strip the bark one face at a time.

A Minecraft mod that adds log-textured stairs, slabs, walls and full-block log panels for every vanilla wood type.
Use an axe on a face to strip only that face, so a one-block-thick wall or roof can show bark on one side and stripped wood on the other.

> **Download:** [GitHub Releases](https://github.com/nokopi36/LogStairsSlabsAndWalls/releases). See the [changelog](CHANGELOG.md) for supported versions.
>
> Sister mod: [Log Fences and Gates](https://www.curseforge.com/minecraft/mc-mods/log-fences-and-gates) (log fences and fence gates with per-part bark stripping).

## Features

- **Log panels, stairs, slabs and walls** for 13 wood types: oak, spruce, birch, jungle, acacia, dark oak, mangrove, cherry, pale oak, poplar, bamboo block, crimson stem and warped stem.
  - Log panels are full blocks that you can place upright or sideways, like vanilla logs. They count as logs of their wood type, so one panel crafts into planks.
  - Stairs, slabs and walls look like an upright log (rings on top and bottom, bark on the sides). They connect and stack like their vanilla counterparts, and walls connect to vanilla walls.
- **Per-face bark stripping.** Right-click with any axe to strip only the face you clicked. Faces follow the world direction, so every face pointing the same way is stripped together.
  - Log panel: 6 faces
  - Stairs: 8 faces. The lower tread and the upper riser are separate from the outer faces, so each step can look different.
  - Slab: 6 faces. On a double slab, the upper and lower halves keep their own bark, so you can stack a stripped slab on a bark slab.
  - Wall: 6 directions (post and wall sections together)
  - Clicking a face that is already stripped does nothing and does not use the axe.
- **Stripped variants.** Craft from stripped logs (or stripped log panels) to get blocks that are fully stripped when placed.
  Breaking a fully stripped block drops the stripped variant; a partly stripped one drops the normal item. Each half of a double slab drops on its own.
- Rotating or mirroring a structure (structure blocks, `/place template`) rotates the stripped faces along with the block.
- Same stats as vanilla logs: hardness 2.0, log sounds, fast to break with an axe. Nether stems do not burn, like vanilla. Panels, stairs and walls burn as fuel for 300 ticks, slabs for 150.
- Uses the vanilla log textures, so it also works with resource packs.

## Recipes

| Result | Recipe |
|---|---|
| Log Panel ×2 | 2 logs stacked vertically (works in the 2×2 inventory grid) |
| Log ×2 | 2 log panels stacked vertically |
| Log Stairs ×16 | 6 logs or log panels in a stair shape |
| Log Slab ×24 | 3 logs or log panels in a row |
| Log Wall ×24 | `L L L` / `L L L` — L = log or log panel |
| Bamboo Block Stairs ×8 / Slab ×12 / Wall ×12 | Same shapes with blocks of bamboo |
| Stripped variants | Same shapes with stripped logs or stripped log panels |

Logs and log panels can be mixed. The counts match the vanilla plank recipes (1 log = 4 planks, 1 block of bamboo = 2 planks); walls follow the vanilla stone wall recipe (6 → 6) converted the same way.

## Requirements

- NeoForge. See the [changelog](CHANGELOG.md) for the supported Minecraft and NeoForge versions of each release.
- Required on both the client and the server.

## Building

```sh
./gradlew build
```

The jar is written to `build/libs/logstairsslabsandwalls-neoforge-<Minecraft version>-<mod version>.jar`. Gradle downloads Java 25 automatically through its toolchain support.

Other useful tasks:

- `./gradlew runClient` — start a development client
- `./gradlew runData` — regenerate models, language files, tags, loot tables and recipes into `src/generated/`
- `./gradlew runGameTestServer` — run the GameTests

## License

[MIT](LICENSE)
