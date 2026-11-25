package dev.carefulharvest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.carefulharvest.CarefulHarvest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Shadow
    @Final
    private Level level;

    /**
     * Every block change in a loaded chunk ({@code Level#setBlock} on both loaders) swaps the state
     * in the chunk section here; the previous state is the value of that call. Exempt soil that is
     * replaced is remembered for the rest of the tick. Hooked at the swap itself (not at the
     * method's return) so it also sees changes whose method returns early, runs before any
     * neighbour reacts, and costs no callback allocation: this is one of the hottest paths in the
     * game, so everything except a different block on a server level returns at once.
     * Only {@code LevelChunk} is hooked: world generation writes to proto chunks, and client levels
     * are skipped.
     */
    @ModifyExpressionValue(method = "setBlockState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState carefulharvest$rememberRemovedSoil(BlockState oldState, @Local(argsOnly = true) BlockPos pos,
                                                         @Local(argsOnly = true) BlockState newState) {
        if (oldState.getBlock() != newState.getBlock() && !this.level.isClientSide()) {
            CarefulHarvest.onBlockReplaced(this.level, pos, oldState, newState);
        }
        return oldState;
    }
}
