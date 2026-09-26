package com.neoloxal.luminous_flashlights.render;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class FlashlightArmPoseParams {
    public static final EnumProxy<HumanoidModel.ArmPose> FLASHLIGHT_ARM_POSE_PROXY = new EnumProxy<>(
            HumanoidModel.ArmPose.class,
            false,
            (IArmPoseTransformer) FlashlightArmPoseParams::getFlashlightArmPoseParameter
    );

    public static void getFlashlightArmPoseParameter(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart armModel = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        float xRot = entity.getXRot();
        if (xRot > 0) {
            xRot *= 1;
            xRot += 180;
        }
        armModel.xRot = (float) Math.toRadians(xRot);
    }
}
