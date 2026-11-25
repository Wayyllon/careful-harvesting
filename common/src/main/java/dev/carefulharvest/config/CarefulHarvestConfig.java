package dev.carefulharvest.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.carefulharvest.DropCause;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The contents of {@code config/carefulharvest.json}. Immutable; see {@link ConfigManager} for
 * the active instance.
 *
 * <p>Each boolean says whether that cause produces drops for tagged plants. Breaking a plant
 * while sneaking always drops.
 *
 * @param exemptSoil plants standing on these blocks are not affected by the mod at all
 */
public record CarefulHarvestConfig(
        boolean nonSneakingBreak,
        boolean fluids,
        boolean explosions,
        boolean supportBroken,
        boolean other,
        ExemptSoil exemptSoil) {

    public static final String NON_SNEAKING_BREAK = "nonSneakingBreak";
    public static final String FLUIDS = "fluids";
    public static final String EXPLOSIONS = "explosions";
    public static final String SUPPORT_BROKEN = "supportBroken";
    public static final String OTHER = "other";
    public static final String EXEMPT_SOIL = "exemptSoil";
    private static final Set<String> KEYS = Set.of(NON_SNEAKING_BREAK, FLUIDS, EXPLOSIONS, SUPPORT_BROKEN, OTHER, EXEMPT_SOIL);

    /** All causes off (the 1.0 behaviour), default exempt soil. */
    public static final CarefulHarvestConfig DEFAULT =
            new CarefulHarvestConfig(false, false, false, false, false, ExemptSoil.defaults());

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public boolean dropsFor(DropCause cause) {
        return switch (cause) {
            case SNEAKING_BREAK -> true;
            case NON_SNEAKING_BREAK -> this.nonSneakingBreak;
            case FLUID -> this.fluids;
            case EXPLOSION -> this.explosions;
            case SUPPORT_BROKEN -> this.supportBroken;
            case OTHER -> this.other;
        };
    }

    public CarefulHarvestConfig withNonSneakingBreak(boolean value) {
        return new CarefulHarvestConfig(value, this.fluids, this.explosions, this.supportBroken, this.other, this.exemptSoil);
    }

    public CarefulHarvestConfig withFluids(boolean value) {
        return new CarefulHarvestConfig(this.nonSneakingBreak, value, this.explosions, this.supportBroken, this.other, this.exemptSoil);
    }

    public CarefulHarvestConfig withExplosions(boolean value) {
        return new CarefulHarvestConfig(this.nonSneakingBreak, this.fluids, value, this.supportBroken, this.other, this.exemptSoil);
    }

    public CarefulHarvestConfig withSupportBroken(boolean value) {
        return new CarefulHarvestConfig(this.nonSneakingBreak, this.fluids, this.explosions, value, this.other, this.exemptSoil);
    }

    public CarefulHarvestConfig withOther(boolean value) {
        return new CarefulHarvestConfig(this.nonSneakingBreak, this.fluids, this.explosions, this.supportBroken, value, this.exemptSoil);
    }

    public CarefulHarvestConfig withExemptSoil(ExemptSoil value) {
        return new CarefulHarvestConfig(this.nonSneakingBreak, this.fluids, this.explosions, this.supportBroken, this.other, value);
    }

    // ------------------------------------------------------------------ JSON

    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(NON_SNEAKING_BREAK, this.nonSneakingBreak);
        json.addProperty(FLUIDS, this.fluids);
        json.addProperty(EXPLOSIONS, this.explosions);
        json.addProperty(SUPPORT_BROKEN, this.supportBroken);
        json.addProperty(OTHER, this.other);
        JsonArray soil = new JsonArray();
        this.exemptSoil.entries().forEach(soil::add);
        json.add(EXEMPT_SOIL, soil);
        return GSON.toJson(json) + "\n";
    }

    /**
     * Parses a config file. Never throws: anything wrong is reported to {@code warnings} and
     * replaced with its default. A file that is not a JSON object at all gives {@link #DEFAULT}.
     * Missing keys get their default value (with a warning), unknown keys are ignored (with a
     * warning), and invalid {@code exemptSoil} entries are skipped (with a warning).
     */
    public static CarefulHarvestConfig parse(String text, Consumer<String> warnings) {
        JsonObject json;
        try {
            JsonElement root = JsonParser.parseString(text);
            if (!root.isJsonObject()) {
                warnings.accept("the config file is not a JSON object; using the default config");
                return DEFAULT;
            }
            json = root.getAsJsonObject();
        } catch (RuntimeException e) { // JsonParseException, or anything else Gson throws on garbage
            warnings.accept("the config file is not valid JSON (" + e.getMessage() + "); using the default config");
            return DEFAULT;
        }

        for (String key : json.keySet()) {
            if (!KEYS.contains(key)) {
                warnings.accept("unknown config key \"" + key + "\"; ignoring it");
            }
        }
        return new CarefulHarvestConfig(
                bool(json, NON_SNEAKING_BREAK, DEFAULT.nonSneakingBreak, warnings),
                bool(json, FLUIDS, DEFAULT.fluids, warnings),
                bool(json, EXPLOSIONS, DEFAULT.explosions, warnings),
                bool(json, SUPPORT_BROKEN, DEFAULT.supportBroken, warnings),
                bool(json, OTHER, DEFAULT.other, warnings),
                soil(json, warnings));
    }

    private static boolean bool(JsonObject json, String key, boolean fallback, Consumer<String> warnings) {
        JsonElement value = json.get(key);
        if (value == null) {
            warnings.accept("config key \"" + key + "\" is missing; using " + fallback);
            return fallback;
        }
        if (value instanceof JsonPrimitive primitive && primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        warnings.accept("config key \"" + key + "\" must be true or false, not " + value + "; using " + fallback);
        return fallback;
    }

    private static ExemptSoil soil(JsonObject json, Consumer<String> warnings) {
        JsonElement value = json.get(EXEMPT_SOIL);
        if (value == null) {
            warnings.accept("config key \"" + EXEMPT_SOIL + "\" is missing; using the default list");
            return DEFAULT.exemptSoil;
        }
        if (!value.isJsonArray()) {
            warnings.accept("config key \"" + EXEMPT_SOIL + "\" must be a list of strings, not " + value + "; using the default list");
            return DEFAULT.exemptSoil;
        }
        List<String> entries = new ArrayList<>();
        for (JsonElement element : value.getAsJsonArray()) {
            if (element instanceof JsonPrimitive primitive && primitive.isString()) {
                entries.add(primitive.getAsString());
            } else {
                warnings.accept("exemptSoil: " + element + " is not a string; skipping it");
            }
        }
        return ExemptSoil.parse(entries, warnings);
    }

    /** For messages: the five toggles. */
    public Map<String, Boolean> toggles() {
        Map<String, Boolean> toggles = new LinkedHashMap<>();
        toggles.put(NON_SNEAKING_BREAK, this.nonSneakingBreak);
        toggles.put(FLUIDS, this.fluids);
        toggles.put(EXPLOSIONS, this.explosions);
        toggles.put(SUPPORT_BROKEN, this.supportBroken);
        toggles.put(OTHER, this.other);
        return toggles;
    }
}
