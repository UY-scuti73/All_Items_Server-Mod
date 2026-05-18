package xyz.quazaros.allitems73.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record BaseItemListPayload(List<String> itemNames) implements CustomPacketPayload {


    public static final CustomPacketPayload.Type<BaseItemListPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("allitems73", "base_item_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BaseItemListPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            BaseItemListPayload::itemNames,
            BaseItemListPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}