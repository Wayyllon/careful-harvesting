package dev.carefulharvest.test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Fabric registration of the shared {@link CarefulHarvestTestCases}: one annotated
 * wrapper per case, plus a check that every shared case is wrapped here with the same timeout
 * and environment (config). Run with {@code ./gradlew :fabric:runGameTest} (also part of
 * {@code ./gradlew build}). The environments are defined in
 * {@code data/carefulharvest_gametest/test_environment/}.
 *
 * <p>Generated from the case list (one wrapper per case, all delegating to
 * {@link CarefulHarvestTestCases#run}); {@code everySharedCaseRegistered} fails if the two drift apart.
 */
public class CarefulHarvestGameTests {
    private static final String CHECK = "everySharedCaseRegistered";

    @GameTest(environment = "carefulharvest_gametest:defaults")
    public void everySharedCaseRegistered(GameTestHelper helper) {
        Map<String, String> wrapped = new TreeMap<>();
        for (Method method : CarefulHarvestGameTests.class.getDeclaredMethods()) {
            GameTest test = method.getAnnotation(GameTest.class);
            if (test != null && !method.getName().equals(CHECK)) {
                wrapped.put(method.getName(), test.maxTicks() + " " + test.environment());
            }
        }
        Map<String, String> shared = CarefulHarvestTestCases.ALL.stream().collect(Collectors.toMap(
                CarefulHarvestTestCases.Case::name,
                c -> c.maxTicks() + " carefulharvest_gametest:" + c.environment(),
                (a, b) -> {
                    throw new IllegalStateException("duplicate case name");
                },
                TreeMap::new));
        helper.assertTrue(wrapped.equals(shared), "Fabric wrappers " + wrapped + " != shared cases " + shared);
        helper.succeed();
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void tagContainsDefaults(GameTestHelper helper) {
        CarefulHarvestTestCases.run("tagContainsDefaults", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void nonSneakingBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void creativeBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("creativeBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingBareHandedGrassDropsNoGrass(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingBareHandedGrassDropsNoGrass", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingShearsGrassDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingShearsGrassDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void unsneakingShearsDropNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("unsneakingShearsDropNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingShearsTallGrassUpperHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingShearsTallGrassUpperHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingShearsTallGrassLowerHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingShearsTallGrassLowerHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingBreakUpperHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingBreakUpperHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingBreakLowerHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingBreakLowerHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void nonSneakingBreakUpperHalfDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakUpperHalfDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void nonSneakingBreakLowerHalfDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakLowerHalfDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void stackedDoublePlantOnlyBrokenOneDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("stackedDoublePlantOnlyBrokenOneDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void explosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void explosionCausedBySneakingPlayerDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionCausedBySneakingPlayerDropsNothing", helper);
    }

    @GameTest(maxTicks = 100, environment = "carefulharvest_gametest:defaults")
    public void tntIgnitedBySneakingPlayerDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("tntIgnitedBySneakingPlayerDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void waterFlowDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("waterFlowDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void pistonDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("pistonDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void pistonControlDropsUntagged(GameTestHelper helper) {
        CarefulHarvestTestCases.run("pistonControlDropsUntagged", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void lostSupportDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("lostSupportDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void lostSupportDoublePlantDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("lostSupportDoublePlantDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void sneakingPlayerAsBreakerOutsideBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("sneakingPlayerAsBreakerOutsideBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void endermanCarriedBlockDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("endermanCarriedBlockDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void untaggedBlockUnaffected(GameTestHelper helper) {
        CarefulHarvestTestCases.run("untaggedBlockUnaffected", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnPoppyDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnPoppyDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnUpperHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnUpperHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnLowerHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnLowerHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnBareHandedGrassDropsNoGrass(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnBareHandedGrassDropsNoGrass", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnShearsGrassDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnShearsGrassDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnShearsTallGrassUpperHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnShearsTallGrassUpperHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnWaterDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnWaterDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnExplosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnExplosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnSoilRemovedDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnSoilRemovedDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnPistonDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnPistonDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnWaterDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnWaterDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnWaterDoublePlantDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnWaterDoublePlantDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnLavaDropsNothingLikeVanilla(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnLavaDropsNothingLikeVanilla", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnNonSneakingBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnNonSneakingBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnExplosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnExplosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnSoilRemovedDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnSoilRemovedDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnPistonDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnPistonDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnPoppiesDrop(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnPoppiesDrop", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnDoublePlantDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnDoublePlantDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnNonSneakingBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnNonSneakingBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnWaterDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnWaterDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnSoilRemovedDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnSoilRemovedDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnPistonDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnPistonDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnSneakingSoilBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnSneakingSoilBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnNonSneakingSoilBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnNonSneakingSoilBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnDoublePlantDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnDoublePlantDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnNonSneakingBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnNonSneakingBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnWaterDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnWaterDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnExplosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnExplosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnPistonDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnPistonDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnPistonDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnPistonDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnEndermanDeathDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnEndermanDeathDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnDestroyBlockOutsideBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnDestroyBlockOutsideBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnNonSneakingBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnNonSneakingBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnWaterDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnWaterDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnExplosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnExplosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnSoilRemovedDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnSoilRemovedDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossNonSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossNonSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossExplosionDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossExplosionDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossWaterDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossWaterDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossSneakingSoilRemovedDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossSneakingSoilRemovedDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossNonSneakingSoilRemovedDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossNonSneakingSoilRemovedDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossPistonDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossPistonDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptPaleMossNonSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptPaleMossNonSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptNyliumNonSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptNyliumNonSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptNyliumExplosionDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptNyliumExplosionDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptNyliumWaterDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptNyliumWaterDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptNyliumSoilRemovedDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptNyliumSoilRemovedDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptWarpedNyliumNonSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptWarpedNyliumNonSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossDoublePlantUpperHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossDoublePlantUpperHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossDoublePlantLowerHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossDoublePlantLowerHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossDoublePlantSoilRemovedDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossDoublePlantSoilRemovedDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossDoublePlantExplosionDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossDoublePlantExplosionDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossDoublePlantWaterDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossDoublePlantWaterDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossTallGrassShearsUpperHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossTallGrassShearsUpperHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossTallGrassShearsLowerHalfDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossTallGrassShearsLowerHalfDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void exemptMossShearsStillNeeded(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptMossShearsStillNeeded", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void nonExemptSoilNextToMossDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonExemptSoilNextToMossDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void nonExemptDirtExplosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonExemptDirtExplosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void mossRemovedElsewhereDoesNotExemptOthers(GameTestHelper helper) {
        CarefulHarvestTestCases.run("mossRemovedElsewhereDoesNotExemptOthers", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:no_exempt_soil")
    public void noExemptSoilMossNonSneakingBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("noExemptSoilMossNonSneakingBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:no_exempt_soil")
    public void noExemptSoilMossSoilRemovedDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("noExemptSoilMossSoilRemovedDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:no_exempt_soil")
    public void noExemptSoilNyliumNonSneakingBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("noExemptSoilNyliumNonSneakingBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:exempt_dirt_tag")
    public void exemptDirtTagNonSneakingBreakDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptDirtTagNonSneakingBreakDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:exempt_dirt_tag")
    public void exemptDirtTagSoilRemovedDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptDirtTagSoilRemovedDrops", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:exempt_dirt_tag")
    public void exemptDirtTagMossDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("exemptDirtTagMossDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnSneakingSoilBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnSneakingSoilBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:fluids")
    public void fluidsOnSneakingSoilBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("fluidsOnSneakingSoilBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnSneakingSoilBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnSneakingSoilBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnSneakingSoilBreakDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnSneakingSoilBreakDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:non_sneaking_break")
    public void nonSneakingBreakOnSneakingSoilBreakDoublePlantDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nonSneakingBreakOnSneakingSoilBreakDoublePlantDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:other")
    public void otherOnSneakingSoilBreakDoublePlantDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("otherOnSneakingSoilBreakDoublePlantDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnNonSneakingSoilBreakDoublePlantDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnNonSneakingSoilBreakDoublePlantDropsOnce", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void nestedCausesClassifiedDeterministically(GameTestHelper helper) {
        CarefulHarvestTestCases.run("nestedCausesClassifiedDeterministically", helper);
    }

    @GameTest(maxTicks = 200, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnPoppyGridEveryPoppyDrops(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnPoppyGridEveryPoppyDrops", helper);
    }

    @GameTest(maxTicks = 200, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnPoppyGridExplosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnPoppyGridExplosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 200, environment = "carefulharvest_gametest:explosions")
    public void explosionsOnSunflowerGridEachDropsOnce(GameTestHelper helper) {
        CarefulHarvestTestCases.run("explosionsOnSunflowerGridEachDropsOnce", helper);
    }

    @GameTest(maxTicks = 200, environment = "carefulharvest_gametest:support_broken")
    public void supportBrokenOnSunflowerGridExplosionDropsNothing(GameTestHelper helper) {
        CarefulHarvestTestCases.run("supportBrokenOnSunflowerGridExplosionDropsNothing", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void markerStackEmptyAfterEveryPath(GameTestHelper helper) {
        CarefulHarvestTestCases.run("markerStackEmptyAfterEveryPath", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void soilMemoryForgottenWhenSoilReplaced(GameTestHelper helper) {
        CarefulHarvestTestCases.run("soilMemoryForgottenWhenSoilReplaced", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void soilMemoryForgottenWhenPlantPlacedAbove(GameTestHelper helper) {
        CarefulHarvestTestCases.run("soilMemoryForgottenWhenPlantPlacedAbove", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void soilMemoryClearedAfterTick(GameTestHelper helper) {
        CarefulHarvestTestCases.run("soilMemoryClearedAfterTick", helper);
    }

    @GameTest(maxTicks = 100, environment = "carefulharvest_gametest:defaults")
    public void soilMemoryPerDimension(GameTestHelper helper) {
        CarefulHarvestTestCases.run("soilMemoryPerDimension", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:no_exempt_soil")
    public void soilMemoryOnlyRecordsExemptSoil(GameTestHelper helper) {
        CarefulHarvestTestCases.run("soilMemoryOnlyRecordsExemptSoil", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configDefaults(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configDefaults", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configMissingKeysUseDefaults(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configMissingKeysUseDefaults", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configUnknownKeysIgnored(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configUnknownKeysIgnored", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configMalformedUsesDefaults(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configMalformedUsesDefaults", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configWrongTypesUseDefaults(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configWrongTypesUseDefaults", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configInvalidIdsSkipped(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configInvalidIdsSkipped", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configTagEntries(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configTagEntries", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configScreenEntrySplitting(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configScreenEntrySplitting", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configFileCreatedWithDefaults(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configFileCreatedWithDefaults", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configMalformedFileNotOverwritten(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configMalformedFileNotOverwritten", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:defaults")
    public void configSaveAndLoadRoundTrip(GameTestHelper helper) {
        CarefulHarvestTestCases.run("configSaveAndLoadRoundTrip", helper);
    }

    @GameTest(maxTicks = 40, environment = "carefulharvest_gametest:reload_command")
    public void reloadCommandReadsFile(GameTestHelper helper) {
        CarefulHarvestTestCases.run("reloadCommandReadsFile", helper);
    }
}
