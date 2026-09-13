package com.neoloxal.luminous_flashlights;

import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.item.Flashlight;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.luminous_flashlights.item.ModItems;
import com.neoloxal.paint_palette_lib.Palette;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(LuminousFlashlights.MODID)
public class LuminousFlashlights {
    public static final String MODID = "luminous_flashlights";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ModItems MOD_ITEMS = new ModItems();

    public LuminousFlashlights(IEventBus modEventBus, ModContainer modContainer) {
        Palette.enableDemoContent = false;
        Palette.registerMod(MODID, modEventBus);

        ModDataComponents.register(modEventBus);
        MOD_ITEMS.register(modEventBus);

        modEventBus.register(this);
        modEventBus.register(LuminousFlashlightsClient.class);
    }

    @SubscribeEvent
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(flashlightOfColor(Color.WHITE));
            event.accept(flashlightOfColor(Color.RED));
        }
    }

    private ItemStack flashlightOfColor(Color color) {
        ItemStack flashlight = MOD_ITEMS.getItem("flashlight").get().getDefaultInstance().copy();
        flashlight.set(ModDataComponents.COLOR.get(), color);
        return flashlight;
    }
}
