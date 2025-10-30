package net.jasper.mod.automation;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.jasper.mod.gui.PlayerautomaMenuScreen;
import net.jasper.mod.gui.RecordingSelectorScreen;
import net.jasper.mod.gui.RecordingStorerScreen;
import net.jasper.mod.util.PlayerautomaExceptionHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.File;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.jasper.mod.PlayerautomaClient.PLAYERAUTOMA_RECORDING_PATH;

/**
 * Class to register all commands associated with playerautoma
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Commands {

    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String CLEAR = "clear";
    private static final String RECORD = "record";
    private static final String QUICKSLOT = "quickslot";
    private static final String LOAD = "load";
    private static final String STORE = "store";
    private static final String SLOT = "slot";
    private static final String NAME = "name";
    private static final String PLAYERAUTOMA = "playerautoma";
    private static final String OPEN = "open";
    private static final String LOADSCREEN = "loadscreen";
    private static final String STORESCREEN = "storescreen";
    private static final String MENU = "menu";
    private static final String JSON = "json";
    private static final String REC = "rec";
    private static final String TOGGLEPAUSE = "togglepause";
    private static final String REPLAY = "replay";
    private static final String LOOP = "loop";


    public static void register() {
        // Register /record <start|stop|clear>
        //          /record quickslot <load|store> <slot>
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
            literal(RECORD)
                .then(literal(START)
                    .executes(context -> {
                        PlayerautomaExceptionHandler.callSafe(PlayerRecorder::startRecord);
                        return 1;
                    })
                )
                .then(literal(STOP)
                    .executes(context -> {
                        PlayerautomaExceptionHandler.callSafe(PlayerRecorder::stopRecord);
                        return 1;
                    })
                )
                .then(literal(CLEAR)
                    .executes(context -> {
                        PlayerautomaExceptionHandler.callSafe(PlayerRecorder::clearRecord);
                        return 1;
                    })
                )
                .then(literal(QUICKSLOT)
                    .then(literal(LOAD)
                        .then(argument(SLOT, IntegerArgumentType.integer())
                            .executes(context -> handleQuickSlotCommand(context, LOAD))
                        )
                    )
                    .then(literal(STORE)
                        .then(argument(SLOT, IntegerArgumentType.integer())
                            .executes(context -> handleQuickSlotCommand(context, STORE))
                        )
                    )
                    .then(literal(CLEAR)
                        .executes(context -> { QuickSlots.clearQuickSlot(); return 1; })
                    .then(argument(SLOT, IntegerArgumentType.integer())
                        .executes(context -> handleQuickSlotCommand(context, CLEAR)))
                    )
                )
                .then(literal(STORE)
                    .then(argument(NAME, StringArgumentType.string())
                        .then(literal(JSON)
                            .executes(context -> handleStoreFileCommand(context, JSON))
                        )
                        .then(literal(REC)
                            .executes(context -> handleStoreFileCommand(context, REC))
                        )
                    )
                )
                .then(literal(LOAD)
                    .then(argument(NAME, StringArgumentType.string())
                        .suggests((context, builder) -> {
                            String current = "";
                            try {
                                current = StringArgumentType.getString(context, NAME);
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
                            String name = StringArgumentType.getString(context, NAME);
                            PlayerautomaExceptionHandler.callSafe(() -> PlayerRecorder.loadRecord(name));
                            return 1;
                        })
                    )
                )
            )
        );

        // Register /replay <start|stop|loop|togglepause>
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal(REPLAY)
                .then(literal(START)
                    .executes(context -> {
                        PlayerautomaExceptionHandler.callSafe(() -> PlayerRecorder.startReplay(false));
                        return 1;
                    })
                )
                .then(literal(STOP)
                    .executes(context -> {
                        PlayerautomaExceptionHandler.callSafe(PlayerRecorder::stopReplay);
                        return 1;
                    })
                )
                .then(literal(TOGGLEPAUSE)
                    .executes(context -> {
                        PlayerautomaExceptionHandler.callSafe(PlayerRecorder::togglePauseReplay);
                        return 1;
                    })
                ).then(literal(LOOP)
                    .executes(context -> {
                        PlayerautomaExceptionHandler.callSafe(PlayerRecorder::startReplay);
                        return 1;
                    })
                )
            )
        );

        // Register /playerautoma open <loadscreen|storescreen|menu>
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal(PLAYERAUTOMA)
                        .then(literal(OPEN)
                                .then(literal(LOADSCREEN).executes(context -> {
                                    MinecraftClient.getInstance().execute(RecordingSelectorScreen::open);
                                    return 1;
                                })
                                ).then(literal(STORESCREEN).executes(context -> {
                                    MinecraftClient.getInstance().execute(RecordingStorerScreen::open);
                                    return 1;
                                })
                                ).then(literal(MENU).executes(context -> {
                                    MinecraftClient.getInstance().execute(PlayerautomaMenuScreen::open);
                                    return 1;
                                }))
                        )
                )
        );
    }

    private static int handleStoreFileCommand(CommandContext<FabricClientCommandSource> context, String fileType) {
        String fileName = StringArgumentType.getString(context, NAME);

        boolean callNext = RecordingStorerScreen.useJSON.getValue() && fileType.equals(REC) || !RecordingStorerScreen.useJSON.getValue() && fileType.equals(JSON);

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

    private static int handleQuickSlotCommand(CommandContext<FabricClientCommandSource> context, String command) {
        final int slot = IntegerArgumentType.getInteger(context, SLOT);
        if (slot < 1 || 9 < slot) {
            context.getSource().sendFeedback(Text.literal("Slot Index out of range"));
            return 0;
        }
        return switch (command) {
            case LOAD -> {
                QuickSlots.loadRecording(slot - 1);
                yield 1;
            }
            case STORE -> {
                QuickSlots.storeRecording(slot - 1);
                yield 1;
            }
            case CLEAR -> {
                QuickSlots.clearQuickSlot(slot - 1);
                yield 1;
            }
            default -> 1;
        };
    }
}
