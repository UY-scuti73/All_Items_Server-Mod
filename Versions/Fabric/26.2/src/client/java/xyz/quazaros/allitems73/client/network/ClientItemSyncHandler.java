package xyz.quazaros.allitems73.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;
import xyz.quazaros.allitems73.network.BaseItemListPayload;
import xyz.quazaros.allitems73.network.ItemDataPayloadEntry;
import xyz.quazaros.allitems73.network.MenuOpenPayload;
import xyz.quazaros.allitems73.network.SyncItemListPayload;

import java.util.ArrayList;
import java.util.List;

public class ClientItemSyncHandler {
    public static final List<ItemDataPayloadEntry> clientCache = new ArrayList<>();

    public static void registerClient() {
        // Receive the server's base item list and initialize the client-side list
        ClientPlayNetworking.registerGlobalReceiver(BaseItemListPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> main.getItemList().initializeFromNames(payload.itemNames()));
        });

        // Receive progress sync from server
        ClientPlayNetworking.registerGlobalReceiver(SyncItemListPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                itemList list = main.getItemList();

                clientCache.clear();
                clientCache.addAll(payload.items());

                list.items.forEach(i -> i.is_found = false);
                for (ItemDataPayloadEntry entry : payload.items()) {
                    item it = list.get(entry.itemName());
                    if (it != null) {
                        it.submit(entry.itemFounder(), entry.itemTime());
                    }
                }
            });
        });
    }

    public static void notifyMenuOpened() {
        ClientPlayNetworking.send(new MenuOpenPayload());
    }
}
