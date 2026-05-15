package xyz.quazaros.allitems73.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MenuOpenPayload() implements CustomPayload {

    public static final CustomPayload.Id<MenuOpenPayload> ID =
            new CustomPayload.Id<>(Identifier.of("allitems73", "menu_open"));

    public static final PacketCodec<PacketByteBuf, MenuOpenPayload> CODEC =
            PacketCodec.unit(new MenuOpenPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}