package dev.carefulharvest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.carefulharvest.DestructionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    /**
     * Remember which block a player is breaking, and whether they are sneaking at that moment,
     * for the duration of the break. Only that block (and the other half of a double-tall
     * plant) counts as broken by the player; everything destroyed as a side effect of the break
     * gets its own cause.
     */
    @WrapMethod(method = "destroyBlock")
    private boolean carefulharvest$trackPlayerBreak(BlockPos pos, Operation<Boolean> original) {
        DestructionContext.push(new DestructionContext.PlayerBreak(
                this.player, pos.immutable(), this.level.getBlockState(pos), this.player.isShiftKeyDown()));
        try {
            return original.call(pos);
        } finally {
            DestructionContext.pop();
        }
    }
}
