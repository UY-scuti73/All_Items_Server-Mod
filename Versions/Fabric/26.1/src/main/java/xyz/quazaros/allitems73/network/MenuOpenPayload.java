package xyz.quazaros.allitems73.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> Server: player opened the menu / pressed load button.
 * No extra data needed; server knows the player from context.
 */
public record MenuOpenPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MenuOpenPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("allitems73", "menu_open"));

    public static final StreamCodec<FriendlyByteBuf, MenuOpenPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        // no data
                    },
                    buf -> new MenuOpenPayload()
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}