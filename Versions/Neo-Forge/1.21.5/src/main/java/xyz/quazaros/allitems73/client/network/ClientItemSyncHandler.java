package xyz.quazaros.allitems73.client.network;

import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;
import xyz.quazaros.allitems73.network.ItemDataPayloadEntry;
import xyz.quazaros.allitems73.network.MenuOpenPayload;
import xyz.quazaros.allitems73.network.SyncItemListPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class ClientItemSyncHandler {

    public static final List<ItemDataPayloadEntry> clientCache = new ArrayList<>();

    public static void handleSync(final SyncItemListPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            itemList list = main.getItemList();

            // If the list hasn't been initialized yet by BaseItemListPayload,
            // we should just store the progress in the cache and exit.
            if (list.items.isEmpty()) {
                clientCache.clear();
                clientCache.addAll(payload.items());
                return;
            }

            // Reset all items locally before applying server data
            for (item i : list.items) {
                i.is_found = false;
            }

            for (ItemDataPayloadEntry entry : payload.items()) {
                item it = list.get(entry.itemName().trim());
                if (it != null) {
                    it.submit(entry.itemFounder(), entry.itemTime());
                }
            }
        });
    }

    public static void registerClient() {}

    public static void notifyMenuOpened() {
        // Use the NeoForge networking API to send the payload to the server
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new MenuOpenPayload());
    }
}