package dev.carefulharvest;

/**
 * Why a tagged block is being turned into drops. See {@link DestructionContext} for how the
 * cause is worked out.
 */
public enum DropCause {
    /** A player who is sneaking breaks the block (or the other half of a double-tall plant). Always drops. */
    SNEAKING_BREAK,
    /** A player who is not sneaking breaks the block. Config key {@code nonSneakingBreak}. */
    NON_SNEAKING_BREAK,
    /** Flowing water or lava replaces the block. Config key {@code fluids}. */
    FLUID,
    /** An explosion destroys the block. Config key {@code explosions}. */
    EXPLOSION,
    /** The block pops off because the block it stands on was removed. Config key {@code supportBroken}. */
    SUPPORT_BROKEN,
    /** Anything else: pistons, enderman deaths, other mods' code, ... Config key {@code other}. */
    OTHER
}
