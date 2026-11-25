package dev.carefulharvest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.carefulharvest.DestructionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
    /**
     * A fluid flowing into {@code pos}: {@code beforeDestroyingBlock} (water drops the block's
     * loot, lava only fizzes) followed by replacing the block with the fluid.
     * {@code LavaFluid} overrides this method but calls {@code super}.
     */
    @WrapMethod(method = "spreadTo")
    private void carefulharvest$trackFluid(LevelAccessor level, BlockPos pos, BlockState state, Direction direction,
                                           FluidState target, Operation<Void> original) {
        DestructionContext.push(new DestructionContext.FluidFlow(pos.immutable()));
        try {
            original.call(level, pos, state, direction, target);
        } finally {
            DestructionContext.pop();
        }
    }
}
