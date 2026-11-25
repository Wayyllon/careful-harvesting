package dev.carefulharvest.test.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.carefulharvest.CarefulHarvest;
import dev.carefulharvest.client.CarefulHarvestConfigScreen;
import dev.carefulharvest.config.CarefulHarvestConfig;
import dev.carefulharvest.config.ConfigManager;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/**
 * Client smoke test for the config screen (not part of {@code ./gradlew build}; needs a display,
 * e.g. {@code xvfb-run ./gradlew :fabric:runClientGameTest}). Opens the screen through the Mod
 * Menu entrypoint, as Mod Menu does, turns on "Fluids", adds an exempt soil entry by typing,
 * presses Done, and checks the live config and the file.
 */
public final class CarefulHarvestClientSmokeTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        ModMenuApi entrypoint = FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).stream()
                .filter(c -> c.getProvider().getMetadata().getId().equals(CarefulHarvest.MOD_ID))
                .findFirst().orElseThrow(() -> new AssertionError("no modmenu entrypoint for " + CarefulHarvest.MOD_ID))
                .getEntrypoint();
        check(ConfigManager.get().equals(CarefulHarvestConfig.DEFAULT), "starts with defaults: " + ConfigManager.get());

        context.setScreen(() -> entrypoint.getModConfigScreenFactory().create(null));
        context.waitForScreen(CarefulHarvestConfigScreen.class);
        context.takeScreenshot("carefulharvest_config_screen");

        context.runOnClient(mc -> {
            Screen screen = mc.gui.screen();
            CycleButton<?> fluids = find(screen, CycleButton.class, "Fluids");
            click(screen, fluids);
            check(Boolean.TRUE.equals(fluids.getValue()), "fluids switched on by the click");
            MultiLineEditBox soil = find(screen, MultiLineEditBox.class, null);
            click(screen, soil);
            check(screen.getFocused() == soil, "exempt soil box focused: " + screen.getFocused());
        });
        context.getInput().holdControl();
        context.getInput().pressKey(InputConstants.KEY_END);
        context.getInput().releaseControl();
        context.getInput().pressKey(InputConstants.KEY_RETURN);
        context.getInput().typeChars("#minecraft:dirt");
        context.waitTick();
        context.takeScreenshot("carefulharvest_config_screen_edited");
        // Resizing rebuilds the widgets; the edits must survive it.
        context.runOnClient(mc -> {
            Screen screen = mc.gui.screen();
            screen.resize(screen.width, screen.height);
            CycleButton<?> resized = find(screen, CycleButton.class, "Fluids");
            check(Boolean.TRUE.equals(resized.getValue()), "fluids still on after resize");
            MultiLineEditBox box = find(screen, MultiLineEditBox.class, null);
            String text = box.getValue();
            check(text.contains("#minecraft:dirt"), "typed entry kept after resize: " + text);
        });
        context.clickScreenButton("gui.done");
        context.waitFor(mc -> !(mc.gui.screen() instanceof CarefulHarvestConfigScreen));

        CarefulHarvestConfig saved = ConfigManager.get();
        check(saved.fluids(), "fluids turned on: " + saved);
        check(!saved.explosions() && !saved.nonSneakingBreak() && !saved.supportBroken() && !saved.other(), "others unchanged: " + saved);
        check(saved.exemptSoil().entries().contains("#minecraft:dirt"), "entry added: " + saved.exemptSoil());
        check(saved.exemptSoil().entries().contains("minecraft:moss_block"), "defaults kept: " + saved.exemptSoil());
        Path file = ConfigManager.path();
        try {
            check(file != null && CarefulHarvestConfig.parse(Files.readString(file), w -> {
                throw new AssertionError(w);
            }).equals(saved), "file matches the live config");
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }

        // Reopen: shows the saved values; Cancel leaves the config alone.
        context.setScreen(() -> new CarefulHarvestConfigScreen(null));
        context.waitForScreen(CarefulHarvestConfigScreen.class);
        context.runOnClient(mc -> {
            Screen screen = mc.gui.screen();
            CycleButton<?> fluids = find(screen, CycleButton.class, "Fluids");
            check(Boolean.TRUE.equals(fluids.getValue()), "fluids shown as on");
            click(screen, find(screen, CycleButton.class, "Explosions"));
        });
        context.clickScreenButton("gui.cancel");
        context.waitFor(mc -> !(mc.gui.screen() instanceof CarefulHarvestConfigScreen));
        check(!ConfigManager.get().explosions(), "cancel does not save");
        try {
            ConfigManager.save(CarefulHarvestConfig.DEFAULT);
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static <T extends AbstractWidget> T find(Screen screen, Class<?> type, String label) {
        for (GuiEventListener child : screen.children()) {
            if (type.isInstance(child) && (label == null || ((AbstractWidget) child).getMessage().getString().startsWith(label))) {
                @SuppressWarnings("unchecked")
                T widget = (T) child;
                return widget;
            }
        }
        throw new AssertionError("no " + type.getSimpleName() + " " + label + " on " + screen);
    }

    private static void click(Screen screen, AbstractWidget widget) {
        // 26.x numbers mouse buttons from 1 (SDL); widgets ignore any other button.
        MouseButtonEvent event = new MouseButtonEvent(widget.getX() + widget.getWidth() / 2.0, widget.getY() + widget.getHeight() / 2.0,
                new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
        check(screen.getChildAt(event.x(), event.y()).orElse(null) == widget, "click lands on " + widget.getMessage().getString());
        check(screen.mouseClicked(event, false), "click handled by " + widget.getMessage().getString());
        screen.mouseReleased(event);
    }
}
