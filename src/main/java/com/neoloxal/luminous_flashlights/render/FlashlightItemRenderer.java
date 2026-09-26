package com.neoloxal.luminous_flashlights.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.paint_palette_lib.builtin.PaletteDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.UUID;

public class FlashlightItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation FLASHLIGHT_MODEL =
            ResourceLocation.fromNamespaceAndPath("luminous_flashlights", "item/flashlight_overrides");

    public FlashlightItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        UUID pUUID = stack.get(ModDataComponents.HOLDER.get());
        Player player = pUUID != null && Minecraft.getInstance().level != null ?
                Minecraft.getInstance().level.getPlayerByUUID(pUUID) : null;

        BakedModel baseModel = Minecraft.getInstance().getModelManager()
                .getModel(ModelResourceLocation.standalone(FLASHLIGHT_MODEL));

        BakedModel flashlightModel = baseModel.getOverrides().resolve(
                baseModel,
                stack,
                Minecraft.getInstance().level,
                null,
                0
        );

        if (flashlightModel == null) {
            flashlightModel = baseModel;
        }

        boolean isLeftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        poseStack.pushPose();

        if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            if (stack.getOrDefault(PaletteDataComponents.TOGGLE.get(), false)) {
                if (player != null) {
                    if (player.getXRot() > 0) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(180));
                        poseStack.translate(0.25f, 0f, -1f);
                    } else {
                        poseStack.translate(0.25f, 0.75f, 0.25f);
                    }
                }
            } else {
                poseStack.translate(0.25f, 0.75f, 0.25f);
            }
        } else if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(1.35f, 0.45f, 0.75f);
        } else if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.25, 0.1f, 0.25f);
        } else if (displayContext == ItemDisplayContext.FIXED) {
            poseStack.translate(0f, 0.15f, 1f);
        } else if (displayContext == ItemDisplayContext.HEAD) {
            poseStack.translate(1f, 0f, 1f);
        }

        flashlightModel.applyTransform(displayContext, poseStack, isLeftHand);

        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                buffer,
                RenderType.CUTOUT,
                true,
                stack.hasFoil()
        );

        Minecraft.getInstance().getItemRenderer().renderModelLists(
                flashlightModel,
                stack,
                packedLight,
                packedOverlay,
                poseStack,
                vertexConsumer
        );

        poseStack.popPose();
    }
}
