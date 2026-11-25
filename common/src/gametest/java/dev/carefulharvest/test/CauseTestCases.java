package dev.carefulharvest.test;

import static dev.carefulharvest.test.CarefulHarvestTestCases.PLANT;
import static dev.carefulharvest.test.CarefulHarvestTestCases.SETTLE_TICKS;
import static dev.carefulharvest.test.CarefulHarvestTestCases.TIMEOUT;
import static dev.carefulharvest.test.CarefulHarvestTestCases.assertItemCount;
import static dev.carefulharvest.test.CarefulHarvestTestCases.breakAs;
import static dev.carefulharvest.test.CarefulHarvestTestCases.prepareGround;
import static dev.carefulharvest.test.CarefulHarvestTestCases.survivalPlayer;

import dev.carefulharvest.DestructionContext;
import dev.carefulharvest.DropCause;
import dev.carefulharvest.SoilMemory;
import dev.carefulharvest.test.CarefulHarvestTestCases.Case;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Cause classification in nested and ordering-dependent situations, the cause marker stack, and
 * the removed-soil memory.
 */
final class CauseTestCases {
    private CauseTestCases() {
    }

    private static final int GRID_TIMEOUT = 200;

    static final List<Case> CASES = List.of(
            // A player breaking the soil is lost support, never a player break, whatever else is on.
            new Case("nonSneakingBreakOnSneakingSoilBreakDropsNothing", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, h -> sneakSoil(h, Blocks.POPPY, Items.POPPY, 0)),
            new Case("fluidsOnSneakingSoilBreakDropsNothing", TIMEOUT, TestConfigs.FLUIDS, h -> sneakSoil(h, Blocks.POPPY, Items.POPPY, 0)),
            new Case("explosionsOnSneakingSoilBreakDropsNothing", TIMEOUT, TestConfigs.EXPLOSIONS, h -> sneakSoil(h, Blocks.POPPY, Items.POPPY, 0)),
            new Case("otherOnSneakingSoilBreakDropsNothing", TIMEOUT, TestConfigs.OTHER, h -> sneakSoil(h, Blocks.POPPY, Items.POPPY, 0)),
            new Case("nonSneakingBreakOnSneakingSoilBreakDoublePlantDropsNothing", TIMEOUT, TestConfigs.NON_SNEAKING_BREAK, h -> sneakSoil(h, Blocks.SUNFLOWER, Items.SUNFLOWER, 0)),
            new Case("otherOnSneakingSoilBreakDoublePlantDropsNothing", TIMEOUT, TestConfigs.OTHER, h -> sneakSoil(h, Blocks.SUNFLOWER, Items.SUNFLOWER, 0)),
            new Case("supportBrokenOnNonSneakingSoilBreakDoublePlantDropsOnce", TIMEOUT, TestConfigs.SUPPORT_BROKEN,
                    h -> Scenarios.soilRemoved(h, Blocks.GRASS_BLOCK, Blocks.SUNFLOWER, false, Items.SUNFLOWER, 1)),
            // Explosion ordering: deterministic classification, and real explosions over a grid.
            new Case("nestedCausesClassifiedDeterministically", TIMEOUT, TestConfigs.DEFAULTS, CauseTestCases::nestedCausesClassifiedDeterministically),
            new Case("explosionsOnPoppyGridEveryPoppyDrops", GRID_TIMEOUT, TestConfigs.EXPLOSIONS, h -> grid(h, Blocks.POPPY, Items.POPPY, 1)),
            new Case("supportBrokenOnPoppyGridExplosionDropsNothing", GRID_TIMEOUT, TestConfigs.SUPPORT_BROKEN, h -> grid(h, Blocks.POPPY, Items.POPPY, 0)),
            new Case("explosionsOnSunflowerGridEachDropsOnce", GRID_TIMEOUT, TestConfigs.EXPLOSIONS, h -> grid(h, Blocks.SUNFLOWER, Items.SUNFLOWER, 1)),
            new Case("supportBrokenOnSunflowerGridExplosionDropsNothing", GRID_TIMEOUT, TestConfigs.SUPPORT_BROKEN, h -> grid(h, Blocks.SUNFLOWER, Items.SUNFLOWER, 0)),
            // The marker stack is always empty again afterwards.
            new Case("markerStackEmptyAfterEveryPath", TIMEOUT, TestConfigs.DEFAULTS, CauseTestCases::markerStackEmptyAfterEveryPath),
            // Removed-soil memory.
            new Case("soilMemoryForgottenWhenSoilReplaced", TIMEOUT, TestConfigs.DEFAULTS, CauseTestCases::soilMemoryForgottenWhenSoilReplaced),
            new Case("soilMemoryForgottenWhenPlantPlacedAbove", TIMEOUT, TestConfigs.DEFAULTS, CauseTestCases::soilMemoryForgottenWhenPlantPlacedAbove),
            new Case("soilMemoryClearedAfterTick", TIMEOUT, TestConfigs.DEFAULTS, CauseTestCases::soilMemoryClearedAfterTick),
            new Case("soilMemoryPerDimension", 100, TestConfigs.DEFAULTS, CauseTestCases::soilMemoryPerDimension),
            new Case("soilMemoryOnlyRecordsExemptSoil", TIMEOUT, TestConfigs.NO_EXEMPT_SOIL, CauseTestCases::soilMemoryOnlyRecordsExemptSoil));

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, message);
    }

    // ------------------------------------------------------------- soil break

    /** A sneaking player breaks the grass block under the plant. */
    private static void sneakSoil(GameTestHelper helper, Block plant, Item item, int count) {
        Scenarios.soilRemoved(helper, Blocks.GRASS_BLOCK, plant, true, item, count);
    }

    // ------------------------------------------------------ deterministic order

    private static DropCause classify(BlockState state, BlockPos pos, @Nullable Entity breaker, DestructionContext.Frame... frames) {
        int pushed = 0;
        try {
            for (DestructionContext.Frame frame : frames) {
                DestructionContext.push(frame);
                pushed++;
            }
            return DestructionContext.classify(state, pos, breaker);
        } finally {
            for (; pushed > 0; pushed--) {
                DestructionContext.pop();
            }
        }
    }

    /**
     * The orders an explosion can process a plant and its soil in, and other nestings, replayed
     * with exactly the frames the mixins push, so no random shuffle decides what is covered.
     */
    static void nestedCausesClassifiedDeterministically(GameTestHelper helper) {
        BlockPos plant = helper.absolutePos(PLANT);
        BlockPos soil = plant.below();
        BlockState poppy = Blocks.POPPY.defaultBlockState();
        BlockState lower = Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
        ServerPlayer sneaking = survivalPlayer(helper, true);
        List<BlockPos> big = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            big.add(soil.east(i + 1));
        }
        List<BlockPos> bigWithPlant = new ArrayList<>(big);
        bigWithPlant.add(plant);
        int depth = DestructionContext.depth();

        // Explosion: soil handled before the plant in the blast -> explosion, not lost support.
        helper.assertValueEqual(classify(poppy, plant, null, DestructionContext.explosionBlocks(List.of(soil, plant)),
                new DestructionContext.ExplosionHit(soil), new DestructionContext.ShapeDestroy(plant, poppy)), DropCause.EXPLOSION, "soil first, plant in blast");
        helper.assertValueEqual(classify(poppy, plant, null, DestructionContext.explosionBlocks(bigWithPlant),
                new DestructionContext.ExplosionHit(soil), new DestructionContext.ShapeDestroy(plant, poppy)), DropCause.EXPLOSION, "large blast (set lookup)");
        helper.assertValueEqual(classify(poppy, plant, null, DestructionContext.explosionBlocks(List.of(plant, soil)),
                new DestructionContext.ExplosionHit(plant)), DropCause.EXPLOSION, "plant first");
        // Plant outside the blast, its soil blown up -> lost support.
        helper.assertValueEqual(classify(poppy, plant, null, DestructionContext.explosionBlocks(List.of(soil)),
                new DestructionContext.ExplosionHit(soil), new DestructionContext.ShapeDestroy(plant, poppy)), DropCause.SUPPORT_BROKEN, "plant outside blast");
        helper.assertValueEqual(classify(poppy, plant, null, DestructionContext.explosionBlocks(big),
                new DestructionContext.ExplosionHit(soil), new DestructionContext.ShapeDestroy(plant, poppy)), DropCause.SUPPORT_BROKEN, "plant outside large blast");
        // Nested explosion (a block blowing up while another explosion is processed): the outer blast counts.
        helper.assertValueEqual(classify(poppy, plant, null, DestructionContext.explosionBlocks(List.of(plant)),
                new DestructionContext.ExplosionHit(soil.west()), DestructionContext.explosionBlocks(List.of(soil)),
                new DestructionContext.ExplosionHit(soil), new DestructionContext.ShapeDestroy(plant, poppy)), DropCause.EXPLOSION, "nested explosion");
        // Double plant in a blast, in every order of lower half, upper half and soil.
        helper.assertValueEqual(classify(upper, plant.above(), null, DestructionContext.explosionBlocks(List.of(plant)),
                new DestructionContext.ExplosionHit(plant), new DestructionContext.ShapeDestroy(plant.above(), upper)), DropCause.EXPLOSION, "upper follows exploded lower");
        helper.assertValueEqual(classify(lower, plant, null, DestructionContext.explosionBlocks(List.of(plant.above())),
                new DestructionContext.ExplosionHit(plant.above()), new DestructionContext.ShapeDestroy(plant, lower)), DropCause.EXPLOSION, "lower follows exploded upper");
        helper.assertValueEqual(classify(lower, plant, null, DestructionContext.explosionBlocks(List.of(soil, plant.above())),
                new DestructionContext.ExplosionHit(soil), new DestructionContext.ShapeDestroy(plant, lower)), DropCause.EXPLOSION, "soil first, only upper half in blast");
        helper.assertValueEqual(classify(upper, plant.above(), null, DestructionContext.explosionBlocks(List.of(soil)),
                new DestructionContext.ExplosionHit(soil), new DestructionContext.ShapeDestroy(plant, lower),
                new DestructionContext.ShapeDestroy(plant.above(), upper)), DropCause.SUPPORT_BROKEN, "double plant outside blast");
        // Player breaking the soil -> lost support, sneaking or not; the plant's own break -> the player's.
        helper.assertValueEqual(classify(poppy, plant, null, new DestructionContext.PlayerBreak(sneaking, soil, Blocks.DIRT.defaultBlockState(), true),
                new DestructionContext.ShapeDestroy(plant, poppy)), DropCause.SUPPORT_BROKEN, "sneaking soil break");
        helper.assertValueEqual(classify(upper, plant.above(), null, new DestructionContext.PlayerBreak(sneaking, soil, Blocks.DIRT.defaultBlockState(), true),
                new DestructionContext.ShapeDestroy(plant, lower), new DestructionContext.ShapeDestroy(plant.above(), upper)), DropCause.SUPPORT_BROKEN,
                "sneaking soil break under double plant");
        helper.assertValueEqual(classify(poppy, plant, sneaking, new DestructionContext.PlayerBreak(sneaking, plant, poppy, true)),
                DropCause.SNEAKING_BREAK, "sneaking break");
        helper.assertValueEqual(classify(lower, plant, null, new DestructionContext.PlayerBreak(sneaking, plant.above(), upper, false),
                new DestructionContext.ShapeDestroy(plant, lower)), DropCause.NON_SNEAKING_BREAK, "other half of a non-sneaking break");
        helper.assertValueEqual(classify(poppy, plant, sneaking, new DestructionContext.PlayerBreak(sneaking, plant.east(), poppy, true)),
                DropCause.OTHER, "a different block during a break");
        // Fluid flowing into one half, the other half following; a plant losing support to the fluid.
        helper.assertValueEqual(classify(upper, plant.above(), null, new DestructionContext.FluidFlow(plant),
                new DestructionContext.ShapeDestroy(plant.above(), upper)), DropCause.FLUID, "upper follows flooded lower");
        helper.assertValueEqual(classify(poppy, plant, null, new DestructionContext.FluidFlow(soil),
                new DestructionContext.ShapeDestroy(plant, poppy)), DropCause.SUPPORT_BROKEN, "support washed away");
        helper.assertValueEqual(classify(poppy, plant, null), DropCause.OTHER, "no frame");
        helper.assertValueEqual(DestructionContext.depth(), depth, "stack depth restored");
        helper.succeed();
    }

    // ------------------------------------------------------------ real blasts

    private static final int ROUNDS = 4;
    private static final BlockPos GRID_CENTRE = new BlockPos(3, 3, 3);

    /**
     * {@link #ROUNDS} real explosions, each centred on a 3x3 grid of plants on dirt. All nine plants
     * are always inside the blast; at least the middle row of dirt is blown up as well, and the
     * blast handles blocks in random order, so in each round several plants are handled after
     * their soil. Across the rounds a wrong classification of "soil first" would show up with a
     * probability far above 99.99%. Expects {@code perPlant} items per plant in every round.
     */
    private static void grid(GameTestHelper helper, Block plant, Item item, int perPlant) {
        gridRound(helper, plant, item, perPlant, 0, 0);
    }

    private static void gridRound(GameTestHelper helper, Block plant, Item item, int perPlant, int round, int soilGoneSoFar) {
        boolean tall = plant instanceof DoublePlantBlock;
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                for (int y = 1; y <= 6; y++) {
                    helper.setBlock(new BlockPos(x, y, z), y == 1 ? Blocks.STONE : Blocks.AIR);
                }
            }
        }
        List<BlockPos> plants = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = GRID_CENTRE.offset(dx, 0, dz);
                plants.add(pos);
                helper.setBlock(pos.below(), Blocks.DIRT);
                if (tall) {
                    CarefulHarvestTestCases.placeDoublePlant(helper, plant, pos);
                } else {
                    helper.setBlock(pos, plant);
                }
            }
        }
        Vec3 centre = helper.absoluteVec(Vec3.atCenterOf(GRID_CENTRE));
        helper.getLevel().explode(null, centre.x, centre.y, centre.z, 2.5F, Level.ExplosionInteraction.TNT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            int soilGone = 0;
            for (BlockPos pos : plants) {
                helper.assertBlockNotPresent(plant, pos);
                if (tall) {
                    helper.assertBlockNotPresent(plant, pos.above());
                }
                if (!helper.getBlockState(pos.below()).is(Blocks.DIRT)) {
                    soilGone++;
                }
            }
            assertItemCount(helper, item, plants.size() * perPlant);
            helper.killAllEntitiesOfClass(ItemEntity.class);
            int total = soilGoneSoFar + soilGone;
            if (round + 1 < ROUNDS) {
                gridRound(helper, plant, item, perPlant, round + 1, total);
            } else {
                check(helper, total >= 3 * ROUNDS, "the blasts should have removed the soil under most plants: " + total);
                helper.succeed();
            }
        });
    }

    // ---------------------------------------------------------- marker stack

    static void markerStackEmptyAfterEveryPath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int depth = DestructionContext.depth();
        prepareGround(helper);
        // Successful sneaking break of a double plant.
        CarefulHarvestTestCases.placeDoublePlant(helper, Blocks.SUNFLOWER, PLANT);
        breakAs(helper, survivalPlayer(helper, true), PLANT);
        helper.assertValueEqual(DestructionContext.depth(), depth, "after a player break");
        // Early return: a player who is not an operator may not break a command block.
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.COMMAND_BLOCK);
        helper.assertFalse(survivalPlayer(helper, true).gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 2, 2))),
                "non-operators cannot break a command block");
        helper.assertBlockPresent(Blocks.COMMAND_BLOCK, new BlockPos(3, 2, 2));
        helper.assertValueEqual(DestructionContext.depth(), depth, "after a refused break");
        helper.setBlock(PLANT, Blocks.POPPY);
        // Soil break with a plant on it, explosion, fluid, destroyBlock.
        breakAs(helper, survivalPlayer(helper, false), PLANT.below());
        helper.assertValueEqual(DestructionContext.depth(), depth, "after a soil break");
        Vec3 centre = helper.absoluteVec(Vec3.atCenterOf(new BlockPos(2, 2, 1)));
        level.explode(null, centre.x, centre.y, centre.z, 1.5F, Level.ExplosionInteraction.TNT);
        helper.assertValueEqual(DestructionContext.depth(), depth, "after an explosion");
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.POPPY);
        level.destroyBlock(helper.absolutePos(new BlockPos(3, 2, 2)), true, null);
        helper.assertValueEqual(DestructionContext.depth(), depth, "after destroyBlock");
        helper.succeed();
    }

    // ----------------------------------------------------------- soil memory

    /** Moss under nothing is broken, dirt is put in its place and a poppy planted on it, all in one tick. */
    static void soilMemoryForgottenWhenSoilReplaced(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        prepareGround(helper);
        BlockPos soil = PLANT.below();
        helper.setBlock(soil, Blocks.MOSS_BLOCK);
        breakAs(helper, survivalPlayer(helper, true), soil);
        check(helper, SoilMemory.removedThisTick(level, helper.absolutePos(soil)) != null, "removed moss remembered");
        helper.setBlock(soil, Blocks.DIRT);
        check(helper, SoilMemory.removedThisTick(level, helper.absolutePos(soil)) == null, "forgotten once dirt is placed");
        helper.setBlock(PLANT, Blocks.POPPY);
        breakAs(helper, survivalPlayer(helper, false), PLANT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            assertItemCount(helper, Items.POPPY, 0);
            helper.succeed();
        });
    }

    /** Moss is broken and, in the same tick, a poppy is put where it can stand without support. */
    static void soilMemoryForgottenWhenPlantPlacedAbove(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        prepareGround(helper);
        BlockPos soil = PLANT.below();
        helper.setBlock(soil, Blocks.MOSS_BLOCK);
        breakAs(helper, survivalPlayer(helper, true), soil);
        level.setBlock(helper.absolutePos(PLANT), Blocks.POPPY.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        check(helper, SoilMemory.removedThisTick(level, helper.absolutePos(soil)) == null, "forgotten once a plant is placed above");
        breakAs(helper, survivalPlayer(helper, false), PLANT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            assertItemCount(helper, Items.POPPY, 0);
            helper.succeed();
        });
    }

    static void soilMemoryClearedAfterTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        prepareGround(helper);
        BlockPos soil = PLANT.below();
        helper.setBlock(soil, Blocks.MOSS_BLOCK);
        breakAs(helper, survivalPlayer(helper, true), soil);
        check(helper, SoilMemory.removedThisTick(level, helper.absolutePos(soil)) != null, "removed moss remembered");
        helper.runAfterDelay(1, () -> {
            check(helper, SoilMemory.removedThisTick(level, helper.absolutePos(soil)) == null, "forgotten in the next tick");
            helper.succeed();
        });
    }

    /**
     * Moss is removed in the Nether at exactly the coordinates of the grass under an overworld
     * poppy, then a sneaking player breaks that grass in the same tick: the poppy is not on moss.
     */
    static void soilMemoryPerDimension(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        check(helper, nether != null, "the Nether exists");
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.POPPY);
        BlockPos soil = helper.absolutePos(PLANT.below());
        BlockState original = nether.getBlockState(soil);
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        nether.setBlock(soil, Blocks.MOSS_BLOCK.defaultBlockState(), flags);
        nether.setBlock(soil, original.is(Blocks.MOSS_BLOCK) ? Blocks.AIR.defaultBlockState() : original, flags);
        check(helper, SoilMemory.removedThisTick(nether, soil) != null, "Nether moss remembered");
        check(helper, SoilMemory.removedThisTick(overworld, soil) == null, "not in the overworld");
        breakAs(helper, survivalPlayer(helper, true), PLANT.below());
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(Blocks.POPPY, PLANT);
            assertItemCount(helper, Items.POPPY, 0);
            helper.succeed();
        });
    }

    /** With an empty exempt soil list nothing is remembered; neither is ordinary soil. */
    static void soilMemoryOnlyRecordsExemptSoil(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        prepareGround(helper);
        helper.setBlock(PLANT.below(), Blocks.MOSS_BLOCK);
        helper.setBlock(PLANT.below().east(), Blocks.DIRT);
        breakAs(helper, survivalPlayer(helper, true), PLANT.below());
        breakAs(helper, survivalPlayer(helper, true), PLANT.below().east());
        check(helper, SoilMemory.removedThisTick(level, helper.absolutePos(PLANT.below())) == null, "moss is not exempt here");
        check(helper, SoilMemory.removedThisTick(level, helper.absolutePos(PLANT.below().east())) == null, "dirt is never recorded");
        helper.succeed();
    }
}
