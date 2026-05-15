package xyz.quazaros.allitems73.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.quazaros.allitems73.client.network.ClientItemSyncHandler;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.item;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.main;

import java.util.ArrayList;
import java.util.List;

public class ItemSyncHandler {

    /**
     * In NeoForge 1.21.1, this is called via the mod event bus listener
     * registered in the main class constructor.
     */
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