package dev.carefulharvest;

import dev.carefulharvest.config.CarefulHarvestConfig;
import dev.carefulharvest.config.ConfigManager;
import dev.carefulharvest.config.ExemptSoil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * Loader-independent logic for Careful Harvest.
 *
 * <p>Every vanilla path that turns a block into item drops (player breaking, explosions,
 * fluids, pistons, lost support, ...) evaluates the block's loot table through
 * {@code BlockBehaviour.BlockStateBase#getDrops(LootParams.Builder)}, where
 * {@link dev.carefulharvest.mixin.BlockStateBaseMixin} calls {@link #shouldSuppressDrops}.
 * For blocks in {@link #CAREFUL_HARVEST} the loot is replaced with nothing unless the plant
 * stands on exempt soil, or the config allows drops for the {@link DropCause} that
 * {@link DestructionContext#classify} works out.
 */
public final class CarefulHarvest {
    public static final String MOD_ID = "carefulharvest";

    /** Block tag {@code carefulharvest:careful_harvest}. */
    public static final TagKey<Block> CAREFUL_HARVEST =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "careful_harvest"));

    private CarefulHarvest() {
    }

    /**
     * @return {@code true} if the loot for {@code state} must be replaced with nothing.
     */
    public static boolean shouldSuppressDrops(BlockState state, LootParams.Builder builder) {
        if (!state.is(CAREFUL_HARVEST)) {
            return false;
        }
        CarefulHarvestConfig config = ConfigManager.get();
        Vec3 origin = builder.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) {
            return !config.dropsFor(DropCause.OTHER);
        }
        BlockPos pos = BlockPos.containing(origin);
        // Block loot is always evaluated at the block's centre. Loot evaluated elsewhere (such as
        // the block an enderman carries, at the enderman's feet) is not a plant in the world.
        if (origin.equals(Vec3.atCenterOf(pos)) && isOnExemptSoil(builder.getLevel(), pos, state, config.exemptSoil())) {
            return false;
        }
        Entity breaker = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        return !config.dropsFor(DestructionContext.classify(state, pos, breaker));
    }

    /**
     * The soil of a plant is the block directly below it; for the upper half of a double-tall
     * plant it is the block below the lower half. If that block was exempt soil earlier in this
     * tick (it is what was just removed, which is why the plant pops off) it still counts.
     */
    public static boolean isOnExemptSoil(Level level, BlockPos pos, BlockState state, ExemptSoil exemptSoil) {
        if (exemptSoil.isEmpty()) {
            return false;
        }
        BlockPos soilPos = DestructionContext.isUpperHalf(state) ? pos.below(2) : pos.below();
        if (exemptSoil.matches(level.getBlockState(soilPos))) {
            return true;
        }
        BlockState removed = SoilMemory.removedThisTick(level, soilPos);
        return removed != null && exemptSoil.matches(removed);
    }

    /**
     * Called by the chunk mixin for every block change in a loaded server chunk, on a hot path:
     * with an empty exempt soil list and nothing remembered it is two volatile reads, otherwise a
     * set lookup (plus a tag check per {@code #tag} entry).
     *
     * @param oldState the block that was there; never the same {@link Block} as {@code newState}
     */
    public static void onBlockReplaced(Level level, BlockPos pos, BlockState oldState, BlockState newState) {
        ExemptSoil exemptSoil = ConfigManager.get().exemptSoil();
        if (!exemptSoil.isEmpty() && exemptSoil.matches(oldState)) {
            SoilMemory.recordRemoval(level, pos, oldState);
        } else if (SoilMemory.isActive()) {
            // Something else now stands where exempt soil may have been removed earlier in the tick,
            // or a plant is being placed on that spot: from now on the real soil counts.
            SoilMemory.forget(level, pos);
            if (newState.is(CAREFUL_HARVEST)) {
                SoilMemory.forget(level, pos.below());
            }
        }
    }
}
