package dev.carefulharvest.test;

import static dev.carefulharvest.test.CarefulHarvestTestCases.TIMEOUT;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.carefulharvest.config.CarefulHarvestConfig;
import dev.carefulharvest.config.ConfigManager;
import dev.carefulharvest.config.ExemptSoil;
import dev.carefulharvest.test.CarefulHarvestTestCases.Case;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Config parsing and the config file. These tests only parse strings and use their own
 * temporary files, so they never touch the active config, except the reload command test,
 * which has an environment (batch) of its own and is put back by {@link ConfigEnvironment}.
 */
final class ConfigTestCases {
    private ConfigTestCases() {
    }

    static final List<Case> CASES = List.of(
            new Case("configDefaults", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configDefaults),
            new Case("configMissingKeysUseDefaults", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configMissingKeysUseDefaults),
            new Case("configUnknownKeysIgnored", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configUnknownKeysIgnored),
            new Case("configMalformedUsesDefaults", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configMalformedUsesDefaults),
            new Case("configWrongTypesUseDefaults", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configWrongTypesUseDefaults),
            new Case("configInvalidIdsSkipped", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configInvalidIdsSkipped),
            new Case("configTagEntries", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configTagEntries),
            new Case("configScreenEntrySplitting", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configScreenEntrySplitting),
            new Case("configFileCreatedWithDefaults", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configFileCreatedWithDefaults),
            new Case("configMalformedFileNotOverwritten", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configMalformedFileNotOverwritten),
            new Case("configSaveAndLoadRoundTrip", TIMEOUT, TestConfigs.DEFAULTS, ConfigTestCases::configSaveAndLoadRoundTrip),
            new Case("reloadCommandReadsFile", TIMEOUT, TestConfigs.RELOAD_COMMAND, ConfigTestCases::reloadCommandReadsFile));

    private static CarefulHarvestConfig parse(String json, List<String> warnings) {
        return CarefulHarvestConfig.parse(json, warnings::add);
    }

    private static void assertWarned(GameTestHelper helper, List<String> warnings, String fragment) {
        helper.assertTrue(warnings.stream().anyMatch(w -> w.contains(fragment)), "expected a warning containing \"" + fragment + "\" in " + warnings);
    }

