package dev.carefulharvest.neoforge.test;

import com.mojang.serialization.MapCodec;
import dev.carefulharvest.test.CarefulHarvestTestCases;
import dev.carefulharvest.test.ConfigEnvironment;
import dev.carefulharvest.test.TestConfigs;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge registration of the shared {@link CarefulHarvestTestCases}. Development-only
 * mod, run with {@code ./gradlew :neoforge:runGameTestServer} (also part of
 * {@code ./gradlew build}). Each {@link TestConfigs} config is a test environment
 * ({@link ConfigEnvironment}); the runner runs one environment's batch at a time.
 */
@Mod(CarefulHarvestNeoForgeGameTests.MOD_ID)
public final class CarefulHarvestNeoForgeGameTests {
    public static final String MOD_ID = "carefulharvest_gametest";

    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends TestEnvironmentDefinition<?>>> ENVIRONMENT_TYPES =
            DeferredRegister.create(Registries.TEST_ENVIRONMENT_DEFINITION_TYPE, MOD_ID);

    static {
        for (CarefulHarvestTestCases.Case testCase : CarefulHarvestTestCases.ALL) {
            TEST_FUNCTIONS.register(path(testCase), testCase::body);
        }
        ENVIRONMENT_TYPES.register(ConfigEnvironment.TYPE_NAME, () -> ConfigEnvironment.CODEC);
    }

    public CarefulHarvestNeoForgeGameTests(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
        ENVIRONMENT_TYPES.register(modBus);
        modBus.addListener(CarefulHarvestNeoForgeGameTests::registerTests);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /** camelCase case name to a valid identifier path. */
    private static String path(CarefulHarvestTestCases.Case testCase) {
        return testCase.name().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Map<String, Holder<TestEnvironmentDefinition<?>>> environments = new HashMap<>();
        for (String variant : TestConfigs.ALL.keySet()) {
            environments.put(variant, event.registerEnvironment(id(variant), new ConfigEnvironment(variant)));
        }
        for (CarefulHarvestTestCases.Case testCase : CarefulHarvestTestCases.ALL) {
            Identifier id = id(path(testCase));
            event.registerTest(id, new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, id),
                    new TestData<>(environments.get(testCase.environment()), id("empty"), testCase.maxTicks(), 0, true)));
        }
    }
}
