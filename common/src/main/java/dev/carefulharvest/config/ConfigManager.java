package dev.carefulharvest.config;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Holds the active {@link CarefulHarvestConfig} and reads and writes
 * {@code <config dir>/carefulharvest.json}.
 *
 * <p>The config is server-side logic. In singleplayer the client and the integrated server
 * share this class, so the config screen edits the live config. A dedicated server reads the
 * file at startup and on {@code /carefulharvest reload}.
 */
public final class ConfigManager {
    public static final String FILE_NAME = "carefulharvest.json";
    static final Logger LOGGER = LogUtils.getLogger();

    private static volatile CarefulHarvestConfig active = CarefulHarvestConfig.DEFAULT;
    private static volatile @Nullable Path path;

    private ConfigManager() {
    }

    /** The result of reading the file. */
    public record LoadResult(CarefulHarvestConfig config, List<String> warnings, boolean createdFile) {
    }

    /** The active config. Cheap; called for every tagged block drop. */
    public static CarefulHarvestConfig get() {
        return active;
    }

    /** Makes {@code config} active without writing the file. */
    public static void set(CarefulHarvestConfig config) {
        active = config;
    }

    public static @Nullable Path path() {
        return path;
    }

    /** Called once by each loader's entrypoint with that loader's config directory. */
    public static void init(Path configDir) {
        path = configDir.resolve(FILE_NAME);
        // Other mods' blocks may not be registered yet: look up the exempt block ids on first use.
        reload(false);
    }

    /**
     * Re-reads the file (creating it with defaults if it does not exist) and makes it active.
     *
     * @param resolveBlocks look up the exempt block ids now (only once all blocks are registered),
     *                      so unknown ones are reported in the result
     */
    public static LoadResult reload(boolean resolveBlocks) {
        Path file = path;
        if (file == null) {
            LoadResult result = new LoadResult(CarefulHarvestConfig.DEFAULT, List.of("the config file location is not known yet"), false);
            set(result.config());
            return result;
        }
        LoadResult result = load(file, resolveBlocks);
        result.warnings().forEach(warning -> LOGGER.warn("[carefulharvest] {}: {}", file, warning));
        set(result.config());
        return result;
    }

    /**
     * Reads a config file. Never throws. A missing file is created with the defaults. A file
     * that cannot be read or parsed gives the defaults and is left untouched, so it can be fixed.
     */
    public static LoadResult load(Path file, boolean resolveBlocks) {
        List<String> warnings = new ArrayList<>();
        if (!Files.exists(file)) {
            boolean created = false;
            try {
                write(file, CarefulHarvestConfig.DEFAULT);
                created = true;
            } catch (IOException e) {
                warnings.add("could not create the default config file (" + e + ")");
            }
            return new LoadResult(CarefulHarvestConfig.DEFAULT, warnings, created);
        }
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            warnings.add("could not read the config file (" + e + "); using the default config");
            return new LoadResult(CarefulHarvestConfig.DEFAULT, warnings, false);
        }
        CarefulHarvestConfig config = CarefulHarvestConfig.parse(text, warnings::add);
        if (resolveBlocks) {
            // Report unknown block ids now rather than on first use.
            config.exemptSoil().resolveBlocks(warnings::add);
        }
        return new LoadResult(config, warnings, false);
    }

    /** Writes {@code config} to the file and makes it active. */
    public static void save(CarefulHarvestConfig config) throws IOException {
        Path file = path;
        if (file == null) {
            throw new IOException("the config file location is not known");
        }
        write(file, config);
        set(config);
    }

    public static void write(Path file, CarefulHarvestConfig config) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, config.toJson(), StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // ------------------------------------------------------------------ tests

    /** Active config and file location, so tests can restore them. */
    public record State(CarefulHarvestConfig config, @Nullable Path path) {
    }

    public static State snapshot() {
        return new State(active, path);
    }

    public static void restore(State state) {
        path = state.path();
        active = state.config();
    }

    /** Points the manager at another file without reading it (tests). */
    public static void setPath(@Nullable Path file) {
        path = file;
    }
}
