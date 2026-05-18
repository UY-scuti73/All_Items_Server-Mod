package xyz.quazaros.allitems73.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MenuOpenPayload() implements CustomPacketPayload {

    public static final Type<MenuOpenPayload> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath("allitems73", "menu_open"));

    public static final StreamCodec<FriendlyByteBuf, MenuOpenPayload> CODEC =
            StreamCodec.unit(new MenuOpenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}