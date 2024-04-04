package net.jasper.mod.automation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class Commands {
    public static void register() {
        // Register /record <store|load|clear>
        //          /record quickslot <load|store> <slot>
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("record")
                .then(literal("start")
                    .executes(context -> {
                        PlayerRecorder.startRecord();
                        return 1;
                    })
                )
                .then(literal("stop")
                    .executes(context -> {
                        PlayerRecorder.stopRecord();
                        return 1;
                    })
                )
                .then(literal("clear")
                    .executes(context -> {
                        PlayerRecorder.clearRecord();
                        return 1;
                    })
                )
                .then(literal("quickslot")
                    .then(literal("load")
                        .then(argument("slot", IntegerArgumentType.integer())
                            .executes(context -> handleQuickSlotCommand(context, true))
                        )
                    )
                    .then(literal("store")
                        .then(argument("slot", IntegerArgumentType.integer())
                            .executes(context -> handleQuickSlotCommand(context, false))
                        )
                    )
                )
            )
        );

        // Register /replay <start|stop|loop|togglepause>
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("replay")
                    .then(literal("start")
                        .executes(context -> {
                            PlayerRecorder.startReplay(false);
                            return 1;
                        })
                    )
                    .then(literal("stop")
                        .executes(context -> {
                            PlayerRecorder.stopReplay();
                            return 1;
                        })
                    )
                    .then(literal("togglepause")
                        .executes(context -> {
                            PlayerRecorder.togglePauseReplay();
                            return 1;
                        })
                    ).then(literal("loop")
                        .executes(context -> {
                            PlayerRecorder.startReplay(true);
                            return 1;
                        })
                    )
                )
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static int handleQuickSlotCommand(CommandContext<ServerCommandSource> context, boolean isLoad) {
        final int slot = IntegerArgumentType.getInteger(context, "slot");
        if (slot < 0 || 9 < slot) {
            context.getSource().sendFeedback(() -> Text.literal("Slot Index out of range"), false);
            return 0;
        }
        if (isLoad) {
            QuickSlots.load(slot);
        } else {
            QuickSlots.store(slot, PlayerRecorder.record);
        }
        return 1;
    }
}
