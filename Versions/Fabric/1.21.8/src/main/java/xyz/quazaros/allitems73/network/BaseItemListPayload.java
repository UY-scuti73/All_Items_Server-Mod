package xyz.quazaros.allitems73.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record BaseItemListPayload(List<String> itemNames) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, BaseItemListPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()),
            BaseItemListPayload::itemNames,
            BaseItemListPayload::new
    );

    public static final CustomPayload.Id<BaseItemListPayload> ID =
            new CustomPayload.Id<>(Identifier.of("allitems73", "base_item_list"));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}