package com.neoloxal.luminous_flashlights.item;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class FlashlightItemTags {
    public static final TagKey<Item> GLASS_PANES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "glass_panes")
    );
}
