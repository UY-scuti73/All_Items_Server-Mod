package xyz.quazaros.allitems73.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import xyz.quazaros.allitems73.client.inventory.VirtualChestScreen;
import xyz.quazaros.allitems73.client.network.ClientItemSyncHandler;

public final class onClickEvent {

    private static KeyMapping keyBinding;

    public static void init() {
        keyBinding = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.allitems73.openinventory",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        KeyMapping.Category.INVENTORY
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.consumeClick()) {
                onInventoryKeyPressed(client, false);
            }
        });
    }

    public static void onInventoryKeyPressed(Minecraft client, boolean filtered) {
        if (client.player == null || client.getConnection() == null) return;

        ClientItemSyncHandler.notifyMenuOpened();
        client.setScreenAndShow(new VirtualChestScreen(filtered));
    }

    private onClickEvent() {}
}