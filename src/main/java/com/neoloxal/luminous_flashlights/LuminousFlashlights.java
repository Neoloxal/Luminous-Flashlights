package com.neoloxal.luminous_flashlights;

import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.item.ModItems;
import com.neoloxal.paint_palette_lib.Palette;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(LuminousFlashlights.MODID)
public class LuminousFlashlights {
    public static final String MODID = "luminous_flashlights";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ModItems MOD_ITEMS = new ModItems();

    public LuminousFlashlights(IEventBus modEventBus, ModContainer modContainer) {
        Palette.enableDemoContent = false;
        Palette.registerMod(MODID, modEventBus);

        MOD_ITEMS.register(modEventBus);

        modEventBus.register(this);
    }

    @SubscribeEvent
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(MOD_ITEMS.getItem("flashlight"));
        }
    }
}
