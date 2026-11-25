package dev.carefulharvest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jspecify.annotations.Nullable;

/**
 * Works out the {@link DropCause} of a block's loot.
 *
 * <p>The mixins push a frame on a per-thread stack for the duration of each vanilla code path
 * that destroys blocks:
 * <ul>
 *   <li>{@link PlayerBreak}: {@code ServerPlayerGameMode#destroyBlock} (every survival block break)</li>
 *   <li>{@link ExplosionBlocks}: {@code ServerExplosion#interactWithBlocks}, with the list of blown-up positions</li>
 *   <li>{@link ExplosionHit}: {@code BlockStateBase#onExplosionHit} for one block</li>
 *   <li>{@link FluidFlow}: {@code FlowingFluid#spreadTo} (water and lava replacing a block)</li>
 *   <li>{@link ShapeDestroy}: {@code Block#updateOrDestroy} turning a block into air because a neighbour
 *       changed (a plant whose soil is gone, or the second half of a double-tall plant)</li>
 * </ul>
 *
 * <p>When loot is evaluated, the innermost frame decides:
 * <ul>
 *   <li>{@code ShapeDestroy} of the half of a double-tall plant, directly inside a frame for its other
 *       half: the half follows the other half, so continue with the next frame as if the other half was
 *       being dropped. This is how vanilla drops double plants (the item comes from whichever half has
 *       the loot), so both halves always get the same cause and the plant drops exactly once.</li>
 *   <li>Any other {@code ShapeDestroy}: {@link DropCause#SUPPORT_BROKEN}, unless an explosion that is
 *       being processed includes the plant's own position. Explosions process their blocks in random
 *       order, so a plant in the blast would otherwise sometimes count as an explosion drop and
 *       sometimes as lost support, depending on whether its soil was handled first.</li>
 *   <li>{@code PlayerBreak}: {@link DropCause#SNEAKING_BREAK} or {@link DropCause#NON_SNEAKING_BREAK} for
 *       exactly the block being broken (and its other half); anything else destroyed during the break
 *       without a frame of its own is {@link DropCause#OTHER}.</li>
 *   <li>{@code ExplosionHit} / {@code ExplosionBlocks}: {@link DropCause#EXPLOSION}.</li>
 *   <li>{@code FluidFlow}: {@link DropCause#FLUID}.</li>
 *   <li>No frame: {@link DropCause#OTHER} (pistons, enderman deaths, {@code /loot}, other mods, ...).</li>
 * </ul>
 */
public final class DestructionContext {
    /** A destruction in progress. */
    public sealed interface Frame {
        /** The block position this frame destroys, or {@code null} if it is not about a single block. */
        @Nullable BlockPos pos();
    }

    public record PlayerBreak(Player player, BlockPos pos, BlockState state, boolean sneaking) implements Frame {
    }

    public record ExplosionBlocks(List<BlockPos> blocks, Set<BlockPos>[] lazySet) implements Frame {
        @Override
        public @Nullable BlockPos pos() {
            return null;
        }

        boolean contains(BlockPos pos) {
            if (this.blocks.size() <= 16) {
                return this.blocks.contains(pos);
            }
            if (this.lazySet[0] == null) {
                this.lazySet[0] = new HashSet<>(this.blocks);
            }
            return this.lazySet[0].contains(pos);
        }
    }

    public record ExplosionHit(BlockPos pos) implements Frame {
    }

    public record FluidFlow(BlockPos pos) implements Frame {
    }

    public record ShapeDestroy(BlockPos pos, BlockState state) implements Frame {
    }

    private static final class Stack {
        private Frame[] frames = new Frame[16];
        private int size;
    }

    private static final ThreadLocal<Stack> STACK = ThreadLocal.withInitial(Stack::new);

    private DestructionContext() {
    }

