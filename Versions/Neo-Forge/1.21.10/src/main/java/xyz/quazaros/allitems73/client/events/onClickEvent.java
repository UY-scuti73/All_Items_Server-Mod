package xyz.quazaros.allitems73.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import com.nimbusds.oauth2.sdk.id.Identifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jline.keymap.KeyMap;
import org.lwjgl.glfw.GLFW;
import xyz.quazaros.allitems73.client.inventory.VirtualChestScreen;
import xyz.quazaros.allitems73.client.network.ClientItemSyncHandler;

public class onClickEvent {

    public static KeyMapping OPEN_INVENTORY_KEY;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_INVENTORY_KEY = new KeyMapping(
                "key.allitems73.openinventory",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KeyMapping.Category.INVENTORY
        );

        event.register(OPEN_INVENTORY_KEY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return; // optional safety
        while (OPEN_INVENTORY_KEY.consumeClick()) {
            onInventoryKeyPressed(Minecraft.getInstance(), false);
        }
    }

    public static void onInventoryKeyPressed(Minecraft client, boolean filtered) {
        if (client.player == null || client.getConnection() == null) return;

        // Notify server to sync data
        ClientItemSyncHandler.notifyMenuOpened();

        // Open the screen
        client.setScreen(new VirtualChestScreen(filtered));
    }
}