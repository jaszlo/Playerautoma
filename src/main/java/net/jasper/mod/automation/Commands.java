package net.jasper.mod.automation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.jasper.mod.gui.RecordingStorerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.File;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.jasper.mod.PlayerAutomaClient.PLAYERAUTOMA_RECORDING_PATH;

/**
 * Class to register all commands associated with playerautoma
 */
public class Commands {
    public static void register() {
        // Register /record <start|stop|clear>
        //          /record quickslot <load|store> <slot>
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
            literal("record")
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
                .then(literal("store")
                    .then(argument("name", StringArgumentType.string())
                        .then(literal("json")
                            .executes(context -> handleStoreFileCommand(context, "json"))
                        )
                        .then(literal("rec")
                            .executes(context -> handleStoreFileCommand(context, "rec"))
                        )
                    )
                )
                .then(literal("load")
                    .then(argument("name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String current = "";
                            try {
                                current = StringArgumentType.getString(context, "name");
                            } catch (IllegalArgumentException e) {
                                // Empty argument therefore keep startsWith as ""
                            }

                            File[] fileList = new File(PLAYERAUTOMA_RECORDING_PATH).listFiles();
                            if (fileList == null) {
                                return builder.buildFuture();
                            }

                            for (File file : fileList) {
                                if (file.getName().startsWith(current) && (file.getName().endsWith(".rec") || file.getName().endsWith(".json"))) {
                                    builder.suggest(file.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            PlayerRecorder.loadRecord(name);
                            return 1;
                        })
                    )
                )
            )
        );

        // Register /replay <start|stop|loop|togglepause>
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
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

    private static int handleStoreFileCommand(CommandContext<FabricClientCommandSource> context, String fileType) {
        String fileName = StringArgumentType.getString(context, "name");

        boolean callNext = RecordingStorerScreen.useJSON.getValue() && fileType.equals("rec") || !RecordingStorerScreen.useJSON.getValue() && fileType.equals("json");

        // Initialize button element to allow calling next
        Screen currentScreen =  RecordingStorerScreen.open();
        currentScreen.close();

        // Set file type to selected
        if (callNext) {
            RecordingStorerScreen.useJSON.next();
        }
        PlayerRecorder.storeRecord(fileName + "." + fileType);

        // Restore original filetype
        if (callNext) {
            RecordingStorerScreen.useJSON.next();
        }

        return 1;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static int handleQuickSlotCommand(CommandContext<FabricClientCommandSource> context, boolean isLoad) {
        final int slot = IntegerArgumentType.getInteger(context, "slot");
        if (slot < 0 || 9 < slot) {
            context.getSource().sendFeedback(Text.literal("Slot Index out of range"));
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
