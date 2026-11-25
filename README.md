# Careful Harvest

A small server-side Minecraft Java Edition mod (Fabric and NeoForge) that makes decorative plants
drop **only when a player breaks them while sneaking**. Each other way of destroying a plant can be
switched back to normal drops in a config file, and plants growing on certain soils (moss and nylium
by default) are left alone completely.

- Mod id: `carefulharvest` - Licence: MIT - Version 1.1.0
- Minecraft **26.3**, Java **25**
- Loaders: Fabric (Loader 0.20.1+, Fabric API) and NeoForge 26.3.0.118+
- Optional: [Mod Menu](https://modrinth.com/mod/modmenu) 21.x on Fabric, for the config screen

## Behaviour

With the default config, blocks in the block tag `carefulharvest:careful_harvest` drop their loot
only if a player who is holding sneak (shift) at that moment breaks them. Otherwise the block is
destroyed and drops nothing. That includes:

- a player breaking the block without sneaking (whatever they are holding)
- explosions (TNT, creepers, beds, respawn anchors, wind charges, ...), even when a sneaking
  player caused the explosion
- water or lava flowing onto the block
- the block losing its support (for example, the dirt under a flower is broken), even when a
  sneaking player broke the block underneath
- pistons, enderman deaths, and any other mod or vanilla path that turns the block into drops,
  even if it names a sneaking player as the breaker, unless it goes through the normal player
  block-breaking code

Every one of these can be turned back on in the [config](#configuration), and plants on
[exempt soil](#exempt-soil) always behave like vanilla.

Other rules:

- **The vanilla loot tables still apply on top of this.** A sneaking player still needs shears to
  get short grass, ferns, and so on, exactly as in vanilla. The same goes for any cause that is
  switched on: the loot table runs normally for it.
- **Double-tall plants** (sunflower, lilac, tall grass, ...) drop once, from either half, just as in
  vanilla. Both halves always get the same cause.
- **Creative mode is unchanged.** Breaking blocks in creative never drops anything.
- Blocks that are not in the tag are not affected.
- `/loot ... mine` returns nothing for tagged blocks unless `other` is on, because it does not
  break the block.

### Default tag contents

`#minecraft:small_flowers` (dandelion, golden dandelion, poppy, the tulips, eyeblossoms, torchflower,
wither rose, ...), sunflower, lilac, rose bush, peony, pitcher plant, pink petals, wildflowers,
leaf litter, short grass, tall grass, fern, large fern, dead bush, bush, firefly bush, short dry
grass, tall dry grass, cactus flower and red shrub.

Saplings, crops, leaves and potted plants are not included. Every entry that a version of the game
might not have uses `"required": false`, so the tag still loads if one is missing.

## Customising the tag

The tag is plain data, so you can change it with any datapack.

**Add blocks.** Create
`data/carefulharvest/tags/block/careful_harvest.json` in a datapack:

```json
{
  "values": [
    "minecraft:moss_carpet",
    "minecraft:glow_lichen",
    { "id": "somemod:fancy_flower", "required": false }
  ]
}
```

**Replace the whole list.** Set `"replace": true`. For example, to limit the mod to tulips only:

```json
{
  "replace": true,
  "values": [
    "minecraft:red_tulip",
    "minecraft:orange_tulip",
    "minecraft:white_tulip",
    "minecraft:pink_tulip"
  ]
}
```

**Turn the mod off** for a world: use `"replace": true` with an empty `"values": []`.

Run `/reload` after editing a datapack.

## Configuration

The config file is `config/carefulharvest.json` (the loader's config folder, in the game or server
directory). It is created with the defaults on first start:

```json
{
  "nonSneakingBreak": false,
  "fluids": false,
  "explosions": false,
  "supportBroken": false,
  "other": false,
  "exemptSoil": [
    "minecraft:moss_block",
    "minecraft:pale_moss_block",
    "minecraft:crimson_nylium",
    "minecraft:warped_nylium"
  ]
}
```

Each boolean answers "does this cause produce drops?". `false` (the default for all of them) means
the plant drops nothing; `true` means the vanilla loot table runs normally. Breaking a plant while
sneaking always drops and has no switch.

| Key                | Cause                                                                                   |
|--------------------|-----------------------------------------------------------------------------------------|
| `nonSneakingBreak` | A player breaks the plant while not sneaking, with any item or none.                    |
| `fluids`           | Water or lava flows into the plant's block. (Vanilla lava destroys plants without dropping anything, so for lava this changes nothing.) |
| `explosions`       | An explosion destroys the plant. A plant inside the blast counts as an explosion drop even if the explosion happened to destroy its soil first. |
| `supportBroken`    | The plant pops off because the block it stands on was removed, by anything: a player (sneaking or not), an explosion that does not reach the plant itself, a piston pulling the soil away, an enderman picking it up, ... |
| `other`            | Everything else: pistons pushing into the plant, endermen dying with a carried plant, `/loot ... mine`, other mods' code calling `Level#destroyBlock`, ... |

The file is forgiving:

- A missing file is created with the defaults.
- A file that is not valid JSON (or not a JSON object) is logged as a warning and the defaults are
  used. The file is not overwritten, so you can fix it.
- Missing keys use their default, unknown keys are ignored, and values of the wrong type use their
  default; each is logged as a warning.
- Invalid entries in `exemptSoil` (not a valid id, not a string, or a block id that no block has)
  are logged as warnings and skipped.

### Exempt soil

`exemptSoil` is a list of block ids (`minecraft:moss_block`) and block tags (`#minecraft:nylium`).
A tagged plant whose **soil** matches the list is completely unaffected by the mod: it drops exactly
as in vanilla, for every cause. Use an empty list (`[]`) to exempt nothing.

- The soil is the block directly below the plant. For the upper half of a double-tall plant it is
  the block below the lower half, so both halves agree.
- **When the soil itself is what was removed** (you break the moss under a flower, or an explosion
  blows it up), the moss is already gone by the time the flower pops off. The mod remembers, for the
  rest of that server tick, which exempt soil blocks were replaced (per dimension), and still treats
  the plant as standing on moss. So a flower growing on moss drops normally when its moss is broken,
  even with `supportBroken` off. Soil that was removed in an earlier tick, soil next to the plant,
  and removed soil that has already been replaced by another block (or had a new plant put on its
  spot) in the same tick do not count.
- Tags are only checked when used, so a tag that does not exist (or is not loaded yet) simply
  matches nothing. Block ids are looked up the first time the list is used, so blocks from other
  mods work.

### Changing the config

- **`/carefulharvest reload`** re-reads the file and applies it straight away, printing any warnings.
  `/carefulharvest` on its own shows the active settings. Both need permission level 2 (game master,
  the same as `/reload`).
- **Config screen:** on Fabric, open it from [Mod Menu](https://modrinth.com/mod/modmenu)
  (Mods, Careful Harvest, the config button); on NeoForge, from the mods list (Mods, Careful Harvest,
  Config). It has a switch for each cause and an editable list of exempt soil (one entry per line or
  comma-separated); invalid entries are shown below the list. **Done** saves the file and applies it
  immediately; **Cancel** discards the changes. The screen is built from vanilla widgets only, so no
  config library is needed. Mod Menu is optional: without it everything works, there is just no
  button for the screen on Fabric.
- The config is server logic. In singleplayer the screen edits the config the game is using. On a
  dedicated server, edit the server's `config/carefulharvest.json` and run `/carefulharvest reload`
  (the screen in your own game only changes your local file).

## Installation

The mod only has to be installed **on the server**, or in your own game for singleplayer.
Clients connecting to a server do not need it, and vanilla clients can join. All the logic runs on
the logical server, and the mod adds no blocks, items or network packets. The config screen is
client-only code that is only loaded from the client entrypoints (Mod Menu's entrypoint on Fabric,
a `Dist.CLIENT` mod class on NeoForge), so the same jar runs on dedicated servers.

- Fabric: put `carefulharvest-fabric-26.3-<version>.jar` and Fabric API into `mods/`
  (and Mod Menu, if you want the config screen).
- NeoForge: put `carefulharvest-neoforge-26.3-<version>.jar` into `mods/`.

## How it works

The common code holds all the logic. Every vanilla drop path for a block (`Block.dropResources`
overloads, explosions, pistons, fluids, `Level.destroyBlock`, endermen) evaluates its loot through
`BlockBehaviour.BlockStateBase#getDrops(LootParams.Builder)`. A mixin there asks
`CarefulHarvest.shouldSuppressDrops` whether to return an empty list for a tagged block:

1. If the plant stands on [exempt soil](#exempt-soil), never.
2. Otherwise `DestructionContext` works out the cause, and the drops are suppressed unless the config
   allows that cause.

**Cause classification.** Mixins push a frame on a per-thread stack for the duration of each code
path that destroys blocks (checked against the 26.3 code of both loaders; NeoForge patches
`ServerPlayerGameMode#destroyBlock`, `BlockStateBase#onExplosionHit` and `Level#setBlock` but keeps
these entry points):

| Frame | Wrapped method | Cause |
|-------|----------------|-------|
| player break | `ServerPlayerGameMode#destroyBlock` (every survival break; records whether the player is sneaking) | sneaking / non-sneaking break, for exactly that block |
| explosion | `ServerExplosion#interactWithBlocks` (with the list of blown-up positions) and `BlockStateBase#onExplosionHit` | explosion |
| fluid | `FlowingFluid#spreadTo` (water drops the block's loot there, lava only fizzes) | fluids |
| shape destroy | `Block#updateOrDestroy` when a neighbour change turns a block into air | lost support, see below |

When loot is evaluated, the innermost frame decides. A shape-destroy frame is the remaining half of a
double-tall plant if the frame directly outside it is for the plant's other half; then the half
takes the cause of that frame, which is how both halves always agree (vanilla drops the item from
whichever half has the loot). Any other shape destroy is `supportBroken`, unless an explosion being
processed includes the plant's position: explosions handle their blocks in random order, and a
plant in the blast should count as an explosion drop whether or not its soil was handled first.
Something destroyed during a player break that is not that block or its other half, and loot with no
frame at all, is `other`.

**Removed soil.** A mixin in `LevelChunk#setBlockState` (every `Level#setBlock` on both loaders), at
the point where the chunk section swaps the block, records exempt soil blocks that get replaced. This
is one of the hottest paths in the game, so it does nothing unless the block actually changes on a
server level, and costs a set lookup (plus one tag check per `#tag` entry) only when the exempt soil
list is not empty. Entries are keyed by level and position, are forgotten as soon as something else
is put at that position or a tagged plant is put on top of it, and are all dropped at the end of every
server tick (a mixin on `MinecraftServer#tickServer`). Vanilla pops unsupported plants in the same
tick, so the soil rule can still see the moss that was just broken. World generation writes to proto
chunks and client levels are skipped, so neither touches it.

## Building

You need any JDK 17 or newer to start the Gradle wrapper (tested with 21). The build provisions JDK 25 itself, both for the Gradle
daemon (`gradle/gradle-daemon-jvm.properties`) and for compiling (through the foojay toolchain
resolver), so you do not have to install it.

```sh
./gradlew build
```

The jars are written to:

- `fabric/build/libs/carefulharvest-fabric-26.3-1.1.0.jar`
- `neoforge/build/libs/carefulharvest-neoforge-26.3-1.1.0.jar`

`./gradlew build` also runs the server game tests on both loaders. The test cases are
loader-independent (`common/src/gametest`) and are registered by a small development-only test mod
per loader (`fabric/src/gametest`, `neoforge/src/gametest`), which is not part of the release jars.
They cover the default behaviour (sneaking and non-sneaking breaks, creative, shears, both halves of
double plants, explosions including TNT lit by a sneaking player, water, pistons, lost support,
endermen, side effects of a sneaking break), each config switch on its own (its cause drops like
vanilla, all other causes still drop nothing), exempt soil (moss, pale moss and both nyliums, for
player breaks, explosions, water, pistons and removed soil, double plants broken from either half),
`#tag` entries and an empty list, config parsing and the config file, and `/carefulharvest reload`.
`CauseTestCases` covers the nested cases: a player (sneaking or not) breaking the soil under single
and double plants with every other switch on, the explosion orderings (replayed deterministically
with the exact cause markers the mixins push, plus repeated real blasts over a grid of plants on
dirt), the marker stack being empty after every path including a refused break, and the removed-soil
memory (forgotten when the soil is replaced or a plant is placed on it, cleared after the tick, kept
apart per dimension, never filled with an empty list). Item counts only look inside each test's own
area, so neighbouring tests of the same batch cannot affect them.

Tests that need a different config run in their own test environment
(`carefulharvest_gametest:<name>`, see `TestConfigs`): the game test runner runs one environment's
batch at a time, sets that config up before the batch and restores the previous one afterwards, so
tests with different configs never run at the same time. The test mod also adds
`minecraft:crimson_roots` to the tag, because no plant in the default tag can grow on nylium.

Run only the game tests with `./gradlew :fabric:runGameTest :neoforge:runGameTestServer`, or skip
them with `./gradlew build -x runGameTest -x runGameTestServer`. JUnit-style reports are written to
`<loader>/build/reports/gametest/`.

`./gradlew build` also runs a plain JUnit test per loader (`<loader>/src/test`) that loads and
verifies the client-only classes. On Fabric there is also a client game test that opens the screen
through the Mod Menu entrypoint, clicks a switch, types an exempt soil entry, resizes the window,
saves, reopens and cancels. It needs a display, so it is not part of the build. Headless, with Mesa's
software renderer and Xvfb:

```sh
SDL_VIDEO_FORCE_EGL=1 XDG_RUNTIME_DIR=/tmp xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew :fabric:runClientGameTest
```

Development runs: `./gradlew :fabric:runClient`, `:fabric:runServer`, `:neoforge:runClient`,
`:neoforge:runServer`.

### Project layout

The project follows the
[MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) layout:

| Project    | Contents                                                                   |
|------------|----------------------------------------------------------------------------|
| `common`   | All logic, the mixins, the config, the command, the config screen (`dev.carefulharvest.client`), the tag, and the shared game test cases. Compiled against vanilla via NeoForm. |
| `fabric`   | `fabric.mod.json`, the entrypoints (main, Mod Menu) and the Fabric test mod. Built with Fabric Loom; Mod Menu is a compile-only dependency. |
| `neoforge` | `neoforge.mods.toml`, the `@Mod` classes (common and client) and the NeoForge test mod. Built with ModDevGradle. |

## Licence

MIT. See [LICENSE](LICENSE).
