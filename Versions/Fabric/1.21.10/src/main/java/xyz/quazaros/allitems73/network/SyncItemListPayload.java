package xyz.quazaros.allitems73.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record SyncItemListPayload(List<ItemDataPayloadEntry> items) implements CustomPayload {

    public static final CustomPayload.Id<SyncItemListPayload> ID =
            new CustomPayload.Id<>(Identifier.of("allitems73", "sync_item_list"));

    public static final PacketCodec<PacketByteBuf, SyncItemListPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeCollection(value.items(), (b, entry) -> {
                            b.writeString(entry.itemName());
                            b.writeString(entry.itemFounder());
                            b.writeString(entry.itemTime());
                        });
                    },
                    buf -> new SyncItemListPayload(
                            buf.readList(b -> new ItemDataPayloadEntry(
                                    b.readString(),
                                    b.readString(),
                                    b.readString()
                            ))
                    )
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}