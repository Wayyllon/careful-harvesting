package dev.carefulharvest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.carefulharvest.DestructionContext;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
    /**
     * The block-destroying part of an explosion, with the positions it blows up, so that a
     * plant in the blast counts as an explosion drop even if its soil happens to be processed
     * first (the list is shuffled) and the plant pops off for lack of support.
     */
    @WrapMethod(method = "interactWithBlocks")
    private void carefulharvest$trackExplosion(List<BlockPos> targetBlocks, Operation<Void> original) {
        DestructionContext.push(DestructionContext.explosionBlocks(targetBlocks));
        try {
            original.call(targetBlocks);
        } finally {
            DestructionContext.pop();
        }
    }
}
