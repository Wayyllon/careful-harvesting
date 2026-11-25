package dev.carefulharvest.test;

import dev.carefulharvest.CarefulHarvest;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * Loader-independent server game tests for Careful Harvest. They use vanilla API
 * only; the Fabric and NeoForge game test mods register every entry of {@link #ALL}.
 *
 * <p>All positions are relative to the test structure (an empty 8x8x8 box, rotation
 * {@code NONE}). Each case names the config it needs ({@link TestConfigs}); see
 * {@link ConfigEnvironment} for how tests with different configs are kept apart.
 */
public final class CarefulHarvestTestCases {
    /**
     * A test: name (also the Fabric method name), timeout in ticks, the {@link TestConfigs} config
     * it runs under (its test environment), body.
     */
    public record Case(String name, int maxTicks, String environment, Consumer<GameTestHelper> body) {
    }

    static final BlockPos PLANT = new BlockPos(1, 2, 1);
    static final int SETTLE_TICKS = 10;
    static final int TIMEOUT = 40;

    /** The 1.0 behaviour, under the default config (every toggle off). */
    static final List<Case> BEHAVIOUR = List.of(
            new Case("tagContainsDefaults", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::tagContainsDefaults),
            new Case("sneakingBreakDrops", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingBreakDrops),
            new Case("nonSneakingBreakDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::nonSneakingBreakDropsNothing),
            new Case("creativeBreakDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::creativeBreakDropsNothing),
            new Case("sneakingBareHandedGrassDropsNoGrass", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingBareHandedGrassDropsNoGrass),
            new Case("sneakingShearsGrassDrops", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingShearsGrassDrops),
            new Case("unsneakingShearsDropNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::unsneakingShearsDropNothing),
            new Case("sneakingShearsTallGrassUpperHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingShearsTallGrassUpperHalfDropsOnce),
            new Case("sneakingShearsTallGrassLowerHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingShearsTallGrassLowerHalfDropsOnce),
            new Case("sneakingBreakUpperHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingBreakUpperHalfDropsOnce),
            new Case("sneakingBreakLowerHalfDropsOnce", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingBreakLowerHalfDropsOnce),
            new Case("nonSneakingBreakUpperHalfDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::nonSneakingBreakUpperHalfDropsNothing),
            new Case("nonSneakingBreakLowerHalfDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::nonSneakingBreakLowerHalfDropsNothing),
            new Case("stackedDoublePlantOnlyBrokenOneDrops", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::stackedDoublePlantOnlyBrokenOneDrops),
            new Case("explosionDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::explosionDropsNothing),
            new Case("explosionCausedBySneakingPlayerDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::explosionCausedBySneakingPlayerDropsNothing),
            new Case("tntIgnitedBySneakingPlayerDropsNothing", 100, TestConfigs.DEFAULTS, CarefulHarvestTestCases::tntIgnitedBySneakingPlayerDropsNothing),
            new Case("waterFlowDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::waterFlowDropsNothing),
            new Case("pistonDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::pistonDropsNothing),
            new Case("pistonControlDropsUntagged", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::pistonControlDropsUntagged),
            new Case("lostSupportDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::lostSupportDropsNothing),
            new Case("lostSupportDoublePlantDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::lostSupportDoublePlantDropsNothing),
            new Case("sneakingPlayerAsBreakerOutsideBreakDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::sneakingPlayerAsBreakerOutsideBreakDropsNothing),
            new Case("endermanCarriedBlockDropsNothing", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::endermanCarriedBlockDropsNothing),
            new Case("untaggedBlockUnaffected", TIMEOUT, TestConfigs.DEFAULTS, CarefulHarvestTestCases::untaggedBlockUnaffected));

    /** Every shared case; the loader test mods register exactly these. */
    public static final List<Case> ALL = Stream.of(
                    CarefulHarvestTestCases.BEHAVIOUR,
                    ToggleTestCases.CASES,
                    ExemptSoilTestCases.CASES,
                    CauseTestCases.CASES,
                    ConfigTestCases.CASES)
            .flatMap(List::stream)
            .toList();

    private CarefulHarvestTestCases() {
    }

    /** Runs the case called {@code name} (the Fabric wrappers call this). */
    public static void run(String name, GameTestHelper helper) {
        ALL.stream().filter(c -> c.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no test case " + name))
                .body().accept(helper);
    }

    // ----------------------------------------------------------------- helpers

    /** Stone floor (x 0..3, z 0..2) with a grass block under {@link #PLANT}. */
    static void prepareGround(GameTestHelper helper) {
        for (int x = 0; x <= 3; x++) {
            for (int z = 0; z <= 2; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        helper.setBlock(PLANT.below(), Blocks.GRASS_BLOCK);
    }

    /** A real survival {@link ServerPlayer} (not in the player list), sneaking or not. */
    static ServerPlayer player(GameTestHelper helper, GameType gameType, boolean sneaking) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(gameType);
        player.setShiftKeyDown(sneaking);
        return player;
    }

    static ServerPlayer survivalPlayer(GameTestHelper helper, boolean sneaking) {
        return player(helper, GameType.SURVIVAL, sneaking);
    }

    static ServerPlayer withItem(ServerPlayer player, Item item) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        return player;
    }

    static void breakAs(GameTestHelper helper, ServerPlayer player, BlockPos relative) {
        helper.assertTrue(player.gameMode.destroyBlock(helper.absolutePos(relative)), "destroyBlock returned false");
    }

    /** After a few ticks, require the block at {@link #PLANT} gone and no item of the given kind anywhere in the test. */
    static void succeedWithoutDrop(GameTestHelper helper, Block block, Item item) {
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(block, PLANT);
            helper.assertItemEntityNotPresent(item);
            helper.succeed();
        });
    }

    /**
     * Requires exactly {@code count} of {@code item} lying in this test's own area (its bounds plus
     * one block), so items from neighbouring tests of the same batch can never be counted.
     */
    static void assertItemCount(GameTestHelper helper, Item item, int count) {
        int found = 0;
        for (ItemEntity entity : helper.getLevel().getEntities(EntityTypes.ITEM, helper.getBounds().inflate(1.0), Entity::isAlive)) {
            if (entity.getItem().is(item)) {
                found += entity.getItem().getCount();
            }
        }
        helper.assertValueEqual(found, count, item + " item count");
    }

    static void placeDoublePlant(GameTestHelper helper, Block block, BlockPos lower) {
        helper.setBlock(lower, block.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(lower.above(), block.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** After a few ticks: both halves at PLANT/PLANT+1 gone and exactly {@code count} of {@code item} in the whole test. */
    static void succeedWithDoublePlantDrops(GameTestHelper helper, Block block, Item item, int count) {
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(block, PLANT);
            helper.assertBlockNotPresent(block, PLANT.above());
            assertItemCount(helper, item, count);
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------- tests

    public static void tagContainsDefaults(GameTestHelper helper) {
        for (Block block : new Block[] {Blocks.POPPY, Blocks.DANDELION, Blocks.TORCHFLOWER, Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.FERN, Blocks.LARGE_FERN, Blocks.SUNFLOWER, Blocks.PITCHER_PLANT, Blocks.LEAF_LITTER, Blocks.PINK_PETALS,
                Blocks.WILDFLOWERS, Blocks.DEAD_BUSH, Blocks.BUSH, Blocks.FIREFLY_BUSH, Blocks.SHORT_DRY_GRASS, Blocks.TALL_DRY_GRASS,
                Blocks.CACTUS_FLOWER}) {
            helper.assertTrue(block.defaultBlockState().is(CarefulHarvest.CAREFUL_HARVEST), block + " should be tagged");
        }
        for (Block block : new Block[] {Blocks.DIRT, Blocks.OAK_SAPLING, Blocks.WHEAT, Blocks.OAK_LEAVES, Blocks.POTTED_POPPY,
                Blocks.FLOWERING_AZALEA, Blocks.SWEET_BERRY_BUSH, Blocks.TORCHFLOWER_CROP, Blocks.PITCHER_CROP}) {
            helper.assertFalse(block.defaultBlockState().is(CarefulHarvest.CAREFUL_HARVEST), block + " must not be tagged");
        }
        helper.succeed();
    }

    public static void sneakingBreakDrops(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.POPPY);
        breakAs(helper, survivalPlayer(helper, true), PLANT);
        helper.assertBlockNotPresent(Blocks.POPPY, PLANT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            assertItemCount(helper, Items.POPPY, 1);
            helper.succeed();
        });
    }

    public static void nonSneakingBreakDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.POPPY);
        breakAs(helper, survivalPlayer(helper, false), PLANT);
        succeedWithoutDrop(helper, Blocks.POPPY, Items.POPPY);
    }

    public static void creativeBreakDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.POPPY);
        breakAs(helper, player(helper, GameType.CREATIVE, true), PLANT);
        succeedWithoutDrop(helper, Blocks.POPPY, Items.POPPY);
    }

    /** Vanilla loot conditions still apply: grass needs shears even when sneaking. */
    public static void sneakingBareHandedGrassDropsNoGrass(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.SHORT_GRASS);
        breakAs(helper, survivalPlayer(helper, true), PLANT);
        succeedWithoutDrop(helper, Blocks.SHORT_GRASS, Items.SHORT_GRASS);
    }

    public static void sneakingShearsGrassDrops(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.SHORT_GRASS);
        breakAs(helper, withItem(survivalPlayer(helper, true), Items.SHEARS), PLANT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            assertItemCount(helper, Items.SHORT_GRASS, 1);
            helper.succeed();
        });
    }

    public static void unsneakingShearsDropNothing(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.SHORT_GRASS);
        breakAs(helper, withItem(survivalPlayer(helper, false), Items.SHEARS), PLANT);
        succeedWithoutDrop(helper, Blocks.SHORT_GRASS, Items.SHORT_GRASS);
    }

    /** Vanilla: shearing tall grass yields 2 short grass, once, from either half. */
    public static void sneakingShearsTallGrassUpperHalfDropsOnce(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.TALL_GRASS, PLANT);
        breakAs(helper, withItem(survivalPlayer(helper, true), Items.SHEARS), PLANT.above());
        succeedWithDoublePlantDrops(helper, Blocks.TALL_GRASS, Items.SHORT_GRASS, 2);
    }

    public static void sneakingShearsTallGrassLowerHalfDropsOnce(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.TALL_GRASS, PLANT);
        breakAs(helper, withItem(survivalPlayer(helper, true), Items.SHEARS), PLANT);
        succeedWithDoublePlantDrops(helper, Blocks.TALL_GRASS, Items.SHORT_GRASS, 2);
    }

    public static void sneakingBreakUpperHalfDropsOnce(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.SUNFLOWER, PLANT);
        breakAs(helper, survivalPlayer(helper, true), PLANT.above());
        succeedWithDoublePlantDrops(helper, Blocks.SUNFLOWER, Items.SUNFLOWER, 1);
    }

    public static void sneakingBreakLowerHalfDropsOnce(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.SUNFLOWER, PLANT);
        breakAs(helper, survivalPlayer(helper, true), PLANT);
        succeedWithDoublePlantDrops(helper, Blocks.SUNFLOWER, Items.SUNFLOWER, 1);
    }

    public static void nonSneakingBreakUpperHalfDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.SUNFLOWER, PLANT);
        breakAs(helper, survivalPlayer(helper, false), PLANT.above());
        succeedWithDoublePlantDrops(helper, Blocks.SUNFLOWER, Items.SUNFLOWER, 0);
    }

    public static void nonSneakingBreakLowerHalfDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.SUNFLOWER, PLANT);
        breakAs(helper, survivalPlayer(helper, false), PLANT);
        succeedWithDoublePlantDrops(helper, Blocks.SUNFLOWER, Items.SUNFLOWER, 0);
    }

    /**
     * Sunflower B stands on top of sunflower A (only possible with commands or mods).
     * Breaking A's upper half while sneaking drops A once; B's lower half loses its
     * support right next to the broken block, but B was not broken by the player.
     */
    public static void stackedDoublePlantOnlyBrokenOneDrops(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.SUNFLOWER, PLANT);
        // B cannot survive on A, so place it without shape updates (like /setblock ... strict).
        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            BlockPos pos = PLANT.above(half == DoubleBlockHalf.LOWER ? 2 : 3);
            helper.getLevel().setBlock(helper.absolutePos(pos),
                    Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, half),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        for (int dy = 0; dy < 4; dy++) {
            helper.assertBlockPresent(Blocks.SUNFLOWER, PLANT.above(dy));
        }
        breakAs(helper, survivalPlayer(helper, true), PLANT.above());
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            for (int dy = 0; dy < 4; dy++) {
                helper.assertBlockNotPresent(Blocks.SUNFLOWER, PLANT.above(dy));
            }
            assertItemCount(helper, Items.SUNFLOWER, 1);
            helper.succeed();
        });
    }

    /**
     * TNT-type explosions have no drop decay, so vanilla would drop every destroyed
     * block. The untagged dirt underneath is the control and must drop.
     */
    public static void explosionDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT.below(), Blocks.DIRT);
        helper.setBlock(PLANT, Blocks.POPPY);
        Vec3 center = helper.absoluteVec(Vec3.atCenterOf(PLANT));
        helper.getLevel().explode(null, center.x, center.y, center.z, 2.0F, Level.ExplosionInteraction.TNT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(Blocks.POPPY, PLANT);
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.assertItemEntityPresent(Items.DIRT);
            helper.succeed();
        });
    }

    /** Even if a sneaking player is the explosion's direct source, explosions never drop. */
    public static void explosionCausedBySneakingPlayerDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT.below(), Blocks.DIRT);
        helper.setBlock(PLANT, Blocks.POPPY);
        ServerPlayer player = survivalPlayer(helper, true);
        Vec3 center = helper.absoluteVec(Vec3.atCenterOf(PLANT));
        helper.getLevel().explode(player, center.x, center.y, center.z, 2.0F, Level.ExplosionInteraction.TNT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(Blocks.POPPY, PLANT);
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.assertItemEntityPresent(Items.DIRT);
            helper.succeed();
        });
    }

    /**
     * Real primed TNT owned by a sneaking player, ticking down and exploding by itself,
     * inside an obsidian pit so the blast stays in the test area.
     */
    public static void tntIgnitedBySneakingPlayerDropsNothing(GameTestHelper helper) {
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                for (int y = 0; y <= 4; y++) {
                    boolean wall = x == 0 || x == 4 || z == 0 || z == 4 || y == 0;
                    if (wall) {
                        helper.setBlock(new BlockPos(x, y, z), Blocks.OBSIDIAN);
                    }
                }
            }
        }
        BlockPos poppy = new BlockPos(2, 2, 2);
        helper.setBlock(poppy.below(), Blocks.DIRT);
        helper.setBlock(poppy, Blocks.POPPY);
        ServerPlayer player = survivalPlayer(helper, true);
        Vec3 tntPos = helper.absoluteVec(Vec3.atBottomCenterOf(poppy.above()));
        PrimedTnt tnt = new PrimedTnt(helper.getLevel(), tntPos.x, tntPos.y, tntPos.z, player);
        tnt.setFuse(5);
        helper.getLevel().addFreshEntity(tnt);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(tnt.isRemoved(), "TNT should have exploded");
            helper.assertBlockNotPresent(Blocks.POPPY, poppy);
            helper.assertBlockNotPresent(Blocks.DIRT, poppy.below());
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.assertItemEntityPresent(Items.DIRT);
            helper.succeed();
        });
    }

    /**
     * Water washes away the poppy (no drop) and, as a control, an untagged torch (drops).
     * Runs the real fluid tick ({@code FluidState#tick}, i.e. {@code FlowingFluid#spread})
     * directly for the source and the first flowing block, so the test does not depend
     * on scheduled-tick timing.
     */
    public static void waterFlowDropsNothing(GameTestHelper helper) {
        // A walled channel along +x so the water can only flow east:
        // water (1,2,1) -> poppy (2,2,1) -> torch (3,2,1).
        for (int x = 0; x <= 5; x++) {
            for (int z = 0; z <= 2; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
            helper.setBlock(new BlockPos(x, 2, 0), Blocks.STONE);
            helper.setBlock(new BlockPos(x, 2, 2), Blocks.STONE);
        }
        helper.setBlock(new BlockPos(0, 2, 1), Blocks.STONE);
        BlockPos water = new BlockPos(1, 2, 1);
        BlockPos poppy = new BlockPos(2, 2, 1);
        BlockPos torch = new BlockPos(3, 2, 1);
        helper.setBlock(poppy.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(poppy, Blocks.POPPY);
        helper.setBlock(torch, Blocks.TORCH);
        helper.setBlock(water, Blocks.WATER);

        ServerLevel level = helper.getLevel();
        for (BlockPos rel : new BlockPos[] {water, poppy}) {
            BlockPos abs = helper.absolutePos(rel);
            BlockState state = level.getBlockState(abs);
            state.getFluidState().tick(level, abs, state);
        }
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockPresent(Blocks.WATER, poppy);
            helper.assertBlockPresent(Blocks.WATER, torch);
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.assertItemEntityPresent(Items.TORCH);
            helper.succeed();
        });
    }

    /** A piston extends into {@code target} (at x=2) when a redstone block powers it. */
    static void pistonInto(GameTestHelper helper, Block target, Item item, boolean expectDrop) {
        prepareGround(helper);
        BlockPos piston = new BlockPos(1, 2, 1);
        BlockPos targetPos = new BlockPos(2, 2, 1);
        helper.setBlock(targetPos.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(piston, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.EAST));
        helper.setBlock(targetPos, target);
        helper.setBlock(piston.west(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockPresent(Blocks.PISTON_HEAD, targetPos);
            if (expectDrop) {
                assertItemCount(helper, item, 1);
            } else {
                helper.assertItemEntityNotPresent(item);
            }
            helper.succeed();
        });
    }

    public static void pistonDropsNothing(GameTestHelper helper) {
        pistonInto(helper, Blocks.POPPY, Items.POPPY, false);
    }

    public static void pistonControlDropsUntagged(GameTestHelper helper) {
        pistonInto(helper, Blocks.TORCH, Items.TORCH, true);
    }

    /**
     * A sneaking player breaks the grass block under a poppy: the poppy pops off, but
     * that is not the player breaking the poppy, so it must not drop.
     */
    public static void lostSupportDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.POPPY);
        breakAs(helper, withItem(survivalPlayer(helper, true), Items.DIAMOND_SHOVEL), PLANT.below());
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(Blocks.POPPY, PLANT);
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.assertItemEntityPresent(Items.DIRT);
            helper.succeed();
        });
    }

    /** Same with a double plant, whose lower half sits directly on the broken block. */
    public static void lostSupportDoublePlantDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        placeDoublePlant(helper, Blocks.SUNFLOWER, PLANT);
        breakAs(helper, withItem(survivalPlayer(helper, true), Items.DIAMOND_SHOVEL), PLANT.below());
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertBlockNotPresent(Blocks.SUNFLOWER, PLANT);
            helper.assertBlockNotPresent(Blocks.SUNFLOWER, PLANT.above());
            helper.assertItemEntityNotPresent(Items.SUNFLOWER);
            helper.assertItemEntityPresent(Items.DIRT);
            helper.succeed();
        });
    }

    /**
     * Code outside a player break (for example another mod) destroys a tagged block and
     * names a sneaking player as the breaker. That is not the player breaking it, and a
     * sneaking break of another block just before must not leave anything behind.
     */
    public static void sneakingPlayerAsBreakerOutsideBreakDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        BlockPos other = new BlockPos(2, 2, 1);
        helper.setBlock(other.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(other, Blocks.DANDELION);
        helper.setBlock(PLANT, Blocks.POPPY);
        ServerPlayer player = survivalPlayer(helper, true);
        breakAs(helper, player, other);
        helper.getLevel().destroyBlock(helper.absolutePos(PLANT), true, player);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            assertItemCount(helper, Items.DANDELION, 1);
            helper.assertBlockNotPresent(Blocks.POPPY, PLANT);
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.succeed();
        });
    }

    /** An enderman carrying a tagged block drops nothing for it; carried dirt (control) drops. */
    public static void endermanCarriedBlockDropsNothing(GameTestHelper helper) {
        prepareGround(helper);
        Enderman withPoppy = helper.spawn(EntityTypes.ENDERMAN, new BlockPos(1, 2, 1));
        Enderman withDirt = helper.spawn(EntityTypes.ENDERMAN, new BlockPos(2, 2, 1));
        withPoppy.setCarriedBlock(Blocks.POPPY.defaultBlockState());
        withDirt.setCarriedBlock(Blocks.DIRT.defaultBlockState());
        withPoppy.kill(helper.getLevel());
        withDirt.kill(helper.getLevel());
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            helper.assertItemEntityNotPresent(Items.POPPY);
            helper.assertItemEntityPresent(Items.DIRT);
            helper.succeed();
        });
    }

    public static void untaggedBlockUnaffected(GameTestHelper helper) {
        prepareGround(helper);
        helper.setBlock(PLANT, Blocks.DIRT);
        breakAs(helper, survivalPlayer(helper, false), PLANT);
        helper.assertBlockNotPresent(Blocks.DIRT, PLANT);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            assertItemCount(helper, Items.DIRT, 1);
            helper.succeed();
        });
    }
}
