package com.neoloxal.luminous_flashlights.datagen;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        super.buildRecipes(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, LuminousFlashlights.MOD_ITEMS.getItem("flashlight"))
                .pattern("IPI")
                .pattern("IRI")
                .pattern(" L ")
                .define('I', Items.IRON_INGOT).define('P', Items.GLASS_PANE)
                .define('R', Items.REDSTONE_LAMP).define('L', Items.LEVER)
                .unlockedBy("darkness", PlayerTrigger.TriggerInstance.located(
                        LocationPredicate.Builder.location().setLight(
                                LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.atMost(3))
                        )
                )).save(recipeOutput);
    }
}
