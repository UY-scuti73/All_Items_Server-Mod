package xyz.quazaros.allitems73;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.items.itemList;

import static xyz.quazaros.allitems73.files.WorldKeys.setWorldKey;

public class main implements ModInitializer {
    public static itemList ItemList = new itemList();

    @Override
    public void onInitialize() {
        registerServerLifecycleCallbacks();
    }

    private void registerServerLifecycleCallbacks() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerOpen);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerClose);
    }

    private void onServerOpen(MinecraftServer server) {
        setWorldKey(server);
        ItemList = new itemList();
    }

    private void onServerClose(MinecraftServer server) {
        FileHandler.saveCurrentProgress();
    }
}
