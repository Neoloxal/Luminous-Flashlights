package com.neoloxal.luminous_flashlights.hud;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FocusOverlay implements LayeredDraw.Layer {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "textures/gui/focus/background.png");

    public static int ticksLeft = 0;
    private static int lastTicksLeft = 0;

    private static int ageTicks = 0;
    private static int lastAgeTicks = 0;

    private static final int TEX_WIDTH = 13;
    private static final int TEX_HEIGHT = 105;

    private static final int BG_TEX_WIDTH = 13;
    private static final int BG_TEX_HEIGHT = 96;

    private static final int POINTER_TEX_WIDTH = 3;
    private static final int POINTER_TEX_HEIGHT = 3;

    private static final float ANIM_DURATION = 10f;

    private static Double savedFocus = 0.0;

    public static void clientTick() {
        lastTicksLeft = ticksLeft;
        lastAgeTicks = ageTicks;
        if (ticksLeft > 0) {
            ticksLeft--;
            ageTicks++;
        } else {
            ageTicks = 0;
            lastAgeTicks = 0;
        }
    }

    /** Duration is in ticks */
    public static void showOverlay(int duration, boolean override) {
        if (override) {
            ticksLeft = duration;
            lastTicksLeft = duration + 1;

            if (ageTicks == 0) {
                ageTicks = 1;
                lastAgeTicks = 0;
            }
        } else {
            if (ticksLeft == 0) {
                int safeDuration = Math.max(duration, (int) ANIM_DURATION + 15);

                ticksLeft = safeDuration;
                lastTicksLeft = safeDuration;
                ageTicks = 1;
                lastAgeTicks = 0;
            } else {
                ticksLeft = Math.max(ticksLeft, duration);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (minecraft.options.hideGui || player == null || ticksLeft <= 0) return;

        Item flashlight = LuminousFlashlights.MOD_ITEMS.getItem("flashlight").get();
        ItemStack stack = null;
        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack pStack = player.getItemInHand(interactionHand);
            if (pStack.is(flashlight)) {
                stack = pStack;
                break;
            }
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        float smoothAge = lastAgeTicks + (ageTicks - lastAgeTicks) * partialTick;

        float smoothTicksLeft = lastTicksLeft + (ticksLeft - lastTicksLeft) * partialTick;

        int xOffset = 0;
        int maxHiddenOffset = -TEX_WIDTH - 64;

        if (smoothAge < ANIM_DURATION) {
            float progress = (ANIM_DURATION - smoothAge) / ANIM_DURATION;
            xOffset = (int) (progress * maxHiddenOffset);
        } else if (smoothTicksLeft < ANIM_DURATION) {
            float progress = (ANIM_DURATION - smoothTicksLeft) / ANIM_DURATION;
            xOffset = (int) (progress * maxHiddenOffset);
        }

        int yOffset = (height / 2) - (BG_TEX_HEIGHT / 2);

        guiGraphics.blit(
                TEXTURE,
                xOffset, yOffset,
                0, 0,
                BG_TEX_WIDTH, BG_TEX_HEIGHT,
                TEX_WIDTH, TEX_HEIGHT
        );

        guiGraphics.blit(
                TEXTURE,
                xOffset + BG_TEX_WIDTH - (POINTER_TEX_WIDTH + 1), (height / 2) - (3 / 2),
                0, BG_TEX_HEIGHT,
                POINTER_TEX_WIDTH, POINTER_TEX_HEIGHT,
                TEX_WIDTH, TEX_HEIGHT
        );

        double focus = savedFocus;
        if (stack != null) {
            focus = stack.getOrDefault(ModDataComponents.FOCUS.get(), 0.0);
            savedFocus = focus;
        }

        int padding = 4;
        int amount = 12;

        drawLine(guiGraphics, getLineTypeForFocus(focus), xOffset, BG_TEX_HEIGHT / 2);
        guiGraphics.drawString(
                minecraft.font,
                String.valueOf(focus),
                xOffset + BG_TEX_WIDTH + 2, height/2 - minecraft.font.lineHeight/2,
                0xFFFFFF, false
        );
        for (int i = 1; i < amount; i++) {
            double newFocus = focus + 0.25 * i;
            drawLine(guiGraphics, getLineTypeForFocus(newFocus), xOffset, BG_TEX_HEIGHT / 2 - padding * i);
        }
        for (int i = -1; i > -amount; i--) {
            double newFocus = focus + 0.25 * i;
            drawLine(guiGraphics, getLineTypeForFocus(newFocus), xOffset, BG_TEX_HEIGHT / 2 - padding * i);
        }
    }

    private static LineType getLineTypeForFocus(double focus) {
        if (focus > ModDataComponents.maxFocus || focus < ModDataComponents.minFocus) {
            return LineType.NONE;
        }
        if (focus == 0) {
            return LineType.ZERO;
        }
        double fractional = Math.abs(focus % 1.0);
        if (fractional < 0.01 || fractional > 0.99) {
            return LineType.LARGE;
        } else if (Math.abs(fractional - 0.5) < 0.01) {
            return LineType.MEDIUM;
        }
        return LineType.SMALL;
    }

    private static void drawLine(GuiGraphics guiGraphics, LineType lineType, int xOffset, int localYOffset) {
        if (lineType.skip) return;

        int yOffset = (guiGraphics.guiHeight() / 2) - (BG_TEX_HEIGHT / 2) - (lineType.height / 2) + localYOffset;

        guiGraphics.blit(
                TEXTURE,
                xOffset, yOffset,
                lineType.ux, lineType.uy,
                lineType.width, lineType.height,
                TEX_WIDTH, TEX_HEIGHT
        );
    }

    private enum LineType {
        SMALL(0, 0, 2, 1),
        MEDIUM(0, 1, 4, 1),
        LARGE(0, 2, 6, 2),
        ZERO(0, 4, 8, 2),
        NONE(0, 0, 0, 0, true);

        final int ux;
        final int uy;
        final int width;
        final int height;
        final boolean skip;

        LineType(int ux, int uy, int width, int height, boolean skip) {
            this.ux = ux;
            this.uy = BG_TEX_HEIGHT + POINTER_TEX_HEIGHT + uy;
            this.width = width;
            this.height = height;
            this.skip = skip;
        }

        LineType(int ux, int uy, int width, int height) {
            this(ux, uy, width, height, false);
        }
    }
}
