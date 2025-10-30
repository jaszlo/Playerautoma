package net.jasper.mod.automation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.jasper.mod.gui.option.PlayerautomaOptionsScreen;
import net.jasper.mod.util.data.TaskQueue;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;

import static net.jasper.mod.util.ClientHelpers.getInteractionManager;
import static net.jasper.mod.util.ClientHelpers.getPlayerEntity;

/**
 * This class is responsible for re-stacking items in the inventory when replaying
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryAutomation {
    private static boolean doAutomation = true;

    public static final TaskQueue inventoryTasks = new TaskQueue(TaskQueue.HIGH_PRIORITY);

    @SuppressWarnings("java:S3776")
    public static void register() {
        inventoryTasks.register("inventoryTasks");
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (GameMode.CREATIVE.equals(player.getGameMode())) {
                return ActionResult.PASS;
            }

            // If is disabled in settings do nothing
            if (Boolean.FALSE.equals(PlayerautomaOptionsScreen.restackBlocksOption.getValue())) {
                return ActionResult.PASS;
            }


            PlayerInventory inventory = player.getInventory();
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            ItemStack currentItem = inventory.getStack(inventory.getSelectedSlot());
            // Check if item is a placeable item in the world
            if (Block.getBlockFromItem(currentItem.getItem()) == Block.getBlockFromItem(Items.AIR)) {
                return ActionResult.PASS;
            }
            // Check if only one is left now as if this callback is called we will place that last block
            if (currentItem.getCount() > 1 || currentItem.getItem() == Items.AIR) {
                return ActionResult.PASS;
            }

            // Check if this automation was already done for this tick!
            if (!doAutomation) {
                return ActionResult.PASS;
            }

            // Check if another Stack of the item in mainHand is in the Inventory
            int exceptedSlot = inventory.getEmptySlot();
            for (int i = 0; i < inventory.getMainStacks().size(); i++) { //NOSONAR
                ItemStack item = inventory.getStack(i);
                if (i == exceptedSlot || item.getItem() != currentItem.getItem()) {
                    continue;
                }

                final int fromSlot = i;
                if (player.getInventory().getStack(fromSlot).getItem() == Items.AIR) {
                    break;
                }

                doAutomation = false;
                MinecraftClient client = MinecraftClient.getInstance();
                InventoryScreen screen = new InventoryScreen(getPlayerEntity());

                // Open Inventory & move Item in a later tick
                inventoryTasks.add(() -> {
                    // Requesting to pick Item from Inventory into mainHand and Opening Inventory to allow this to happen on Servers
                    client.setScreen(screen);
                    getInteractionManager().clickSlot(
                            screen.getScreenHandler().syncId,
                            fromSlot,
                            inventory.getSelectedSlot(),
                            SlotActionType.SWAP,
                            getPlayerEntity()
                    );
                });

                // Close Inventory in a later tick
                inventoryTasks.add(() -> {
                    // Closing Inventory and clearing flag so this automation can run again for other Blocks
                    client.setScreen(null);
                    doAutomation = true;
                });
                break;

            }
            return ActionResult.PASS;
        });
    }
}
