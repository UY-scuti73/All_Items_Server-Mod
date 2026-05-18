package xyz.quazaros.allitems73.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SyncItemListPayload(List<ItemDataPayloadEntry> items) implements CustomPacketPayload {

    public static final Type<SyncItemListPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath("allitems73", "sync_item_list"));

    public static final StreamCodec<FriendlyByteBuf, SyncItemListPayload> CODEC =
            StreamCodec.of(
                    (buf, value) -> {
                        buf.writeCollection(value.items(), (b, entry) -> {
                            b.writeUtf(entry.itemName());
                            b.writeUtf(entry.itemFounder());
                            b.writeUtf(entry.itemTime());
                        });
                    },
                    buf -> new SyncItemListPayload(
                            buf.readList(b -> new ItemDataPayloadEntry(
                                    b.readUtf(),
                                    b.readUtf(),
                                    b.readUtf()
                            ))
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}