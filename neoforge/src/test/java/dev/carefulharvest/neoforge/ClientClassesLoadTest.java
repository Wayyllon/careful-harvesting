package dev.carefulharvest.neoforge;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.junit.jupiter.api.Test;

/**
 * The config screen needs a running client, which the build cannot start. This at least loads,
 * links and verifies the client-only classes against the NeoForge and game jars the mod is built
 * against (the game test server is a dedicated server, which never loads them).
 */
class ClientClassesLoadTest {
    @Test
    void configScreenAndClientEntrypointLoad() throws ReflectiveOperationException {
        ClassLoader loader = getClass().getClassLoader();
        for (String name : new String[] {"dev.carefulharvest.client.CarefulHarvestConfigScreen",
                "dev.carefulharvest.neoforge.CarefulHarvestNeoForgeClient"}) {
            Class<?> type = Class.forName(name, true, loader);
            assertNotNull(type.getDeclaredMethods());
            assertNotNull(type.getDeclaredConstructors());
        }
        Mod mod = Class.forName("dev.carefulharvest.neoforge.CarefulHarvestNeoForgeClient", false, loader).getAnnotation(Mod.class);
        assertNotNull(mod, "@Mod");
        assertArrayEquals(new Dist[] {Dist.CLIENT}, mod.dist(), "client only");
        assertNotNull(Class.forName("dev.carefulharvest.neoforge.CarefulHarvestNeoForgeClient", false, loader)
                .getConstructor(net.neoforged.fml.ModContainer.class));
    }
}
