package xyz.quazaros.allitems73.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record BaseItemListPayload(List<String> itemNames) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, BaseItemListPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            BaseItemListPayload::itemNames,
            BaseItemListPayload::new
    );

    public static final Type<BaseItemListPayload> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath("allitems73", "base_item_list"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}