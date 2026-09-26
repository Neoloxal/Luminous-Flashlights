package com.neoloxal.luminous_flashlights.config;

import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class LuminousClient {
    public static final LuminousClient CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.EnumValue<ModDataComponents.ScrollSpeed> FOCUS_SCROLL_SPEED;
    public final ModConfigSpec.DoubleValue BRIGHTNESS_OFFSET;

    public LuminousClient(ModConfigSpec.Builder builder) {
        FOCUS_SCROLL_SPEED = builder
                .translation("config.luminous_flashlights.focus_scroll_speed")
                .comment("The speed to adjust focus.")
                .defineEnum("focus_scroll_speed", ModDataComponents.ScrollSpeed.FAST);

        BRIGHTNESS_OFFSET = builder
                .translation("config.luminous_flashlights.brightness_offset")
                .comment("The amount to adjust brightness.")
                .defineInRange("brightness_offset", 0f, -4f, 10f);
    }

    static {
        Pair<LuminousClient, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(LuminousClient::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
