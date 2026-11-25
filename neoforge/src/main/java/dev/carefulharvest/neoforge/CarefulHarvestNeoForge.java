package dev.carefulharvest.neoforge;

import dev.carefulharvest.CarefulHarvest;
import dev.carefulharvest.CarefulHarvestCommand;
import dev.carefulharvest.config.ConfigManager;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * NeoForge entrypoint (both sides): loads the config and registers {@code /carefulharvest}.
 * The drop logic lives in the common mixins and the {@code carefulharvest:careful_harvest} tag.
 */
@Mod(CarefulHarvest.MOD_ID)
public final class CarefulHarvestNeoForge {
    public CarefulHarvestNeoForge() {
        ConfigManager.init(FMLPaths.CONFIGDIR.get());
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> CarefulHarvestCommand.register(event.getDispatcher()));
    }
}
