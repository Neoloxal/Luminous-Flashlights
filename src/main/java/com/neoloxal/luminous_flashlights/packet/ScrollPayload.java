package com.neoloxal.luminous_flashlights.packet;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ScrollPayload(double scrollDelta) implements CustomPacketPayload {
    public static final Type<ScrollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "scroll"));

    public static final StreamCodec<FriendlyByteBuf, ScrollPayload> CODEC = StreamCodec.of(
            (buf, playload) -> buf.writeDouble(playload.scrollDelta),
            buf -> new ScrollPayload(buf.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
