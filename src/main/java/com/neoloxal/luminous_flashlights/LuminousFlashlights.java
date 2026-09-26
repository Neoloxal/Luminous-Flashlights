package com.neoloxal.luminous_flashlights;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.datagen.ModBlockTagsProvider;
import com.neoloxal.luminous_flashlights.datagen.ModItemTagsProvider;
import com.neoloxal.luminous_flashlights.datagen.ModLangProvider;
import com.neoloxal.luminous_flashlights.datagen.ModRecipeProvider;
import com.neoloxal.luminous_flashlights.item.Flashlight;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.luminous_flashlights.item.ModItems;
import com.neoloxal.luminous_flashlights.packet.ScrollPayload;
import com.neoloxal.luminous_flashlights.sound.ModSounds;
import com.neoloxal.paint_palette_lib.Palette;
import com.neoloxal.paint_palette_lib.PaletteUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.lwjgl.glfw.GLFW;
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

        ModSounds.register(modEventBus);

        modEventBus.register(this);
        modEventBus.register(LuminousFlashlightsClient.class);

        PaletteUtils.Canvas.useOtherLanguageProvider(MODID, ModLangProvider::new);

        PaletteUtils.Canvas.createTagsGenerator(MODID, ModBlockTagsProvider::new, ModItemTagsProvider::new);
        PaletteUtils.Canvas.createRecipeGenerator(MODID, ModRecipeProvider::new);
    }

    @SubscribeEvent
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(flashlightOfColor(Color.GLASS));
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

    @SubscribeEvent
    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);

        registrar.playToServer(
                ScrollPayload.TYPE,
                ScrollPayload.CODEC,
                Flashlight::handleScroll
        );
    }

    private ItemStack flashlightOfColor(Color color) {
        ItemStack flashlight = MOD_ITEMS.getItem("flashlight").get().getDefaultInstance().copy();
        flashlight.set(ModDataComponents.COLOR.get(), color);
        return flashlight;
    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber
    public static class Keybinds {
        public static final KeyMapping FLASHLIGHT_FOCUS = new KeyMapping(
                "key.luminous_flashlights.flashlight_focus",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "key.categories.gameplay"
        );

        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            event.register(FLASHLIGHT_FOCUS);
        }
    }
}
