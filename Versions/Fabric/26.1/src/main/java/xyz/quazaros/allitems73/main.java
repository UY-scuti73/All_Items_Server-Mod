package xyz.quazaros.allitems73;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.network.ItemSyncHandler;

import static xyz.quazaros.allitems73.files.WorldKeys.setWorldKey;

public class main implements ModInitializer {

    private static itemList ItemList;

    @Override
    public void onInitialize() {
        // Register payload types + handlers
        ItemSyncHandler.registerCommon();

        registerServerLifecycleCallbacks();

        // When a player joins, send base list + current progress
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ItemSyncHandler.sendBaseListToPlayer(player);
            ItemSyncHandler.sendSyncPacketToClient(player);
        });
    }

    private void onServerOpen(MinecraftServer server) {
        FileHandler.initDefaultList();
        setWorldKey(server);
        ItemList = new itemList();
    }

    private void onServerClose(MinecraftServer server) {
        FileHandler.saveCurrentProgress();
    }

    private void registerServerLifecycleCallbacks() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerOpen);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerClose);
    }

    public static itemList getItemList() {
        return ItemList;
    }

    public static void updateItemList(Inventory inv, String name) {
        ItemList.updateList(inv, name);
        FileHandler.saveCurrentProgress();
    }

    public static void updateItemList(ItemStack item, String name) {
        ItemList.updateList(item, name);
        FileHandler.saveCurrentProgress();
    }

    public static void setNewItemList() {
        ItemList = new itemList();
    }
}