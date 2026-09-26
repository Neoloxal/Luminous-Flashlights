package com.neoloxal.luminous_flashlights;

import com.neoloxal.luminous_flashlights.hud.FocusOverlay;
import com.neoloxal.luminous_flashlights.item.ModItemProperties;
import com.neoloxal.luminous_flashlights.render.FlashlightItemExtensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Consumer;

@EventBusSubscriber(modid = LuminousFlashlights.MODID, value = Dist.CLIENT)
@Mod(value = LuminousFlashlights.MODID, dist = Dist.CLIENT)
public class LuminousFlashlightsClient {
    public LuminousFlashlightsClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModItemProperties::register);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
                VanillaGuiLayers.CHAT,
                ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "flashlight_focus"),
                new FocusOverlay()
        );
    }

    @SubscribeEvent
    public static void registerItemExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new FlashlightItemExtensions(), LuminousFlashlights.MOD_ITEMS.getItem("flashlight"));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "item/flashlight_overrides")
        ));
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "item/flashlight_base")
        ));
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "item/flashlight")
        ));
    }
}
