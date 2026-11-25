package dev.carefulharvest.test;

import dev.carefulharvest.config.CarefulHarvestConfig;
import dev.carefulharvest.config.ExemptSoil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The configs the game tests run under. Each one is a test environment
 * ({@link ConfigEnvironment}, id {@code carefulharvest_gametest:<name>}): the game test runner
 * runs one environment's batch at a time and sets the config up before the batch and restores
 * the previous one after it, so tests that need different configs never run at the same time.
 */
public final class TestConfigs {
    public static final String DEFAULTS = "defaults";
    public static final String NON_SNEAKING_BREAK = "non_sneaking_break";
    public static final String FLUIDS = "fluids";
    public static final String EXPLOSIONS = "explosions";
    public static final String SUPPORT_BROKEN = "support_broken";
    public static final String OTHER = "other";
    public static final String NO_EXEMPT_SOIL = "no_exempt_soil";
    public static final String EXEMPT_DIRT_TAG = "exempt_dirt_tag";
    /** Default config; the reload command test changes the active config and file location itself. */
    public static final String RELOAD_COMMAND = "reload_command";

    private static final CarefulHarvestConfig D = CarefulHarvestConfig.DEFAULT;

    public static final Map<String, CarefulHarvestConfig> ALL = create();

    private TestConfigs() {
    }

    private static Map<String, CarefulHarvestConfig> create() {
        Map<String, CarefulHarvestConfig> configs = new LinkedHashMap<>();
        configs.put(DEFAULTS, D);
        configs.put(NON_SNEAKING_BREAK, D.withNonSneakingBreak(true));
        configs.put(FLUIDS, D.withFluids(true));
        configs.put(EXPLOSIONS, D.withExplosions(true));
        configs.put(SUPPORT_BROKEN, D.withSupportBroken(true));
        configs.put(OTHER, D.withOther(true));
        configs.put(NO_EXEMPT_SOIL, D.withExemptSoil(ExemptSoil.parse(List.of(), w -> {
            throw new IllegalStateException(w);
        })));
        configs.put(EXEMPT_DIRT_TAG, D.withExemptSoil(ExemptSoil.parse(List.of("#minecraft:dirt"), w -> {
            throw new IllegalStateException(w);
        })));
        configs.put(RELOAD_COMMAND, D);
        return Map.copyOf(configs);
    }

    public static CarefulHarvestConfig get(String name) {
        CarefulHarvestConfig config = ALL.get(name);
        if (config == null) {
            throw new IllegalArgumentException("No test config " + name);
        }
        return config;
    }
}
