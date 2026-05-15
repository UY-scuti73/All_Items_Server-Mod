package xyz.quazaros.allitems73.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;

public class ItemSyncHandler {
    /* ========= SERVER ========= */

    static {
        // 1. MenuOpen: Client -> Server (This is C2S / SERVERBOUND)
        PayloadTypeRegistry.playC2S()
                .register(MenuOpenPayload.ID, MenuOpenPayload.CODEC);

        // 2. SyncItemList: Server -> Client (This is S2C / CLIENTBOUND)
        PayloadTypeRegistry.playS2C()
                .register(SyncItemListPayload.ID, SyncItemListPayload.CODEC);
    }

    public static void init() {}

    public static void registerReceivers() {
        // Handle menu open requests
        ServerPlayNetworking.registerGlobalReceiver(MenuOpenPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
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
}