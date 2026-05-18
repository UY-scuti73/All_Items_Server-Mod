package xyz.quazaros.allitems73;

import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import xyz.quazaros.allitems73.client.events.onClickEvent;
import xyz.quazaros.allitems73.client.network.ClientItemSyncHandler;
import xyz.quazaros.allitems73.files.FileHandler;
import xyz.quazaros.allitems73.files.WorldKeys;
import xyz.quazaros.allitems73.items.itemList;
import xyz.quazaros.allitems73.network.ItemSyncHandler;

@Mod(main.MODID)
public class main {
    public static final String MODID = "allitems73";
    private static itemList ItemList = new itemList();

    public main(IEventBus modEventBus) {
        // 1. Common Payload Registration (Correct)
        modEventBus.addListener(ItemSyncHandler::registerPayloads);

        // 2. Server-side events (Correct)
        NeoForge.EVENT_BUS.addListener(this::onServerOpen);
        NeoForge.EVENT_BUS.addListener(this::onServerClose);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);

        // 3. Client-side only setup
        if (FMLEnvironment.getDist().isClient()) {
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(onClickEvent::registerKeyMappings);

            // Only register this once, and ONLY on the client!
            NeoForge.EVENT_BUS.addListener(onClickEvent::onClientTick);

            // Register THIS class instance so onClientJoin works
            NeoForge.EVENT_BUS.register(this);
        }
    }

    private void onServerOpen(ServerStartedEvent event) {
        FileHandler.initDefaultList();
        WorldKeys.setWorldKey(event.getServer());
        ItemList = new itemList();
    }

    private void onServerClose(ServerStoppedEvent event) {
        FileHandler.saveCurrentProgress();
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ItemSyncHandler.sendBaseListToPlayer(serverPlayer);
        }
    }

    public static itemList getItemList() {
        return ItemList;
    }

    public static void updateItemList(Container inv, String name) {
        ItemList.updateList(inv, name);
        FileHandler.saveCurrentProgress();
    }

    public static void setNewItemList() {
        ItemList = new itemList();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // No longer strictly needed for NeoForge, but harmless if empty
        ClientItemSyncHandler.registerClient();
    }

    // This listener now works because of NeoForge.EVENT_BUS.register(this)
    @SubscribeEvent
    public void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!minecraft.isLocalServer()) {
            String ip = "unknown";
            if (minecraft.getCurrentServer() != null) {
                ip = minecraft.getCurrentServer().ip;
            }
            WorldKeys.setClientWorldKey(ip);
            main.setNewItemList();
        }
    }
}