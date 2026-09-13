package com.neoloxal.luminous_flashlights;

import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.item.Flashlight;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.luminous_flashlights.item.ModItems;
import com.neoloxal.paint_palette_lib.Palette;
import com.neoloxal.paint_palette_lib.datagen.LibDataGenerators;
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
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;

import java.util.Arrays;

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

        LibDataGenerators.LibLangProvider.EXTERNAL_TRANSLATORS.add((modId, translationRegistry) ->
            Arrays.stream(Color.values()).iterator().forEachRemaining(
                color -> translationRegistry.accept(
                    "item.luminous_flashlights.flashlight.tooltip.%s".formatted(color.getSerializedName()),
                    WordUtils.capitalizeFully(color.getSerializedName().replace('_', ' '))
                )
            )
        );
    }

    @SubscribeEvent
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(flashlightOfColor(Color.RED));
            event.accept(flashlightOfColor(Color.ORANGE));
            event.accept(flashlightOfColor(Color.YELLOW));
            event.accept(flashlightOfColor(Color.LIME));
            event.accept(flashlightOfColor(Color.GREEN));
            event.accept(flashlightOfColor(Color.CYAN));
            event.accept(flashlightOfColor(Color.LIGHT_BLUE));
            event.accept(flashlightOfColor(Color.BLUE));
            event.accept(flashlightOfColor(Color.PURPLE));
            event.accept(flashlightOfColor(Color.MAGENTA));
            event.accept(flashlightOfColor(Color.PINK));
            event.accept(flashlightOfColor(Color.WHITE));
            event.accept(flashlightOfColor(Color.LIGHT_GRAY));
            event.accept(flashlightOfColor(Color.GRAY));
            event.accept(flashlightOfColor(Color.BLACK));
            event.accept(flashlightOfColor(Color.BROWN));
        }
    }

    private ItemStack flashlightOfColor(Color color) {
        ItemStack flashlight = MOD_ITEMS.getItem("flashlight").get().getDefaultInstance().copy();
        flashlight.set(ModDataComponents.COLOR.get(), color);
        return flashlight;
    }
}
