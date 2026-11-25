package dev.carefulharvest.neoforge;

import dev.carefulharvest.CarefulHarvest;
import dev.carefulharvest.client.CarefulHarvestConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client-only entrypoint: the config screen, opened from the mods list ("Config" button). */
@Mod(value = CarefulHarvest.MOD_ID, dist = Dist.CLIENT)
public final class CarefulHarvestNeoForgeClient {
    public CarefulHarvestNeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (mod, parent) -> new CarefulHarvestConfigScreen(parent));
    }
}
