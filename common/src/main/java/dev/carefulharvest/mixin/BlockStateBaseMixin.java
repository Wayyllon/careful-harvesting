package dev.carefulharvest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.carefulharvest.CarefulHarvest;
import dev.carefulharvest.DestructionContext;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow
    protected abstract BlockState asState();

    /**
     * Single choke point for block loot: every drop path in vanilla
     * ({@code Block.dropResources} overloads, {@code Block.getDrops} statics,
     * explosions, pistons, fluids, lost support via {@code Level.destroyBlock})
     * ends up here.
     */
    @Inject(method = "getDrops", at = @At("HEAD"), cancellable = true)
    private void carefulharvest$suppressCarelessDrops(LootParams.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (CarefulHarvest.shouldSuppressDrops(this.asState(), builder)) {
            cir.setReturnValue(List.of());
        }
    }

    /**
     * Mark the duration of an explosion's per-block handling (loot, removal and the shape
     * updates that follow) as an explosion.
     */
    @WrapMethod(method = "onExplosionHit")
    private void carefulharvest$trackExplosion(ServerLevel level, BlockPos pos, Explosion explosion,
                                              BiConsumer<ItemStack, BlockPos> onDrop, Operation<Void> original) {
        DestructionContext.push(new DestructionContext.ExplosionHit(pos.immutable()));
        try {
            original.call(level, pos, explosion, onDrop);
        } finally {
            DestructionContext.pop();
        }
    }
}
