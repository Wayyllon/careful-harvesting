package dev.carefulharvest.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.carefulharvest.config.ConfigManager;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;

/**
 * Test environment type {@code carefulharvest_gametest:config}: makes {@link TestConfigs#get}
 * of {@code variant} the active config for one batch of tests, and puts the previous config
 * (and config file location) back afterwards.
 */
public record ConfigEnvironment(String variant) implements TestEnvironmentDefinition<ConfigManager.State> {
    public static final String TYPE_NAME = "config";

    public static final MapCodec<ConfigEnvironment> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("variant").forGetter(ConfigEnvironment::variant)).apply(i, ConfigEnvironment::new));

    @Override
    public ConfigManager.State setup(ServerLevel level) {
        ConfigManager.State previous = ConfigManager.snapshot();
        ConfigManager.set(TestConfigs.get(this.variant));
        return previous;
    }

    @Override
    public void teardown(ServerLevel level, ConfigManager.State previous) {
        ConfigManager.restore(previous);
    }

    @Override
    public MapCodec<ConfigEnvironment> codec() {
        return CODEC;
    }
}
