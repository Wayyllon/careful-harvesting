package dev.carefulharvest.test;

import static dev.carefulharvest.test.CarefulHarvestTestCases.PLANT;
import static dev.carefulharvest.test.CarefulHarvestTestCases.SETTLE_TICKS;
import static dev.carefulharvest.test.CarefulHarvestTestCases.assertItemCount;
import static dev.carefulharvest.test.CarefulHarvestTestCases.breakAs;
import static dev.carefulharvest.test.CarefulHarvestTestCases.placeDoublePlant;
import static dev.carefulharvest.test.CarefulHarvestTestCases.prepareGround;
import static dev.carefulharvest.test.CarefulHarvestTestCases.survivalPlayer;
import static dev.carefulharvest.test.CarefulHarvestTestCases.withItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Parameterised scenarios, one per drop cause, shared by the toggle and exempt-soil tests. Each
 * destroys the plant at {@link CarefulHarvestTestCases#PLANT} (standing on {@code soil}) in one
 * way and, after a few ticks, requires exactly {@code count} of {@code item} in the test area.
 * A {@link DoublePlantBlock} is placed as both halves with its lower half at {@code PLANT}.
 */
final class Scenarios {
    private Scenarios() {
    }

    private static boolean isDouble(Block plant) {
        return plant instanceof DoublePlantBlock;
    }

    private static void placePlant(GameTestHelper helper, Block soil, Block plant, BlockPos pos) {
        helper.setBlock(pos.below(), soil);
        if (isDouble(plant)) {
            placeDoublePlant(helper, plant, pos);
        } else {
            helper.setBlock(pos, plant);
        }
    }

    private static void expect(GameTestHelper helper, Block plant, Item item, int count, BlockPos... plantPositions) {
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            for (BlockPos pos : plantPositions) {
                helper.assertBlockNotPresent(plant, pos);
                if (isDouble(plant)) {
                    helper.assertBlockNotPresent(plant, pos.above());
                }
            }
            assertItemCount(helper, item, count);
            helper.succeed();
        });
    }

    /** A survival player breaks the plant (the upper half of a double plant if {@code upper}). */
    static void playerBreak(GameTestHelper helper, Block soil, Block plant, boolean sneaking, Item held, boolean upper,
                            Item item, int count) {
        prepareGround(helper);
        placePlant(helper, soil, plant, PLANT);
        ServerPlayer player = survivalPlayer(helper, sneaking);
        if (held != Items.AIR) {
            withItem(player, held);
        }
        breakAs(helper, player, upper && isDouble(plant) ? PLANT.above() : PLANT);
        expect(helper, plant, item, count, PLANT);
    }

    static void nonSneakingBreak(GameTestHelper helper, Block soil, Block plant, Item item, int count) {
        playerBreak(helper, soil, plant, false, Items.AIR, false, item, count);
    }

    /**
     * A TNT-strength explosion (no drop decay) centred on the plant; for single plants a second
     * one stands next to it. The soil is usually blown up too, in random order with the plants.
     */
    static void explosion(GameTestHelper helper, Block soil, Block plant, Item item, int countPerPlant) {
        prepareGround(helper);
        BlockPos second = PLANT.east();
        placePlant(helper, soil, plant, PLANT);
        if (!isDouble(plant)) {
            placePlant(helper, soil, plant, second);
        }
        Vec3 center = helper.absoluteVec(Vec3.atCenterOf(PLANT));
        helper.getLevel().explode(null, center.x, center.y, center.z, 2.0F, Level.ExplosionInteraction.TNT);
        if (isDouble(plant)) {
            expect(helper, plant, item, countPerPlant, PLANT);
        } else {
            expect(helper, plant, item, 2 * countPerPlant, PLANT, second);
        }
    }

    /**
     * Water (or lava) in a walled channel flows onto the plant at (2,2,1). Runs the real fluid
     * tick directly for the source block so the test does not depend on scheduled-tick timing.
     */
    static void fluid(GameTestHelper helper, Block fluid, Block soil, Block plant, Item item, int count) {
        for (int x = 0; x <= 5; x++) {
            for (int z = 0; z <= 2; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
            helper.setBlock(new BlockPos(x, 2, 0), Blocks.STONE);
            helper.setBlock(new BlockPos(x, 2, 2), Blocks.STONE);
        }
        helper.setBlock(new BlockPos(0, 2, 1), Blocks.STONE);
        BlockPos source = new BlockPos(1, 2, 1);
        BlockPos plantPos = new BlockPos(2, 2, 1);
        placePlant(helper, soil, plant, plantPos);
        helper.setBlock(source, fluid);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(source);
        BlockState state = level.getBlockState(abs);
        state.getFluidState().tick(level, abs, state);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockPresent(fluid == Blocks.LAVA ? Blocks.LAVA : Blocks.WATER, plantPos);
            if (isDouble(plant)) {
                helper.assertBlockNotPresent(plant, plantPos.above());
            }
            assertItemCount(helper, item, count);
            helper.succeed();
        });
    }

    /** A survival player (sneaking or not) breaks the soil under the plant, which pops off. */
    static void soilRemoved(GameTestHelper helper, Block soil, Block plant, boolean sneaking, Item item, int count) {
        prepareGround(helper);
        placePlant(helper, soil, plant, PLANT);
        breakAs(helper, withItem(survivalPlayer(helper, sneaking), Items.DIAMOND_PICKAXE), PLANT.below());
        helper.runAfterDelay(SETTLE_TICKS, () -> helper.assertBlockNotPresent(soil, PLANT.below()));
        expect(helper, plant, item, count, PLANT);
    }

    /** A piston extends into the plant at (2,2,1), which is destroyed. */
    static void piston(GameTestHelper helper, Block soil, Block plant, Item item, int count) {
        prepareGround(helper);
        BlockPos piston = new BlockPos(1, 2, 1);
        BlockPos target = new BlockPos(2, 2, 1);
        helper.setBlock(target.below(), soil);
        helper.setBlock(piston, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.EAST));
        helper.setBlock(target, plant);
        helper.setBlock(piston.west(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockPresent(Blocks.PISTON_HEAD, target);
            assertItemCount(helper, item, count);
            helper.succeed();
        });
    }

    /** An enderman carrying a poppy dies. */
    static void endermanDeath(GameTestHelper helper, int count) {
        prepareGround(helper);
        Enderman enderman = helper.spawn(EntityTypes.ENDERMAN, PLANT);
        enderman.setCarriedBlock(Blocks.POPPY.defaultBlockState());
        enderman.kill(helper.getLevel());
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            assertItemCount(helper, Items.POPPY, count);
            helper.succeed();
        });
    }

    /** Code outside any player break (another mod, say) destroys the plant with drops. */
    static void destroyBlockOutsideBreak(GameTestHelper helper, Block soil, Block plant, Item item, int count) {
        prepareGround(helper);
        placePlant(helper, soil, plant, PLANT);
        helper.getLevel().destroyBlock(helper.absolutePos(PLANT), true, null);
        expect(helper, plant, item, count, PLANT);
    }
}
