package com.neoloxal.luminous_flashlights.item.data_component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum Color implements StringRepresentable {
    GLASS("glass", 0f, 0xffffff, 0.5f, 3f),
    RED("red", 1f, 0xff0000, 4f),
    ORANGE("orange", 2f, 0xfca103, 4f),
    YELLOW("yellow", 3f, 0xfce703, 4f),
    LIME("lime", 4f, 0xbefc03, 4f),
    GREEN("green", 5f, 0x35fc03, 4f),
    CYAN("cyan", 6f, 0x03fcce, 4f),
    LIGHT_BLUE("light_blue", 7f, 0x03bafc, 4f),
    BLUE("blue", 8f, 0x036ffc, 4f),
    PURPLE("purple", 9f, 0x7b03fc, 4f),
    MAGENTA("magenta", 10f, 0xe600ff, 4f),
    PINK("pink", 11f, 0xff0077, 4f),
    WHITE("white", 12f, 0xffedba, 4f),
    LIGHT_GRAY("light_gray", 13f, 0xffedba, 2f),
    GRAY("gray", 14f, 0xffedba, 1f),
    BLACK("black", 15f, 0x474747, 0f, 4f),
    BROWN("brown", 16f, 0x542700, 5f),
    NULL("null", -1f, 0xdf03fc, 10f);

    final int hex_color;
    final float brightness;
    final float chatBrightness;

    final float identifier;
    final String name;

    Color(String name, float identifier, int hexColor, float brightness, float chatBrightness) {
        this.name = name;
        this.identifier = identifier;

        this.hex_color = hexColor;
        this.brightness = brightness;
        this.chatBrightness = chatBrightness;
    }

    Color(String name, float identifier, int hexColor, float brightness) {
        this(name, identifier, hexColor, brightness, brightness);
    }

    public int getHexColor() {
        return hex_color;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getChatBrightness() {
        return chatBrightness;
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
