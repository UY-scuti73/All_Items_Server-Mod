package xyz.quazaros.allitems73.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import xyz.quazaros.allitems73.files.WorldKeys;
import xyz.quazaros.allitems73.main;
import xyz.quazaros.allitems73.network.ItemSyncHandler;

import static xyz.quazaros.allitems73.client.events.onClickEvent.registerKeyPressed;

public class Allitems73Client implements ClientModInitializer {
    public static final Identifier ITEM_SYNC_ID = Identifier.fromNamespaceAndPath("allitems73", "sync_items");

    @Override
    public void onInitializeClient() {
        registerKeyPressed();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.getCurrentServer() != null) {
                WorldKeys.setClientWorldKey(client.getCurrentServer().ip);
                main.setNewItemList();
            }
        });
        ItemSyncHandler.registerClient();
    }
}