    private static void withTempDir(GameTestHelper helper, Consumer<Path> body) {
        Path dir;
        try {
            dir = Files.createTempDirectory("carefulharvest-test");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            body.accept(dir);
        } finally {
            try (Stream<Path> files = Files.walk(dir)) {
                files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeFile(Path file, String text) {
        try {
            Files.writeString(file, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------------------------------------------------------------ parsing

    static void configDefaults(GameTestHelper helper) {
        CarefulHarvestConfig d = CarefulHarvestConfig.DEFAULT;
        helper.assertFalse(d.nonSneakingBreak() || d.fluids() || d.explosions() || d.supportBroken() || d.other(), "all toggles default to false");
        helper.assertValueEqual(d.exemptSoil().entries(), List.of("minecraft:moss_block", "minecraft:pale_moss_block",
                "minecraft:crimson_nylium", "minecraft:warped_nylium"), "default exempt soil");
        for (Block block : new Block[] {Blocks.MOSS_BLOCK, Blocks.PALE_MOSS_BLOCK, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM}) {
            helper.assertTrue(d.exemptSoil().matches(block.defaultBlockState()), block + " is exempt by default");
        }
        helper.assertFalse(d.exemptSoil().matches(Blocks.GRASS_BLOCK.defaultBlockState()), "grass is not exempt");
        List<String> warnings = new ArrayList<>();
        helper.assertValueEqual(parse(d.toJson(), warnings), d, "defaults round trip");
        helper.assertValueEqual(warnings, List.of(), "no warnings for the default file");
        helper.succeed();
    }

    static void configMissingKeysUseDefaults(GameTestHelper helper) {
        List<String> warnings = new ArrayList<>();
        helper.assertValueEqual(parse("{}", warnings), CarefulHarvestConfig.DEFAULT, "empty object");
        for (String key : List.of("nonSneakingBreak", "fluids", "explosions", "supportBroken", "other", "exemptSoil")) {
            assertWarned(helper, warnings, key);
        }
        warnings.clear();
        CarefulHarvestConfig partial = parse("{\"fluids\": true, \"other\": true}", warnings);
        helper.assertValueEqual(partial, CarefulHarvestConfig.DEFAULT.withFluids(true).withOther(true), "partial file");
        helper.assertValueEqual(warnings.size(), 4, "one warning per missing key");
        helper.succeed();
    }

    static void configUnknownKeysIgnored(GameTestHelper helper) {
        List<String> warnings = new ArrayList<>();
        CarefulHarvestConfig config = parse("""
                {"explosions": true, "shears": true, "nonSneakingBreak": false, "fluids": false,
                 "supportBroken": false, "other": false, "exemptSoil": [], "nested": {"a": 1}}""", warnings);
        helper.assertValueEqual(config, CarefulHarvestConfig.DEFAULT.withExplosions(true).withExemptSoil(ExemptSoil.NONE), "known keys still read");
        helper.assertValueEqual(warnings.size(), 2, "one warning per unknown key: " + warnings);
        assertWarned(helper, warnings, "shears");
        assertWarned(helper, warnings, "nested");
        helper.succeed();
    }

    static void configMalformedUsesDefaults(GameTestHelper helper) {
        for (String text : List.of("", "   ", "{ not json", "{\"fluids\": true", "[true]", "42", "\"text\"", "null",
                "{\"fluids\": true} trailing", "\u0000\u0001")) {
            List<String> warnings = new ArrayList<>();
            helper.assertValueEqual(parse(text, warnings), CarefulHarvestConfig.DEFAULT, "malformed " + text);
            helper.assertTrue(warnings.size() == 1, "one warning for malformed " + text + ": " + warnings);
        }
        helper.succeed();
    }

    static void configWrongTypesUseDefaults(GameTestHelper helper) {
        List<String> warnings = new ArrayList<>();
        CarefulHarvestConfig config = parse("""
                {"nonSneakingBreak": "yes", "fluids": 1, "explosions": null, "supportBroken": true, "other": [true],
                 "exemptSoil": "minecraft:dirt"}""", warnings);
        helper.assertValueEqual(config, CarefulHarvestConfig.DEFAULT.withSupportBroken(true), "bad values replaced by defaults");
        helper.assertValueEqual(warnings.size(), 5, "one warning per bad value: " + warnings);
        assertWarned(helper, warnings, "exemptSoil");
        helper.succeed();
    }

    static void configInvalidIdsSkipped(GameTestHelper helper) {
        List<String> warnings = new ArrayList<>();
        CarefulHarvestConfig config = parse("""
                {"nonSneakingBreak": false, "fluids": false, "explosions": false, "supportBroken": false, "other": false,
                 "exemptSoil": ["minecraft:moss_block", "Not An Id!", "#bad tag", "", "#", 5, null,
                                "minecraft:no_such_block", " minecraft:crimson_nylium "]}""", warnings);
        ExemptSoil soil = config.exemptSoil();
        helper.assertValueEqual(warnings.size(), 6, "syntax warnings: " + warnings);
        assertWarned(helper, warnings, "Not An Id!");
        assertWarned(helper, warnings, "#bad tag");
        List<String> resolveWarnings = new ArrayList<>();
        helper.assertValueEqual(soil.resolveBlocks(resolveWarnings::add).size(), 2, "valid blocks found");
        helper.assertValueEqual(resolveWarnings.size(), 1, "unknown block reported: " + resolveWarnings);
        assertWarned(helper, resolveWarnings, "minecraft:no_such_block");
        helper.assertTrue(soil.matches(Blocks.MOSS_BLOCK.defaultBlockState()), "moss still matches");
        helper.assertTrue(soil.matches(Blocks.CRIMSON_NYLIUM.defaultBlockState()), "entries are trimmed");
        helper.assertFalse(soil.matches(Blocks.AIR.defaultBlockState()), "air does not match");
        helper.assertFalse(soil.matches(Blocks.DIRT.defaultBlockState()), "dirt does not match");
        // Invalid entries are kept for saving, so a typo in the file is not silently deleted.
        helper.assertTrue(soil.entries().contains("Not An Id!"), "invalid entries kept in the list");
        helper.succeed();
    }

    static void configTagEntries(GameTestHelper helper) {
        List<String> warnings = new ArrayList<>();
        ExemptSoil soil = ExemptSoil.parse(List.of("#minecraft:dirt", "#minecraft:no_such_tag", "#c:nonexistent", "minecraft:moss_block"), warnings::add);
        helper.assertValueEqual(warnings, List.of(), "tags are only checked for syntax");
        helper.assertValueEqual(soil.tags().size(), 3, "three tags");
        for (Block block : new Block[] {Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.MOSS_BLOCK}) {
            helper.assertTrue(soil.matches(block.defaultBlockState()), block + " matches");
        }
        for (Block block : new Block[] {Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.CRIMSON_NYLIUM}) {
            helper.assertFalse(soil.matches(block.defaultBlockState()), block + " does not match");
        }
        ExemptSoil nylium = ExemptSoil.parse(List.of("#minecraft:nylium"), warnings::add);
        helper.assertTrue(nylium.matches(Blocks.WARPED_NYLIUM.defaultBlockState()), "#minecraft:nylium matches warped nylium");
        helper.succeed();
    }

    static void configScreenEntrySplitting(GameTestHelper helper) {
        helper.assertValueEqual(ExemptSoil.splitEntries(" minecraft:moss_block \n\n#minecraft:dirt, minecraft:stone ,\n"),
                List.of("minecraft:moss_block", "#minecraft:dirt", "minecraft:stone"), "lines and commas");
        helper.assertValueEqual(ExemptSoil.splitEntries(""), List.of(), "empty text");
        helper.succeed();
    }

    // --------------------------------------------------------------------- file

    static void configFileCreatedWithDefaults(GameTestHelper helper) {
        withTempDir(helper, dir -> {
            Path file = dir.resolve("sub").resolve(ConfigManager.FILE_NAME);
            ConfigManager.LoadResult result = ConfigManager.load(file, true);
            helper.assertTrue(result.createdFile(), "file created");
            helper.assertValueEqual(result.config(), CarefulHarvestConfig.DEFAULT, "defaults");
            helper.assertValueEqual(result.warnings(), List.of(), "no warnings");
            String text = read(file);
            helper.assertValueEqual(text, CarefulHarvestConfig.DEFAULT.toJson(), "default JSON written");
            helper.assertTrue(text.contains("\"nonSneakingBreak\": false") && text.contains("\"minecraft:moss_block\""), "readable JSON: " + text);
            ConfigManager.LoadResult again = ConfigManager.load(file, true);
            helper.assertFalse(again.createdFile(), "not created twice");
            helper.assertValueEqual(again.config(), CarefulHarvestConfig.DEFAULT, "reads back");
        });
        helper.succeed();
    }

    static void configMalformedFileNotOverwritten(GameTestHelper helper) {
        withTempDir(helper, dir -> {
            Path file = dir.resolve(ConfigManager.FILE_NAME);
            String garbage = "{ \"fluids\": tru";
            writeFile(file, garbage);
            ConfigManager.LoadResult result = ConfigManager.load(file, true);
            helper.assertValueEqual(result.config(), CarefulHarvestConfig.DEFAULT, "defaults");
            helper.assertValueEqual(result.warnings().size(), 1, "one warning");
            helper.assertValueEqual(read(file), garbage, "file left alone so it can be fixed");
        });
        helper.succeed();
    }

    static void configSaveAndLoadRoundTrip(GameTestHelper helper) {
        withTempDir(helper, dir -> {
            Path file = dir.resolve(ConfigManager.FILE_NAME);
            CarefulHarvestConfig config = CarefulHarvestConfig.DEFAULT.withNonSneakingBreak(true).withSupportBroken(true)
                    .withExemptSoil(ExemptSoil.parse(List.of("#minecraft:dirt", "minecraft:gravel"), w -> {
                    }));
            try {
                ConfigManager.write(file, config);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            ConfigManager.LoadResult result = ConfigManager.load(file, true);
            helper.assertValueEqual(result.config(), config, "round trip");
            helper.assertValueEqual(result.warnings(), List.of(), "no warnings");
            helper.assertFalse(Files.exists(dir.resolve(ConfigManager.FILE_NAME + ".tmp")), "temp file cleaned up");
        });
        helper.succeed();
    }

    // ------------------------------------------------------------------ command

    /**
     * {@code /carefulharvest reload} re-reads the file and applies it; it needs permission level 2.
     * Runs in its own batch; {@link ConfigEnvironment} restores the config and file location.
     */
    static void reloadCommandReadsFile(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack admin = server.createCommandSourceStack().withSuppressedOutput();
        CommandSourceStack nobody = admin.withPermission(PermissionSet.NO_PERMISSIONS);
        withTempDir(helper, dir -> {
            Path file = dir.resolve(ConfigManager.FILE_NAME);
            ConfigManager.setPath(file);
            writeFile(file, "{\"fluids\": true, \"exemptSoil\": [\"#minecraft:dirt\", \"minecraft:no_such_block\"]}");
            helper.assertFalse(ConfigManager.get().fluids(), "starts with the default config");
            boolean denied = false;
            try {
                server.getCommands().getDispatcher().execute("carefulharvest reload", nobody);
            } catch (CommandSyntaxException e) {
                denied = true;
            }
            helper.assertTrue(denied, "players without permission cannot reload");
            helper.assertFalse(ConfigManager.get().fluids(), "not reloaded without permission");
            try {
                helper.assertValueEqual(server.getCommands().getDispatcher().execute("carefulharvest reload", admin), 1, "command result");
                helper.assertValueEqual(server.getCommands().getDispatcher().execute("carefulharvest", admin), 1, "show result");
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException(e);
            }
            CarefulHarvestConfig active = ConfigManager.get();
            helper.assertTrue(active.fluids(), "fluids read from the file");
            helper.assertFalse(active.explosions(), "missing keys default");
            helper.assertTrue(active.exemptSoil().matches(Blocks.DIRT.defaultBlockState()), "tag entry applied");
            helper.assertFalse(active.exemptSoil().matches(Blocks.MOSS_BLOCK.defaultBlockState()), "list replaced");
            // A broken file falls back to the defaults (and is not overwritten).
            writeFile(file, "{oops");
            try {
                server.getCommands().getDispatcher().execute("carefulharvest reload", admin);
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException(e);
            }
            helper.assertValueEqual(ConfigManager.get(), CarefulHarvestConfig.DEFAULT, "malformed file gives defaults");
            helper.assertValueEqual(read(file), "{oops", "malformed file kept");
        });
        helper.succeed();
    }
}
