package com.neoloxal.luminous_flashlights.datagen;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.paint_palette_lib.datagen.DatagenUtils;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;

public class ModLangProvider extends LanguageProvider implements DatagenUtils.CanvasLanguageProvider {
    public ModLangProvider(PackOutput output, String modid) {
        super(output, modid, "en_us");
    }

    @Override
    protected void addTranslations() {
        generateLanguage(LuminousFlashlights.MODID, this::add);

        Arrays.stream(Color.values()).iterator().forEachRemaining(
                color -> add(
                        "item.luminous_flashlights.flashlight.tooltip.%s".formatted(color.getSerializedName()),
                        WordUtils.capitalizeFully(color.getSerializedName().replace('_', ' '))
                )
        );

        add("item.luminous_flashlights.flashlight.tooltip", "Swap hands while holding a flashlight in your offhand and a lens in your main hand.");

        add("sounds.luminous_flashlights.flashlight_on", "Flashlight turns on");
        add("sounds.luminous_flashlights.flashlight_off", "Flashlight turns off");
        add("sounds.luminous_flashlights.swap_lens", "Lens is swapped");
        add("sounds.luminous_flashlights.focus_change", "Focus changes");

        add("key.luminous_flashlights.flashlight_focus", "Change flashlight focus");
    }
}
