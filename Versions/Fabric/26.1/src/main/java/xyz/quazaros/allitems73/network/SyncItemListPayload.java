package xyz.quazaros.allitems73.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record SyncItemListPayload(List<ItemDataPayloadEntry> items) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncItemListPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("allitems73", "sync_item_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncItemListPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        List<ItemDataPayloadEntry> list = payload.items();
                        buf.writeInt(list.size());
                        for (ItemDataPayloadEntry entry : list) {
                            buf.writeUtf(entry.itemName());
                            buf.writeUtf(entry.itemFounder());
                            buf.writeUtf(entry.itemTime());
                        }
                    },
                    buf -> {
                        int size = buf.readInt();
                        List<ItemDataPayloadEntry> list = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            String itemName = buf.readUtf();
                            String itemFounder = buf.readUtf();
                            String itemTime = buf.readUtf();
                            list.add(new ItemDataPayloadEntry(itemName, itemFounder, itemTime));
                        }
                        return new SyncItemListPayload(list);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}