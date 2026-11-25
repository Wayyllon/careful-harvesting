package dev.carefulharvest.mixin;

import dev.carefulharvest.SoilMemory;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    /** The removed-soil memory only lasts for the tick it was recorded in. */
    @Inject(method = "tickServer", at = @At("RETURN"))
    private void carefulharvest$forgetRemovedSoil(BooleanSupplier haveTime, CallbackInfo ci) {
        SoilMemory.clear();
    }
}
