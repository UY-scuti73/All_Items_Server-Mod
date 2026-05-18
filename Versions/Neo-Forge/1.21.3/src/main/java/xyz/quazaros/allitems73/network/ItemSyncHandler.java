package xyz.quazaros.allitems73.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.quazaros.allitems73.client.network.ClientItemSyncHandler;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemData;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemSyncHandler {
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // 1. Client -> Server (This is what notifyMenuOpened uses)
        registrar.playToServer(
                MenuOpenPayload.ID,
                MenuOpenPayload.CODEC,
                ItemSyncHandler::handleMenuOpen // This method must exist in ItemSyncHandler
        );

        // 2. Server -> Client (This is what handleSync uses)
        registrar.playToClient(
                SyncItemListPayload.ID,
                SyncItemListPayload.CODEC,
                ClientItemSyncHandler::handleSync
        );

        // 3. Server's item list
        registrar.playToClient(
                BaseItemListPayload.ID,
                BaseItemListPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        // Update the client-side itemList with these names
                        main.getItemList().initializeFromNames(payload.itemNames());
                    });
                }
        );
    }

    public static void sendBaseListToPlayer(ServerPlayer player) {
        List<String> names = FileHandler.getBaseItemList();
        PacketDistributor.sendToPlayer(player, new BaseItemListPayload(names));
    }

    public static void sendSyncPacketToClient(ServerPlayer player) {
        // 1. Get the current progress data from your FileHandler
        ArrayList<itemData> progress = FileHandler.getItemData();

        // 2. Convert to the Payload Entry format your SyncItemListPayload expects
        List<ItemDataPayloadEntry> entries = progress.stream()
                .map(data -> new ItemDataPayloadEntry(data.item_name, data.item_founder, data.item_time))
                .collect(Collectors.toList());

        // 3. Send it
        PacketDistributor.sendToPlayer(player, new SyncItemListPayload(entries));
    }

    /**
     * Handler for MenuOpenPayload on the Server.
     */
    private static void handleMenuOpen(MenuOpenPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            itemList serverList = main.getItemList();

            // 1. Update server's itemList based on player's inventory
            // Inventory in NeoForge/Mojang maps is usually accessed via player.getInventory()
            serverList.updateList(player.getInventory(), player.getName().getString());
            FileHandler.saveCurrentProgress();

            // 2. Build sync payload
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
            context.reply(new SyncItemListPayload(entries));
        });
    }
}