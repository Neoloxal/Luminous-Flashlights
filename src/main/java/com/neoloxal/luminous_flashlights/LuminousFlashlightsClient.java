package com.neoloxal.luminous_flashlights;

import com.neoloxal.luminous_flashlights.item.ModItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = LuminousFlashlights.MODID, value = Dist.CLIENT)
public class LuminousFlashlightsClient {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModItemProperties::register);
    }
}
