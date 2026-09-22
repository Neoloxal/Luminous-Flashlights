package com.neoloxal.luminous_flashlights.item;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.paint_palette_lib.Palette;
import com.neoloxal.paint_palette_lib.PaletteUtils;
import com.neoloxal.paint_palette_lib.registrar.ItemRegistrar;
import net.minecraft.world.item.Item;

public class ModItems extends ItemRegistrar {
    public ModItems() {
        super(LuminousFlashlights.MODID);
    }

    @Override
    protected void registerItems() {
        basicItem("flashlight", () -> new Flashlight(new Item.Properties()
                .stacksTo(1)
                .component(ModDataComponents.COLOR.get(), Color.GLASS)
        ));
        PaletteUtils.Canvas.generateName(getItem("flashlight"));
    }
}