    /** Pushes a frame; the caller must {@link #pop()} it in a {@code finally} block. */
    public static void push(Frame frame) {
        Stack stack = STACK.get();
        if (stack.size == stack.frames.length) {
            stack.frames = Arrays.copyOf(stack.frames, stack.size * 2);
        }
        stack.frames[stack.size++] = frame;
    }

    public static void pop() {
        Stack stack = STACK.get();
        stack.frames[--stack.size] = null;
    }

    /** Number of frames on this thread's stack (0 outside every tracked destruction; tests). */
    public static int depth() {
        return STACK.get().size;
    }

    @SuppressWarnings("unchecked")
    public static ExplosionBlocks explosionBlocks(List<BlockPos> blocks) {
        return new ExplosionBlocks(blocks, (Set<BlockPos>[]) new Set<?>[1]);
    }

    /**
     * @param state   the block whose loot is being evaluated
     * @param pos     its position
     * @param breaker the {@code this_entity} of the loot context, if any
     */
    public static DropCause classify(BlockState state, BlockPos pos, @Nullable Entity breaker) {
        Stack stack = STACK.get();
        BlockPos target = pos;
        BlockState targetState = state;
        boolean viaOtherHalf = false;
        for (int i = stack.size - 1; i >= 0; i--) {
            Frame frame = stack.frames[i];
            switch (frame) {
                case ShapeDestroy shape -> {
                    if (!shape.pos().equals(target)) {
                        return DropCause.OTHER;
                    }
                    BlockPos otherHalf = otherHalf(target, targetState);
                    if (otherHalf != null && i > 0 && otherHalf.equals(stack.frames[i - 1].pos())) {
                        target = otherHalf;
                        targetState = flipHalf(targetState);
                        viaOtherHalf = true;
                        continue;
                    }
                    return inExplosion(stack, i, target, targetState) ? DropCause.EXPLOSION : DropCause.SUPPORT_BROKEN;
                }
                case PlayerBreak playerBreak -> {
                    return classifyPlayerBreak(playerBreak, target, targetState, breaker, viaOtherHalf);
                }
                case ExplosionHit _, ExplosionBlocks _ -> {
                    return DropCause.EXPLOSION;
                }
                case FluidFlow _ -> {
                    return DropCause.FLUID;
                }
            }
        }
        return DropCause.OTHER;
    }

    private static DropCause classifyPlayerBreak(PlayerBreak current, BlockPos target, BlockState targetState,
                                                 @Nullable Entity breaker, boolean viaOtherHalf) {
        DropCause cause = current.sneaking() ? DropCause.SNEAKING_BREAK : DropCause.NON_SNEAKING_BREAK;
        if (!targetState.is(current.state().getBlock())) {
            return DropCause.OTHER;
        }
        // The broken block itself: vanilla passes the player as this_entity.
        if (target.equals(current.pos()) && (breaker == current.player() || viaOtherHalf && breaker == null)) {
            return cause;
        }
        // The other half of the broken double plant, removed some other way than a shape update
        // (no entity in the loot context).
        if ((breaker == null || breaker == current.player()) && target.equals(otherHalf(current.pos(), current.state()))
                && targetState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && targetState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != current.state().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return cause;
        }
        return DropCause.OTHER;
    }

    private static boolean inExplosion(Stack stack, int below, BlockPos target, BlockState targetState) {
        BlockPos otherHalf = otherHalf(target, targetState);
        for (int i = below - 1; i >= 0; i--) {
            if (stack.frames[i] instanceof ExplosionBlocks blocks
                    && (blocks.contains(target) || otherHalf != null && blocks.contains(otherHalf))) {
                return true;
            }
        }
        return false;
    }

    /** The position of the other half if {@code state} is half of a double-tall block. */
    public static @Nullable BlockPos otherHalf(BlockPos pos, BlockState state) {
        if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return null;
        }
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
    }

    public static boolean isUpperHalf(BlockState state) {
        return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
    }

    private static BlockState flipHalf(BlockState state) {
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        return state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, half == DoubleBlockHalf.LOWER ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
    }
}
