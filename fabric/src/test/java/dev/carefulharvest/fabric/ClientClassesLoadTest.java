package dev.carefulharvest.fabric;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.terraformersmc.modmenu.api.ModMenuApi;
import org.junit.jupiter.api.Test;

/**
 * The config screen needs a running client, which the build cannot start. This at least loads,
 * links and verifies the client-only classes against the game and Mod Menu jars the mod is built
 * against (the game test server refuses to load client classes at all).
 */
class ClientClassesLoadTest {
    @Test
    void configScreenAndModMenuEntrypointLoad() throws ReflectiveOperationException {
        ClassLoader loader = getClass().getClassLoader();
        for (String name : new String[] {"dev.carefulharvest.client.CarefulHarvestConfigScreen",
                "dev.carefulharvest.fabric.client.CarefulHarvestModMenu"}) {
            Class<?> type = Class.forName(name, true, loader);
            assertNotNull(type.getDeclaredMethods());
            assertNotNull(type.getDeclaredConstructors());
        }
        Class<?> entrypoint = Class.forName("dev.carefulharvest.fabric.client.CarefulHarvestModMenu", true, loader);
        assertTrue(ModMenuApi.class.isAssignableFrom(entrypoint));
        ModMenuApi api = (ModMenuApi) entrypoint.getDeclaredConstructor().newInstance();
        assertNotNull(api.getModConfigScreenFactory(), "config screen factory");
    }
}
