package dev.carefulharvest;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Remembers, for the current server tick only, exempt soil blocks that were replaced by something
 * else. When a player breaks the moss under a flower, the flower pops off (and its loot is
 * evaluated) after the moss is already gone, so the soil rule needs to know what used to be
 * there. Vanilla pops unsupported plants in the same tick, usually inside the very call that
 * removed the soil; neighbour updates queued behind a running update chain still run in that tick.
 *
 * <ul>
 *   <li>Entries are keyed by level (identity) and position, so the same position in another
 *       dimension is unrelated.</li>
 *   <li>All entries are dropped at the end of every server tick ({@link #clear()}, called from
 *       {@code MinecraftServer#tickServer}), so the map only ever holds the exempt soil removed in
 *       one tick, even while the game is frozen with {@code /tick freeze}.</li>
 *   <li>An entry is forgotten as soon as anything else is placed at its position, or a tagged plant
 *       is placed directly above it, so a plant placed later in the same tick on whatever replaced
 *       the soil is judged by its real soil.</li>
 *   <li>Only blocks that match the exempt soil list are recorded, and when nothing is recorded the
 *       check is a single volatile read.</li>
 * </ul>
 */
public final class SoilMemory {
    private record Key(Level level, BlockPos pos) {
    }

    private static final Map<Key, BlockState> REMOVED = new HashMap<>();
    /** {@code true} while {@link #REMOVED} may be non-empty; lets the hot path skip locking. */
    private static volatile boolean active;

    private SoilMemory() {
    }

    /** Called when the exempt soil {@code old} at {@code pos} is replaced by a different block. */
    public static void recordRemoval(Level level, BlockPos pos, BlockState old) {
        synchronized (REMOVED) {
            REMOVED.put(new Key(level, pos.immutable()), old);
            active = true;
        }
    }

    /** Whether anything is remembered at all (cheap). */
    public static boolean isActive() {
        return active;
    }

    /** Forgets the removed soil at {@code pos}, if any. */
    public static void forget(Level level, BlockPos pos) {
        if (!active) {
            return;
        }
        synchronized (REMOVED) {
            REMOVED.remove(new Key(level, pos));
        }
    }

    /** The exempt soil that was at {@code pos} earlier in this tick, if any. */
    public static @Nullable BlockState removedThisTick(Level level, BlockPos pos) {
        if (!active) {
            return null;
        }
        synchronized (REMOVED) {
            return REMOVED.get(new Key(level, pos));
        }
    }

    /** Forgets everything; called at the end of every server tick. */
    public static void clear() {
        if (!active) {
            return;
        }
        synchronized (REMOVED) {
            REMOVED.clear();
            active = false;
        }
    }

    /** Number of remembered positions (tests). */
    public static int size() {
        synchronized (REMOVED) {
            return REMOVED.size();
        }
    }
}
