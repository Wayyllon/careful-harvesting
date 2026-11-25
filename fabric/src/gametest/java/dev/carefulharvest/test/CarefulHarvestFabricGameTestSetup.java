package dev.carefulharvest.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Registers the {@code carefulharvest_gametest:config} test environment type. */
public final class CarefulHarvestFabricGameTestSetup implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE,
                Identifier.fromNamespaceAndPath("carefulharvest_gametest", ConfigEnvironment.TYPE_NAME), ConfigEnvironment.CODEC);
    }
}
