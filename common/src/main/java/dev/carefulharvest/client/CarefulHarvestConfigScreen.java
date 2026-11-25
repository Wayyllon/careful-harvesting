package dev.carefulharvest.client;

import dev.carefulharvest.config.CarefulHarvestConfig;
import dev.carefulharvest.config.ConfigManager;
import dev.carefulharvest.config.ExemptSoil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Config screen built only from vanilla widgets, opened from Mod Menu (Fabric) or the NeoForge
 * mods list. Client-only: referenced only from the loaders' client entrypoints.
 *
 * <p>"Done" writes {@code config/carefulharvest.json} and applies it immediately. In
 * singleplayer that is the config the integrated server uses; on a multiplayer server the
 * server's own file applies (edit it there and run {@code /carefulharvest reload}).
 */
public class CarefulHarvestConfigScreen extends Screen {
    private static final int TOGGLE_WIDTH = 150;
    private static final int SOIL_WIDTH = 310;

    private final @Nullable Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private CarefulHarvestConfig config;
    private String soilText;
    private @Nullable StringWidget status;
    private @Nullable Component saveError;

    public CarefulHarvestConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("carefulharvest.config.title"));
        this.parent = parent;
        this.config = ConfigManager.get();
        this.soilText = String.join("\n", this.config.exemptSoil().entries());
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);
        LinearLayout content = this.layout.addToContents(LinearLayout.vertical().spacing(4));
        content.defaultCellSetting().alignHorizontallyCenter();

        GridLayout grid = new GridLayout().columnSpacing(10).rowSpacing(4);
        GridLayout.RowHelper rows = grid.createRowHelper(2);
        rows.addChild(this.toggle(CarefulHarvestConfig.NON_SNEAKING_BREAK, this.config.nonSneakingBreak(),
                value -> this.config = this.config.withNonSneakingBreak(value)));
        rows.addChild(this.toggle(CarefulHarvestConfig.FLUIDS, this.config.fluids(), value -> this.config = this.config.withFluids(value)));
        rows.addChild(this.toggle(CarefulHarvestConfig.EXPLOSIONS, this.config.explosions(),
                value -> this.config = this.config.withExplosions(value)));
        rows.addChild(this.toggle(CarefulHarvestConfig.SUPPORT_BROKEN, this.config.supportBroken(),
                value -> this.config = this.config.withSupportBroken(value)));
        rows.addChild(this.toggle(CarefulHarvestConfig.OTHER, this.config.other(), value -> this.config = this.config.withOther(value)));
        content.addChild(grid);

        content.addChild(new StringWidget(Component.translatable("carefulharvest.config.exemptSoil"), this.font).setMaxWidth(SOIL_WIDTH));
        // Header, footer, toggles and the labels take about 180 pixels; the list gets the rest.
        int soilHeight = Math.max(36, Math.min(120, this.height - 190));
        MultiLineEditBox soil = MultiLineEditBox.builder()
                .setPlaceholder(Component.translatable("carefulharvest.config.exemptSoil.placeholder"))
                .build(this.font, SOIL_WIDTH, soilHeight, Component.translatable("carefulharvest.config.exemptSoil"));
        soil.setValue(this.soilText, true);
        soil.setValueListener(value -> {
            this.soilText = value;
            this.saveError = null;
            this.updateStatus();
        });
        soil.setTooltip(Tooltip.create(Component.translatable("carefulharvest.config.exemptSoil.tooltip")));
        content.addChild(soil);

        this.status = content.addChild(new StringWidget(SOIL_WIDTH, 9, Component.empty(), this.font));
        this.updateStatus();

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.saveAndClose()).width(150).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).width(150).build());

        this.layout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    private CycleButton<Boolean> toggle(String key, boolean value, java.util.function.Consumer<Boolean> onChange) {
        return CycleButton.onOffBuilder(value)
                .withTooltip(v -> Tooltip.create(Component.translatable("carefulharvest.config." + key + ".tooltip")))
                .create(0, 0, TOGGLE_WIDTH, 20, Component.translatable("carefulharvest.config." + key),
                        (button, newValue) -> onChange.accept(newValue));
    }

    private ExemptSoil parseSoil(List<String> warnings) {
        ExemptSoil soil = ExemptSoil.parse(ExemptSoil.splitEntries(this.soilText), warnings::add);
        soil.resolveBlocks(warnings::add);
        return soil;
    }

    private void updateStatus() {
        if (this.status == null) {
            return;
        }
        Component message;
        if (this.saveError != null) {
            message = this.saveError;
        } else {
            List<String> warnings = new ArrayList<>();
            this.parseSoil(warnings);
            message = warnings.isEmpty()
                    ? Component.translatable(this.minecraft != null && this.minecraft.getSingleplayerServer() == null && this.minecraft.level != null
                            ? "carefulharvest.config.status.remote" : "carefulharvest.config.status.ok")
                    : Component.translatable("carefulharvest.config.status.invalid", warnings.size(), warnings.getFirst());
        }
        this.status.setMessage(message);
        this.status.setTooltip(Tooltip.create(message));
    }

    private void saveAndClose() {
        CarefulHarvestConfig updated = this.config.withExemptSoil(this.parseSoil(new ArrayList<>()));
        try {
            ConfigManager.save(updated);
        } catch (IOException e) {
            this.saveError = Component.translatable("carefulharvest.config.status.saveFailed", e.getMessage());
            this.updateStatus();
            return;
        }
        this.onClose();
    }

    @Override
    protected void repositionElements() {
        this.clearWidgets();
        this.layout.removeChildren();
        this.init();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
