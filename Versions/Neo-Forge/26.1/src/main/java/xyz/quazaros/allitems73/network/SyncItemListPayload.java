package xyz.quazaros.allitems73.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SyncItemListPayload(List<ItemDataPayloadEntry> items) implements CustomPacketPayload {

    public static final Type<SyncItemListPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath("allitems73", "sync_item_list"));

    public static final StreamCodec<FriendlyByteBuf, ItemDataPayloadEntry> ENTRY_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ItemDataPayloadEntry::itemName,
            ByteBufCodecs.STRING_UTF8, ItemDataPayloadEntry::itemFounder,
            ByteBufCodecs.STRING_UTF8, ItemDataPayloadEntry::itemTime,
            ItemDataPayloadEntry::new
    );

    public static final StreamCodec<FriendlyByteBuf, SyncItemListPayload> CODEC = StreamCodec.composite(
            ENTRY_CODEC.apply(ByteBufCodecs.list()), SyncItemListPayload::items,
            SyncItemListPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}