package xyz.quazaros.allitems73.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemData;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemSyncHandler {

    static {
        // 1. MenuOpen: Client -> Server (C2S)
        PayloadTypeRegistry.playC2S()
                .register(MenuOpenPayload.ID, MenuOpenPayload.CODEC);

        // 2. SyncItemList: Server -> Client (S2C)
        PayloadTypeRegistry.playS2C()
                .register(SyncItemListPayload.ID, SyncItemListPayload.CODEC);

        // 3. BaseItemList: Server -> Client (S2C)
        PayloadTypeRegistry.playS2C()
                .register(BaseItemListPayload.ID, BaseItemListPayload.CODEC);
    }

    public static void init() {}

    public static void registerReceivers() {
        // Handle menu open requests from client
        ServerPlayNetworking.registerGlobalReceiver(MenuOpenPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            itemList serverList = main.getItemList();

            // 1. Update server's itemList based on player's inventory
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

            // 3. Send response back to the player
            ServerPlayNetworking.send(player, new SyncItemListPayload(entries));
        });
    }

    public static void sendBaseListToPlayer(ServerPlayerEntity player) {
        List<String> names = FileHandler.getBaseItemList();
        ServerPlayNetworking.send(player, new BaseItemListPayload(names));
    }

    public static void sendSyncPacketToClient(ServerPlayerEntity player) {
        ArrayList<itemData> progress = FileHandler.getItemData();

        List<ItemDataPayloadEntry> entries = progress.stream()
                .map(data -> new ItemDataPayloadEntry(data.item_name, data.item_founder, data.item_time))
                .collect(Collectors.toList());

        ServerPlayNetworking.send(player, new SyncItemListPayload(entries));
    }
}