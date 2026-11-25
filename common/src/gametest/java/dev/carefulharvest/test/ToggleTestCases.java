package dev.carefulharvest.test;

import static dev.carefulharvest.test.CarefulHarvestTestCases.TIMEOUT;

import dev.carefulharvest.test.CarefulHarvestTestCases.Case;
import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * The five per-cause toggles. Each toggle's environment turns only that toggle on; its own cause
 * must then drop exactly like vanilla, and every other cause must still drop nothing. The
 * "toggle off" side of each cause is covered by {@link CarefulHarvestTestCases#BEHAVIOUR}, which
 * runs with every toggle off.
 */
final class ToggleTestCases {
    private ToggleTestCases() {
    }

    static final List<Case> CASES = List.of(
            // nonSneakingBreak
            new Case("nonSneakingBreakOnPoppyDrops", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::nonSneakingBreakOnPoppyDrops),
            new Case("nonSneakingBreakOnUpperHalfDropsOnce", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::nonSneakingBreakOnUpperHalfDropsOnce),
            new Case("nonSneakingBreakOnLowerHalfDropsOnce", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::nonSneakingBreakOnLowerHalfDropsOnce),
            new Case("nonSneakingBreakOnBareHandedGrassDropsNoGrass", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::nonSneakingBreakOnBareHandedGrassDropsNoGrass),
            new Case("nonSneakingBreakOnShearsGrassDrops", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::nonSneakingBreakOnShearsGrassDrops),
            new Case("nonSneakingBreakOnShearsTallGrassUpperHalfDropsOnce", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::nonSneakingBreakOnShearsTallGrassUpperHalfDropsOnce),
            new Case("nonSneakingBreakOnWaterDropsNothing", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::waterDropsNothing),
            new Case("nonSneakingBreakOnExplosionDropsNothing", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::explosionDropsNothing),
            new Case("nonSneakingBreakOnSoilRemovedDropsNothing", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::nonSneakingSoilRemovedDropsNothing),
            new Case("nonSneakingBreakOnPistonDropsNothing", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::pistonDropsNothing),
            // fluids
            new Case("fluidsOnWaterDrops", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::fluidsOnWaterDrops),
            new Case("fluidsOnWaterDoublePlantDropsOnce", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::fluidsOnWaterDoublePlantDropsOnce),
            new Case("fluidsOnLavaDropsNothingLikeVanilla", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::fluidsOnLavaDropsNothingLikeVanilla),
            new Case("fluidsOnNonSneakingBreakDropsNothing", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::nonSneakingBreakDropsNothing),
            new Case("fluidsOnExplosionDropsNothing", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::explosionDropsNothing),
            new Case("fluidsOnSoilRemovedDropsNothing", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::nonSneakingSoilRemovedDropsNothing),
            new Case("fluidsOnPistonDropsNothing", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::pistonDropsNothing),
            // explosions
            new Case("explosionsOnPoppiesDrop", TIMEOUT, TestConfigs.EXPLOSIONS, ToggleTestCases::explosionsOnPoppiesDrop),
            new Case("explosionsOnDoublePlantDropsOnce", TIMEOUT, TestConfigs.EXPLOSIONS, ToggleTestCases::explosionsOnDoublePlantDropsOnce),
            new Case("explosionsOnNonSneakingBreakDropsNothing", TIMEOUT, TestConfigs.EXPLOSIONS, ToggleTestCases::nonSneakingBreakDropsNothing),
            new Case("explosionsOnWaterDropsNothing", TIMEOUT, TestConfigs.EXPLOSIONS, ToggleTestCases::waterDropsNothing),
            new Case("explosionsOnSoilRemovedDropsNothing", TIMEOUT, TestConfigs.EXPLOSIONS, ToggleTestCases::nonSneakingSoilRemovedDropsNothing),
            new Case("explosionsOnPistonDropsNothing", TIMEOUT, TestConfigs.EXPLOSIONS, ToggleTestCases::pistonDropsNothing),
            // supportBroken
            new Case("supportBrokenOnSneakingSoilBreakDrops", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::supportBrokenOnSneakingSoilBreakDrops),
            new Case("supportBrokenOnNonSneakingSoilBreakDrops", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::supportBrokenOnNonSneakingSoilBreakDrops),
            new Case("supportBrokenOnDoublePlantDropsOnce", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::supportBrokenOnDoublePlantDropsOnce),
            new Case("supportBrokenOnNonSneakingBreakDropsNothing", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::nonSneakingBreakDropsNothing),
            new Case("supportBrokenOnWaterDropsNothing", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::waterDropsNothing),
            new Case("supportBrokenOnExplosionDropsNothing", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::explosionDropsNothing),
            new Case("supportBrokenOnPistonDropsNothing", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::pistonDropsNothing),
            // other
            new Case("otherOnPistonDrops", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::otherOnPistonDrops),
            new Case("otherOnEndermanDeathDrops", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::otherOnEndermanDeathDrops),
            new Case("otherOnDestroyBlockOutsideBreakDrops", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::otherOnDestroyBlockOutsideBreakDrops),
            new Case("otherOnNonSneakingBreakDropsNothing", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::nonSneakingBreakDropsNothing),
            new Case("otherOnWaterDropsNothing", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::waterDropsNothing),
            new Case("otherOnExplosionDropsNothing", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::explosionDropsNothing),
            new Case("otherOnSoilRemovedDropsNothing", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::nonSneakingSoilRemovedDropsNothing),
            // Sneaking still drops with every toggle on its own.
            new Case("nonSneakingBreakOnSneakingBreakDrops", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, ToggleTestCases::sneakingBreakDrops),
            new Case("fluidsOnSneakingBreakDrops", TIMEOUT, TestConfigs.FLUIDS, ToggleTestCases::sneakingBreakDrops),
            new Case("explosionsOnSneakingBreakDrops", TIMEOUT, TestConfigs.EXPLOSIONS, ToggleTestCases::sneakingBreakDrops),
            new Case("supportBrokenOnSneakingBreakDrops", TIMEOUT, TestConfigs.SUPPORT_BROKEN, ToggleTestCases::sneakingBreakDrops),
            new Case("otherOnSneakingBreakDrops", TIMEOUT, TestConfigs.OTHER, ToggleTestCases::sneakingBreakDrops));

    // ---------------------------------------------------- shared negative checks

    static void nonSneakingBreakDropsNothing(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 0);
    }

    static void waterDropsNothing(GameTestHelper helper) {
        Scenarios.fluid(helper, Blocks.WATER, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 0);
    }

    static void explosionDropsNothing(GameTestHelper helper) {
        Scenarios.explosion(helper, Blocks.DIRT, Blocks.POPPY, Items.POPPY, 0);
    }

    static void nonSneakingSoilRemovedDropsNothing(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, false, Items.POPPY, 0);
    }

    static void pistonDropsNothing(GameTestHelper helper) {
        Scenarios.piston(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 0);
    }

    static void sneakingBreakDrops(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, true, Items.AIR, false, Items.POPPY, 1);
    }

    // --------------------------------------------------------- nonSneakingBreak

    static void nonSneakingBreakOnPoppyDrops(GameTestHelper helper) {
        Scenarios.nonSneakingBreak(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    static void nonSneakingBreakOnUpperHalfDropsOnce(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.GRASS_BLOCK, Blocks.SUNFLOWER, false, Items.AIR, true, Items.SUNFLOWER, 1);
    }

    static void nonSneakingBreakOnLowerHalfDropsOnce(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.GRASS_BLOCK, Blocks.SUNFLOWER, false, Items.AIR, false, Items.SUNFLOWER, 1);
    }

    /** The loot table still applies: no shears, no grass. */
    static void nonSneakingBreakOnBareHandedGrassDropsNoGrass(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.GRASS_BLOCK, Blocks.SHORT_GRASS, false, Items.AIR, false, Items.SHORT_GRASS, 0);
    }

    static void nonSneakingBreakOnShearsGrassDrops(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.GRASS_BLOCK, Blocks.SHORT_GRASS, false, Items.SHEARS, false, Items.SHORT_GRASS, 1);
    }

    static void nonSneakingBreakOnShearsTallGrassUpperHalfDropsOnce(GameTestHelper helper) {
        Scenarios.playerBreak(helper, Blocks.GRASS_BLOCK, Blocks.TALL_GRASS, false, Items.SHEARS, true, Items.SHORT_GRASS, 2);
    }

    // ------------------------------------------------------------------- fluids

    static void fluidsOnWaterDrops(GameTestHelper helper) {
        Scenarios.fluid(helper, Blocks.WATER, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    static void fluidsOnWaterDoublePlantDropsOnce(GameTestHelper helper) {
        Scenarios.fluid(helper, Blocks.WATER, Blocks.GRASS_BLOCK, Blocks.SUNFLOWER, Items.SUNFLOWER, 1);
    }

    /** Vanilla lava destroys plants without dropping anything; the toggle does not change that. */
    static void fluidsOnLavaDropsNothingLikeVanilla(GameTestHelper helper) {
        Scenarios.fluid(helper, Blocks.LAVA, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 0);
    }

    // --------------------------------------------------------------- explosions

    /** Two poppies on dirt; the dirt is blown up too, so either may pop off before it is hit. */
    static void explosionsOnPoppiesDrop(GameTestHelper helper) {
        Scenarios.explosion(helper, Blocks.DIRT, Blocks.POPPY, Items.POPPY, 1);
    }

    static void explosionsOnDoublePlantDropsOnce(GameTestHelper helper) {
        Scenarios.explosion(helper, Blocks.DIRT, Blocks.SUNFLOWER, Items.SUNFLOWER, 1);
    }

    // ------------------------------------------------------------ supportBroken

    static void supportBrokenOnSneakingSoilBreakDrops(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, true, Items.POPPY, 1);
    }

    static void supportBrokenOnNonSneakingSoilBreakDrops(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, false, Items.POPPY, 1);
    }

    static void supportBrokenOnDoublePlantDropsOnce(GameTestHelper helper) {
        Scenarios.soilRemoved(helper, Blocks.GRASS_BLOCK, Blocks.SUNFLOWER, true, Items.SUNFLOWER, 1);
    }

    // -------------------------------------------------------------------- other

    static void otherOnPistonDrops(GameTestHelper helper) {
        Scenarios.piston(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }

    static void otherOnEndermanDeathDrops(GameTestHelper helper) {
        Scenarios.endermanDeath(helper, 1);
    }

    static void otherOnDestroyBlockOutsideBreakDrops(GameTestHelper helper) {
        Scenarios.destroyBlockOutsideBreak(helper, Blocks.GRASS_BLOCK, Blocks.POPPY, Items.POPPY, 1);
    }
}
