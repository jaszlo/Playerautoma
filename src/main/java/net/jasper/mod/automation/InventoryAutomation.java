package net.jasper.mod.automation;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.jasper.mod.PlayerAutomaClient;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;



public class InventoryAutomation {

    private static boolean doAutomation = true;

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;

    /**
     * Will re-stack items in your mainHand automatically like InventoryTweaks
     */
    public static void registerInventoryAutomation() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            PlayerInventory inv = player.getInventory();
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            // Check if only one is left now as if this callback is called we will place that last block
            ItemStack currentItem = inv.getStack(inv.selectedSlot);
            if (currentItem.getCount() > 1 || currentItem.getItem() == Items.AIR) {
                return ActionResult.PASS;
            }

            // Check if this automation was already done for this tick!
            if (!doAutomation) {
                return ActionResult.PASS;
            }

            // Check if another Stack of the item in mainHand is in the Inventory
            int exceptedSlot = inv.selectedSlot;
            for (int i = 0; i < inv.main.size(); i++) {
                ItemStack item = inv.getStack(i);
                if (i == exceptedSlot || item.getItem() != currentItem.getItem()) {
                    continue;
                }

                LOGGER.info("Another Stack of " + item.getName() + " in Slot " + i + " will be placed in MainHand");
                final int from_slot = i;

                if (player.getInventory().getStack(from_slot).getItem() == Items.AIR) {
                    break;
                }
                doAutomation = false;

                // Add the Inventory changing in a task list, so it will be executed in a later tick
                PlayerAutomaClient.inventoryTasks.add("Open Inventory & move Item", () -> {
                    LOGGER.info("Requesting to pick Item from Inventory into mainHand and Opening Inventory to allow this to happen on Servers.");
                    MinecraftClient client = MinecraftClient.getInstance();
                    Screen invScreen = new InventoryScreen(player);
                    client.setScreen(invScreen);
                    assert client.interactionManager != null;
                    client.interactionManager.pickFromInventory(from_slot);
                });

                PlayerAutomaClient.inventoryTasks.add("Close Inventory", () -> {
                    LOGGER.info("Closing Inventory and clearing flag so this automation can run again for other Blocks!");
                    MinecraftClient.getInstance().setScreen(null);
                    doAutomation = true;
                });
                break;

            }

            return ActionResult.PASS;
        });
    }
}
