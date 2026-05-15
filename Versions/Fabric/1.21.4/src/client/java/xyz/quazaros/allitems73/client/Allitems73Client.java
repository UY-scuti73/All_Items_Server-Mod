package xyz.quazaros.allitems73.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.Identifier;
import xyz.quazaros.allitems73.files.WorldKeys;
import xyz.quazaros.allitems73.main;
import xyz.quazaros.allitems73.client.network.ClientItemSyncHandler;
import xyz.quazaros.allitems73.network.ItemSyncHandler;

import static xyz.quazaros.allitems73.client.events.onClickEvent.registerKeyPressed;

public class Allitems73Client implements ClientModInitializer {
    public static final Identifier ITEM_SYNC_ID = Identifier.of("allitems73", "sync_items");

    @Override
    public void onInitializeClient() {
        ItemSyncHandler.init();

        ClientItemSyncHandler.registerClient();

        registerKeyPressed();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.getServer() != null) {
                WorldKeys.setClientWorldKey(client.getServer().getServerIp());
                main.setNewItemList();
            }
        });
    }
}
