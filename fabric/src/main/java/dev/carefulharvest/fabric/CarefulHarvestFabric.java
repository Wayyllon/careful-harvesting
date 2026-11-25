package dev.carefulharvest.fabric;

import dev.carefulharvest.CarefulHarvestCommand;
import dev.carefulharvest.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

/** Fabric entrypoint (both sides): loads the config and registers {@code /carefulharvest}. */
public final class CarefulHarvestFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.init(FabricLoader.getInstance().getConfigDir());
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> CarefulHarvestCommand.register(dispatcher));
    }
}
