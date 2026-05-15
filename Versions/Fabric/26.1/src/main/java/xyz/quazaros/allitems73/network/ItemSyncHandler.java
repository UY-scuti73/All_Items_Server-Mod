package xyz.quazaros.allitems73.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;

public class ItemSyncHandler {

    /* ========= SERVER ========= */

    public static void registerCommon() {
        // Client -> Server: menu opened / load requested
        PayloadTypeRegistry.serverboundPlay()
                .register(MenuOpenPayload.TYPE, MenuOpenPayload.CODEC);

        // Server -> Client: sync full itemList
        PayloadTypeRegistry.clientboundPlay()
                .register(SyncItemListPayload.TYPE, SyncItemListPayload.CODEC);

        // Handle menu open requests
        ServerPlayNetworking.registerGlobalReceiver(MenuOpenPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            itemList serverList = main.getItemList();

            // 1. Update server's itemList based on THIS player's inventory
            serverList.updateList(player.getInventory(), player.getName().getString());
            FileHandler.saveCurrentProgress();

            // 2. Build sync payload from updated serverList
            List<ItemDataPayloadEntry> entries = new ArrayList<>();
            for (item i : serverList.items) {
                if (i.is_found && i.data != null) {
                    entries.add(new ItemDataPayloadEntry(
                            i.data.item_name,
                            i.data.item_founder,
                            i.data.item_time
                    ));
                }
            }

            // 3. Send to this player (or broadcast if you prefer)
            ServerPlayNetworking.send(player, new SyncItemListPayload(entries));
        });
    }

    /* ========= CLIENT ========= */

    public static void registerClient() {
        // Receive server's list
        ClientPlayNetworking.registerGlobalReceiver(SyncItemListPayload.TYPE, (payload, context) -> {
            itemList list = main.getItemList();

            // Reset client copy
            list.items.forEach(i -> i.is_found = false);

            // Apply new data
            for (ItemDataPayloadEntry entry : payload.items()) {
                item it = list.get(entry.itemName());
                if (it != null) {
                    it.submit(entry.itemFounder(), entry.itemTime());
                }
            }

            System.out.println("[AllItems73] Client itemList updated from server");
        });
    }

    /* ========= CLIENT API ========= */

    /** Client-only: notify server that the menu was opened / load button clicked. */
    public static void notifyMenuOpened() {
        System.out.println("[AllItems73] Client notifying server: menu opened");
        ClientPlayNetworking.send(new MenuOpenPayload());
    }
}