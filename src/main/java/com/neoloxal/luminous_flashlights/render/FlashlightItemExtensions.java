package com.neoloxal.luminous_flashlights.render;

import com.mojang.logging.LogUtils;
import com.neoloxal.paint_palette_lib.builtin.PaletteDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FlashlightItemExtensions implements IClientItemExtensions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private FlashlightItemRenderer bewlr;

    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (bewlr == null) {
            bewlr = new FlashlightItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()
            );

        }
        return bewlr;
    }

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        if (itemStack.getOrDefault(PaletteDataComponents.TOGGLE.get(), false)) {
            return FlashlightArmPoseParams.FLASHLIGHT_ARM_POSE_PROXY.getValue();
        }
        return HumanoidModel.ArmPose.ITEM;
    }
}
