package com.neoloxal.luminous_flashlights.datagen;

import com.neoloxal.luminous_flashlights.item.FlashlightItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture, CompletableFuture<TagLookup<Block>> tagLookupCompletableFuture, String s, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, providerCompletableFuture, tagLookupCompletableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(FlashlightItemTags.GLASS_PANES)
                .add(Items.GLASS_PANE)

                .add(Items.RED_STAINED_GLASS_PANE)
                .add(Items.ORANGE_STAINED_GLASS_PANE)
                .add(Items.YELLOW_STAINED_GLASS_PANE)
                .add(Items.LIME_STAINED_GLASS_PANE)
                .add(Items.GREEN_STAINED_GLASS_PANE)
                .add(Items.CYAN_STAINED_GLASS_PANE)
                .add(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                .add(Items.BLUE_STAINED_GLASS_PANE)
                .add(Items.PURPLE_STAINED_GLASS_PANE)
                .add(Items.MAGENTA_STAINED_GLASS_PANE)
                .add(Items.PINK_STAINED_GLASS_PANE)
                .add(Items.WHITE_STAINED_GLASS_PANE)
                .add(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                .add(Items.GRAY_STAINED_GLASS_PANE)
                .add(Items.BLACK_STAINED_GLASS_PANE)
                .add(Items.BROWN_STAINED_GLASS_PANE);
    }
}
