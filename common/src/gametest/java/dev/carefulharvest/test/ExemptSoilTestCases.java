package dev.carefulharvest.test;

import static dev.carefulharvest.test.CarefulHarvestTestCases.PLANT;
import static dev.carefulharvest.test.CarefulHarvestTestCases.TIMEOUT;

import dev.carefulharvest.test.CarefulHarvestTestCases.Case;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Exempt soil, under the default config (all toggles off, default soil list): plants on moss or
 * nylium drop like vanilla for every cause, including when the soil itself is what was removed.
 * The game test mod adds {@code minecraft:crimson_roots} to the tag (test data only), because no
 * plant in the default tag can stand on nylium.
 */
final class ExemptSoilTestCases {
    private ExemptSoilTestCases() {
    }

    static final List<Case> CASES = List.of(
            new Case("exemptMossNonSneakingBreakDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossNonSneakingBreakDrops),
            new Case("exemptMossExplosionDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossExplosionDrops),
            new Case("exemptMossWaterDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossWaterDrops),
            new Case("exemptMossSneakingSoilRemovedDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossSneakingSoilRemovedDrops),
            new Case("exemptMossNonSneakingSoilRemovedDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossNonSneakingSoilRemovedDrops),
            new Case("exemptMossPistonDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossPistonDrops),
            new Case("exemptPaleMossNonSneakingBreakDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptPaleMossNonSneakingBreakDrops),
            new Case("exemptNyliumNonSneakingBreakDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptNyliumNonSneakingBreakDrops),
            new Case("exemptNyliumExplosionDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptNyliumExplosionDrops),
            new Case("exemptNyliumWaterDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptNyliumWaterDrops),
            new Case("exemptNyliumSoilRemovedDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptNyliumSoilRemovedDrops),
            new Case("exemptWarpedNyliumNonSneakingBreakDrops", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptWarpedNyliumNonSneakingBreakDrops),
            new Case("exemptMossDoublePlantUpperHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossDoublePlantUpperHalfDropsOnce),
            new Case("exemptMossDoublePlantLowerHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossDoublePlantLowerHalfDropsOnce),
            new Case("exemptMossDoublePlantSoilRemovedDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossDoublePlantSoilRemovedDropsOnce),
            new Case("exemptMossDoublePlantExplosionDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossDoublePlantExplosionDropsOnce),
            new Case("exemptMossDoublePlantWaterDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossDoublePlantWaterDropsOnce),
            new Case("exemptMossTallGrassShearsUpperHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossTallGrassShearsUpperHalfDropsOnce),
            new Case("exemptMossTallGrassShearsLowerHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossTallGrassShearsLowerHalfDropsOnce),
            new Case("exemptMossShearsStillNeeded", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::exemptMossShearsStillNeeded),
            new Case("nonExemptSoilNextToMossDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::nonExemptSoilNextToMossDropsNothing),
            new Case("nonExemptDirtExplosionDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::nonExemptDirtExplosionDropsNothing),
            new Case("mossRemovedElsewhereDoesNotExemptOthers", TIMEOUT, TestConfigs.DEFAULTS, ExemptSoilTestCases::mossRemovedElsewhereDoesNotExemptOthers),
            // Exempt soil list empty: moss is ordinary soil.
            new Case("noExemptSoilMossNonSneakingBreakDropsNothing", TIMEOUT, TestConfigs.NO_EXEMPT_SOIL, ExemptSoilTestCases::noExemptSoilMossNonSneakingBreakDropsNothing),
            new Case("noExemptSoilMossSoilRemovedDropsNothing", TIMEOUT, TestConfigs.NO_EXEMPT_SOIL, ExemptSoilTestCases::noExemptSoilMossSoilRemovedDropsNothing),
            new Case("noExemptSoilNyliumNonSneakingBreakDropsNothing", TIMEOUT, TestConfigs.NO_EXEMPT_SOIL, ExemptSoilTestCases::noExemptSoilNyliumNonSneakingBreakDropsNothing),
            // Exempt soil list ["#minecraft:dirt"]: tag entries work, and moss is no longer exempt.
            new Case("exemptDirtTagNonSneakingBreakDrops", TIMEOUT, TestConfigs.EXEMPT_DIRT_TAG, ExemptSoilTestCases::exemptDirtTagNonSneakingBreakDrops),
            new Case("exemptDirtTagSoilRemovedDrops", TIMEOUT, TestConfigs.EXEMPT_DIRT_TAG, ExemptSoilTestCases::exemptDirtTagSoilRemovedDrops),
            new Case("exemptDirtTagMossDropsNothing", TIMEOUT, TestConfigs.EXEMPT_DIRT_TAG, ExemptSoilTestCases::exemptDirtTagMossDropsNothing));

    // --------------------------------------------------------------------- moss

    static void exemptMossNonSneakingBreakDrops(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    static void exemptMossExplosionDrops(GameTestHelper helper) {
        Scenarios.explosion(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    static void exemptMossWaterDrops(GameTestHelper helper) {
        Scenarios.fluid(helper, Blocks.WATER, Blocks.MOSS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    /** The moss is already air when the poppy pops off; it still counts as standing on moss. */
    static void exemptMossSneakingSoilRemovedDrops(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, true, Items.POPPY, 1);
    }

    static void exemptMossNonSneakingSoilRemovedDrops(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, false, Items.POPPY, 1);
    }

    static void exemptMossPistonDrops(GameTestHelper helper) {
        Scenarios.piston(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    static void exemptPaleMossNonSneakingBreakDrops(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.PALE_MOSS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    // ------------------------------------------------------------------- nylium

    static void exemptNyliumNonSneakingBreakDrops(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.CRIMSON_NYLIUM, Blocks.CRIMSON_ROOTS, Items.CRIMSON_ROOTS, 1);
    }

    static void exemptNyliumExplosionDrops(GameTestHelper helper) {
        Scenarios.explosion(helper, Blocks.CRIMSON_NYLIUM, Blocks.CRIMSON_ROOTS, Items.CRIMSON_ROOTS, 1);
    }

    static void exemptNyliumWaterDrops(GameTestHelper helper) {
        Scenarios.fluid(helper, Blocks.WATER, Blocks.CRIMSON_NYLIUM, Blocks.CRIMSON_ROOTS, Items.CRIMSON_ROOTS, 1);
    }

    static void exemptNyliumSoilRemovedDrops(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.CRIMSON_NYLIUM, Blocks.CRIMSON_ROOTS, true, Items.CRIMSON_ROOTS, 1);
    }

    static void exemptWarpedNyliumNonSneakingBreakDrops(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.WARPED_NYLIUM, Blocks.CRIMSON_ROOTS, Items.CRIMSON_ROOTS, 1);
    }

    // ------------------------------------------------------------ double plants

    static void exemptMossDoublePlantUpperHalfDropsOnce(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.MOSS_BLOCK, Blocks.SUNFLOWER, false, Items.AIR, true, Items.SUNFLOWER, 1);
    }

    static void exemptMossDoublePlantLowerHalfDropsOnce(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.MOSS_BLOCK, Blocks.SUNFLOWER, false, Items.AIR, false, Items.SUNFLOWER, 1);
    }

    static void exemptMossDoublePlantSoilRemovedDropsOnce(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.MOSS_BLOCK, Blocks.SUNFLOWER, true, Items.SUNFLOWER, 1);
    }

    static void exemptMossDoublePlantExplosionDropsOnce(GameTestHelper helper) {
        Scenarios.explosion(helper, Blocks.MOSS_BLOCK, Blocks.SUNFLOWER, Items.SUNFLOWER, 1);
    }

    static void exemptMossDoublePlantWaterDropsOnce(GameTestHelper helper) {
        Scenarios.fluid(helper, Blocks.WATER, Blocks.MOSS_BLOCK, Blocks.SUNFLOWER, Items.SUNFLOWER, 1);
    }

    /**
     * Sheared tall grass drops from whichever half is broken first, so the upper half must find
     * its soil below the lower half.
     */
    static void exemptMossTallGrassShearsUpperHalfDropsOnce(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.MOSS_BLOCK, Blocks.TALL_GRASS, false, Items.SHEARS, true, Items.SHORT_GRASS, 2);
    }

    static void exemptMossTallGrassShearsLowerHalfDropsOnce(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.MOSS_BLOCK, Blocks.TALL_GRASS, false, Items.SHEARS, false, Items.SHORT_GRASS, 2);
    }

    /** Exempt means vanilla, so the loot table still wants shears for grass. */
    static void exemptMossShearsStillNeeded(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.MOSS_BLOCK, Blocks.SHORT_GRASS, Items.SHORT_GRASS, 0);
    }

    // --------------------------------------------------------- non-exempt soil

    /** Only the block directly below counts: moss beside and under the grass block does not exempt the poppy. */
    static void nonExemptSoilNextToMossDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.prepareGround(helper);
        helper.setBlock(PLANT.below().north(), Blocks.MOSS_BLOCK);
        helper.setBlock(PLANT.below().west(), Blocks.MOSS_BLOCK);
        helper.setBlock(PLANT.below(2), Blocks.MOSS_BLOCK);
        helper.setBlock(PLANT, Blocks.POPPY);
        CarefulHarvestTestCases.breakAs(helper, CarefulHarvestTestCases.survivalPlayer(helper, false), PLANT);
        CarefulHarvestTestCases.succeedWithoutDrop(helper, Blocks.POPPY, Items.POPPY);
    }

    static void nonExemptDirtExplosionDropsNothing(GameTestHelper helper) {
        Scenarios.explosion(helper, Blocks.DIRT, Blocks.POPPY, Items.POPPY, 0);
    }

    /**
     * Moss broken earlier in the same tick somewhere else must not exempt a poppy on grass that
     * pops off in that tick: the removed-soil memory is per position.
     */
    static void mossRemovedElsewhereDoesNotExemptOthers(GameTestHelper helper) {
        CarefulHarvestTestCases.prepareGround(helper);
        BlockPos moss = new BlockPos(3, 1, 2);
        helper.setBlock(moss, Blocks.MOSS_BLOCK);
        helper.setBlock(PLANT, Blocks.POPPY);
        CarefulHarvestTestCases.breakAs(helper, CarefulHarvestTestCases.survivalPlayer(helper, true), moss);
        CarefulHarvestTestCases.breakAs(helper, CarefulHarvestTestCases.survivalPlayer(helper, true), PLANT.below());
        helper.runAfterDelay(CarefulHarvestTestCases.SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(Blocks.POPPY, PLANT);
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.assertItemEntityPresent(Items.MOSS_BLOCK);
            helper.succeed();
        });
    }

    // ----------------------------------------------------------- other configs

    static void noExemptSoilMossNonSneakingBreakDropsNothing(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, Items.POPPY, 0);
    }

    static void noExemptSoilMossSoilRemovedDropsNothing(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, true, Items.POPPY, 0);
    }

    static void noExemptSoilNyliumNonSneakingBreakDropsNothing(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.CRIMSON_NYLIUM, Blocks.CRIMSON_ROOTS, Items.CRIMSON_ROOTS, 0);
    }

    static void exemptDirtTagNonSneakingBreakDrops(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.COARSE_DIRT, Blocks.POPPY, Items.POPPY, 1);
    }

    static void exemptDirtTagSoilRemovedDrops(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.DIRT, Blocks.POPPY, false, Items.POPPY, 1);
    }

    static void exemptDirtTagMossDropsNothing(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.MOSS_BLOCK, Blocks.POPPY, Items.POPPY, 0);
    }
}
