package xyz.quazaros.allitems73;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.network.ItemSyncHandler;

import static xyz.quazaros.allitems73.files.WorldKeys.setWorldKey;

public class main implements ModInitializer {
    private static itemList ItemList = new itemList();

    @Override
    public void onInitialize() {
        // 1. Tell the game these packets exist
        ItemSyncHandler.init();

        // 2. Tell the server how to handle the ones it receives
        ItemSyncHandler.registerReceivers();

        registerServerLifecycleCallbacks();
    }
    private void onServerOpen(MinecraftServer server) {
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
