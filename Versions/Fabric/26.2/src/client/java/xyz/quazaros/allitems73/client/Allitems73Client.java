package xyz.quazaros.allitems73.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import xyz.quazaros.allitems73.client.events.onClickEvent;
import xyz.quazaros.allitems73.client.network.ClientItemSyncHandler;
import xyz.quazaros.allitems73.files.WorldKeys;
import xyz.quazaros.allitems73.main;

public class Allitems73Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.getCurrentServer() != null) {
                WorldKeys.setClientWorldKey(client.getCurrentServer().ip);
                main.setNewItemList();
            }
        });
        ClientItemSyncHandler.registerClient();
        onClickEvent.init();
    }
}
