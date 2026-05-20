package xyz.quazaros.allitems73.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemData;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemSyncHandler {

    /* ========= SERVER ========= */
    public static void registerCommon() {
        // Client -> Server: menu opened / load requested
        PayloadTypeRegistry.serverboundPlay()
                .register(MenuOpenPayload.TYPE, MenuOpenPayload.CODEC);

        // Server -> Client: sync full itemList
        PayloadTypeRegistry.clientboundPlay()
                .register(SyncItemListPayload.TYPE, SyncItemListPayload.CODEC);

        // Server -> Client: base item list
        PayloadTypeRegistry.clientboundPlay()
                .register(BaseItemListPayload.TYPE, BaseItemListPayload.CODEC);

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

            // 3. Send to this player
            ServerPlayNetworking.send(player, new SyncItemListPayload(entries));
        });
    }

    /**
     * Server-only helper: send base list to a player.
     */
    public static void sendBaseListToPlayer(ServerPlayer player) {
        List<String> names = FileHandler.getBaseItemList();
        ServerPlayNetworking.send(player, new BaseItemListPayload(names));
    }

    /**
     * Server-only helper: send saved progress (from file) to a player.
     */
    public static void sendSyncPacketToClient(ServerPlayer player) {
        ArrayList<itemData> progress = FileHandler.getItemData();
        List<ItemDataPayloadEntry> entries = progress.stream()
                .map(data -> new ItemDataPayloadEntry(data.item_name, data.item_founder, data.item_time))
                .collect(Collectors.toList());
        ServerPlayNetworking.send(player, new SyncItemListPayload(entries));
    }
}