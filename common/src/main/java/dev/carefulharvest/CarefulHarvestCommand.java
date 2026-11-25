package dev.carefulharvest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.carefulharvest.config.CarefulHarvestConfig;
import dev.carefulharvest.config.ConfigManager;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * {@code /carefulharvest} shows the active config; {@code /carefulharvest reload} re-reads
 * {@code config/carefulharvest.json}. Both need the game master permission (level 2, the
 * same as {@code /reload}).
 */
public final class CarefulHarvestCommand {
    private CarefulHarvestCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(CarefulHarvest.MOD_ID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(CarefulHarvestCommand::show)
                .then(Commands.literal("reload").executes(CarefulHarvestCommand::reload)));
    }

    private static int show(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(describe(ConfigManager.get())), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        ConfigManager.LoadResult result = ConfigManager.reload(true);
        CommandSourceStack source = context.getSource();
        for (String warning : result.warnings()) {
            source.sendFailure(Component.literal("Careful Harvest config: " + warning));
        }
        source.sendSuccess(() -> Component.literal("Reloaded " + ConfigManager.path() + (result.warnings().isEmpty() ? "" :
                " with " + result.warnings().size() + " warning(s)") + ". " + describe(result.config())), true);
        return 1;
    }

    public static String describe(CarefulHarvestConfig config) {
        StringBuilder text = new StringBuilder("Careful Harvest drops: sneaking break=true");
        for (Map.Entry<String, Boolean> toggle : config.toggles().entrySet()) {
            text.append(", ").append(toggle.getKey()).append('=').append(toggle.getValue());
        }
        return text.append("; exemptSoil=").append(config.exemptSoil()).toString();
    }
}
