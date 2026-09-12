package com.neoloxal.luminous_flashlights.item.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum Color implements StringRepresentable {
    WHITE("white", 0xffffff, 0f),
    RED("red", 0xff0000, 1f);

    final int hex_color;
    final float identifier;
    final String name;

    Color(String name, int hexColor, float identifier) {
        this.name = name;
        this.hex_color = hexColor;
        this.identifier = identifier;
    }

    public int getHexColor() {
        return hex_color;
    }

    public float getIdentifier() {
        return identifier;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static final Codec<Color> CODEC = StringRepresentable.fromEnum(Color::values);

    private static final IntFunction<Color> BY_ID = ByIdMap.continuous(
            Color::ordinal,
            Color.values(),
            ByIdMap.OutOfBoundsStrategy.ZERO
    );
    public static final StreamCodec<ByteBuf, Color> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Color::ordinal);
}
