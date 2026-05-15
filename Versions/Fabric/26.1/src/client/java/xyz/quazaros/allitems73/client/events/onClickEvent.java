package xyz.quazaros.allitems73.client.events;

import com.mojang.blaze3d.platform.InputConstants;          // was client.util.InputUtil
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;                    // was client.option.KeyBinding
import org.lwjgl.glfw.GLFW;
import xyz.quazaros.allitems73.client.Allitems73Client;
import xyz.quazaros.allitems73.client.inventory.VirtualChestScreen;
import xyz.quazaros.allitems73.main;
import xyz.quazaros.allitems73.network.ItemSyncHandler;

public class onClickEvent {

    private static KeyMapping keyBinding; // Store as a static field for the listener

    public static void registerKeyPressed() {
        keyBinding = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.allitems73.openinventory",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        KeyMapping.Category.MISC
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.consumeClick()) {
                onInventoryKeyPressed(client, false);
            }
        });
    }

    public static void onInventoryKeyPressed(Minecraft client, boolean filtered) {
        if (client.player == null || client.getConnection() == null) {
            return;
        }

        ItemSyncHandler.notifyMenuOpened();

        client.setScreen(new VirtualChestScreen(filtered));
    }
}