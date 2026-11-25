package dev.carefulharvest.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.carefulharvest.client.CarefulHarvestConfigScreen;

/**
 * Mod Menu integration (the {@code modmenu} entrypoint). Mod Menu is optional: it is only a
 * compile-time dependency, and this class is only loaded by Mod Menu, on the client.
 */
public final class CarefulHarvestModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CarefulHarvestConfigScreen::new;
    }
}
