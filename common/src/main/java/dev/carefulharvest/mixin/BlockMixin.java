package dev.carefulharvest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.carefulharvest.DestructionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockMixin {
    /**
     * A shape update that turns a block into air: a plant that can no longer survive (its soil
     * is gone) or the remaining half of a double-tall plant. Both end in
     * {@code Level#destroyBlock(pos, true, null)}, which evaluates the loot.
     */
    @WrapMethod(method = "updateOrDestroy(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V")
    private static void carefulharvest$trackShapeDestroy(BlockState blockState, BlockState newState, LevelAccessor level,
                                                         BlockPos pos, int updateFlags, int updateLimit, Operation<Void> original) {
        if (newState == blockState || !newState.isAir() || level.isClientSide()) {
            original.call(blockState, newState, level, pos, updateFlags, updateLimit);
            return;
        }
        DestructionContext.push(new DestructionContext.ShapeDestroy(pos.immutable(), blockState));
        try {
            original.call(blockState, newState, level, pos, updateFlags, updateLimit);
        } finally {
            DestructionContext.pop();
        }
    }
}
