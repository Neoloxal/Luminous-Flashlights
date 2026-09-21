package com.neoloxal.luminous_flashlights.item;

import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import com.neoloxal.luminous_flashlights.hud.FocusOverlay;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.luminous_flashlights.packet.ScrollPayload;
import com.neoloxal.luminous_flashlights.sounds.ModSounds;
import com.neoloxal.paint_palette_lib.builtin.LibDataComponents;
import com.neoloxal.paint_palette_lib.utils.Vec3Utils;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.SpotLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@EventBusSubscriber
public class Flashlight extends Item {
    public Flashlight(Properties properties) {
        super(properties);
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Map<InteractionHand, LightRenderHandle<SpotLightData>>> ACTIVE_LIGHTS = new HashMap<>();

    private static final float DEFAULT_DISTANCE = 35;
    private static final float DEFAULT_SIZE = 0.5f;

    private static final List<Pair<Item, Color>> PANE_COLOR_MAP = List.of(
            new Pair<>(Items.GLASS_PANE, Color.GLASS),
            new Pair<>(Items.RED_STAINED_GLASS_PANE, Color.RED),
            new Pair<>(Items.ORANGE_STAINED_GLASS_PANE, Color.ORANGE),
            new Pair<>(Items.YELLOW_STAINED_GLASS_PANE, Color.YELLOW),
            new Pair<>(Items.LIME_STAINED_GLASS_PANE, Color.LIME),
            new Pair<>(Items.GREEN_STAINED_GLASS_PANE, Color.GREEN),
            new Pair<>(Items.CYAN_STAINED_GLASS_PANE, Color.CYAN),
            new Pair<>(Items.LIGHT_BLUE_STAINED_GLASS_PANE, Color.LIGHT_BLUE),
            new Pair<>(Items.BLUE_STAINED_GLASS_PANE, Color.BLUE),
            new Pair<>(Items.PURPLE_STAINED_GLASS_PANE, Color.PURPLE),
            new Pair<>(Items.MAGENTA_STAINED_GLASS_PANE, Color.MAGENTA),
            new Pair<>(Items.PINK_STAINED_GLASS_PANE, Color.PINK),
            new Pair<>(Items.WHITE_STAINED_GLASS_PANE, Color.WHITE),
            new Pair<>(Items.LIGHT_GRAY_STAINED_GLASS_PANE, Color.LIGHT_GRAY),
            new Pair<>(Items.GRAY_STAINED_GLASS_PANE, Color.GRAY),
            new Pair<>(Items.BLACK_STAINED_GLASS_PANE, Color.BLACK),
            new Pair<>(Items.BROWN_STAINED_GLASS_PANE, Color.BROWN)
    );
    private static final Map<Item, Color> PANE_MAP = PANE_COLOR_MAP.stream().collect(Collectors.toMap(Pair::getA, Pair::getB));
    private static final Map<Color, Item> COLOR_MAP = PANE_COLOR_MAP.stream().collect(Collectors.toMap(Pair::getB, Pair::getA));

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        //stack.set(LibDataComponents.TOGGLE.get(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            boolean currentState = stack.getOrDefault(LibDataComponents.TOGGLE.get(), false);
            boolean newState = !currentState;
            stack.set(LibDataComponents.TOGGLE.get(), newState);

            if (newState) {
                toggleOn(level, player);
            } else {
                toggleOff(level, player);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    public static void toggleOn(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FLASHLIGHT_ON.get(), SoundSource.PLAYERS);
    }

    public static void toggleOff(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FLASHLIGHT_OFF.get(), SoundSource.PLAYERS);
    }

    private void addLightData(ItemStack stack, InteractionHand interactionHand, UUID pUUID) {
        SpotLightData lightData = new SpotLightData();
        Color color = stack.getOrDefault(ModDataComponents.COLOR.get(), Color.NULL);
        lightData.setColor(color.getHexColor());
        lightData.setBrightness(color.getBrightness());
        lightData.setDistance(DEFAULT_DISTANCE);
        lightData.setSize(DEFAULT_SIZE);
        lightData.setOcclusionEnabled(true);
        lightData.setInscatteringStrength(2.5f);

        LightRenderHandle<SpotLightData> lightRenderHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(lightData);

        if (!ACTIVE_LIGHTS.containsKey(pUUID)) {
            ACTIVE_LIGHTS.put(pUUID, new HashMap<>());
            ACTIVE_LIGHTS.get(pUUID).put(interactionHand, lightRenderHandle);
            return;
        }

        if (ACTIVE_LIGHTS.get(pUUID).containsKey(interactionHand)) {
            ACTIVE_LIGHTS.get(pUUID).get(interactionHand).free();
            ACTIVE_LIGHTS.get(pUUID).remove(interactionHand);
        }

        ACTIVE_LIGHTS.get(pUUID).put(interactionHand, lightRenderHandle);
    }

    public static void turnOffLight(UUID pUUID, InteractionHand interactionHand) {
        if (ACTIVE_LIGHTS.get(pUUID) != null) {
            if (ACTIVE_LIGHTS.get(pUUID).get(interactionHand) != null) {
                ACTIVE_LIGHTS.get(pUUID).get(interactionHand).free();
                ACTIVE_LIGHTS.get(pUUID).remove(interactionHand);
                if (ACTIVE_LIGHTS.get(pUUID).isEmpty()) {
                    ACTIVE_LIGHTS.remove(pUUID);
                }
            }
        }
    }

    public static void turnOffLights(UUID pUUID) {
        turnOffLight(pUUID, InteractionHand.MAIN_HAND);
        turnOffLight(pUUID, InteractionHand.OFF_HAND);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player) {
            boolean heldInMain = player.getMainHandItem() == stack;
            boolean heldInOff = player.getOffhandItem() == stack;
            boolean selected = heldInMain || heldInOff;
            InteractionHand interactionHand = heldInMain ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

            if (!selected && stack.getOrDefault(LibDataComponents.TOGGLE.get(), false)) {
                if (level.isClientSide()) {
                    turnOffLight(player.getUUID(), interactionHand);
                } else {
                    stack.set(LibDataComponents.TOGGLE.get(), false);
                    toggleOff(level, player);
                }
            }
        }
    }

    public void updateLights(Player player, float partialTicks, ItemStack stack, boolean toggleState, InteractionHand interactionHand) {
        UUID pUUID = player.getUUID();
        if (!ACTIVE_LIGHTS.containsKey(pUUID) || !ACTIVE_LIGHTS.get(pUUID).containsKey(interactionHand)) {
            if (toggleState) {
                addLightData(stack, interactionHand, pUUID);
            }
        }
        if (!toggleState) {
            turnOffLight(pUUID, interactionHand);
        }

        if (toggleState && ACTIVE_LIGHTS.get(pUUID).get(interactionHand) != null) {
            SpotLightData lightData = ACTIVE_LIGHTS.get(pUUID).get(interactionHand).getLightData();

            Vector3f playerRotation = Vec3Utils.toVector3f(player.getViewVector(partialTicks));
            Quaternionf orientation = new Quaternionf();
            playerRotation.negate();
            orientation.lookAlong(playerRotation, new Vector3f(0, 1, 0));
            lightData.getOrientationMutable().set(orientation);

            boolean isFirstPersonAndLocal = player == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson();

            int direction = interactionHand.equals(InteractionHand.MAIN_HAND) ? 1 : -1;
            direction *= player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            Vec3 localOffset = new Vec3(-0.4 * direction, -0.7, 0.8);
            float yRot = player.yBodyRot;
            if (isFirstPersonAndLocal) {
                localOffset = new Vec3(-1 * direction, -0.7, 0.9);
                yRot = player.getViewYRot(partialTicks);
            }

            Vec3 worldOffset = localOffset.xRot((float) Math.toRadians(-player.getViewXRot(partialTicks))).yRot((float) Math.toRadians(-yRot));
            Vec3 position = player.getEyePosition(partialTicks).add(worldOffset);
            lightData.getPositionMutable().set(Vec3Utils.toVector3d(position));

            double focus = stack.getOrDefault(ModDataComponents.FOCUS.get(), 0.0);
            float baseBrightness = stack.getOrDefault(ModDataComponents.COLOR.get(), Color.GLASS).getBrightness();
            lightData.setDistance((float) (DEFAULT_DISTANCE + Math.floor(focus) / 1.25f));

            float sizeAtMin = 1f;
            float sizeAtMax = 0.5f;
            double sizeMarker = (focus - ModDataComponents.minFocus)
                    / (ModDataComponents.maxFocus - ModDataComponents.minFocus);
            sizeMarker = Math.max(0.0, Math.min(1.0, sizeMarker));

            lightData.setSize((float) (sizeAtMin + (sizeAtMax - sizeAtMin) * sizeMarker));

            float brightnessAtMin = -3f;
            float brightnessAtMax = 2f;
            double brightnessMarker = (focus - ModDataComponents.minFocus)
                    / (ModDataComponents.maxFocus - ModDataComponents.minFocus);
            brightnessMarker = Math.max(0.0, Math.min(1.0, brightnessMarker));

            lightData.setBrightness((float) Math.max(0.25f, (baseBrightness + (brightnessAtMin + (brightnessAtMax - brightnessAtMin) * brightnessMarker*brightnessMarker))));
            if (stack.getOrDefault(ModDataComponents.COLOR.get(), Color.GLASS) == Color.BLACK) {
                lightData.setBrightness(-lightData.getBrightness());
            }
        }
    }

    @SubscribeEvent
    public static void onCameraMove(ViewportEvent.ComputeCameraAngles event) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Level level = Minecraft.getInstance().level;

        assert level != null;
        assert connection != null;
        Collection<PlayerInfo> playerList = connection.getListedOnlinePlayers();
        List<Player> players = new ArrayList<>();
        playerList.forEach(playerInfo ->
            players.add(level.getPlayerByUUID(playerInfo.getProfile().getId())));

        Set<UUID> unverifiedPlayers = new HashSet<>(ACTIVE_LIGHTS.keySet());

        for (Player player : players) {
            if (player == null) continue;
            unverifiedPlayers.remove(player.getUUID());

            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                Item flashlight = LuminousFlashlights.MOD_ITEMS.getItem("flashlight").get();
                if (level.isClientSide()) {
                    if (stack.is(flashlight)) {
                        ((Flashlight) stack.getItem()).updateLights(player, (float) event.getPartialTick(), stack, stack.getOrDefault(LibDataComponents.TOGGLE.get(), false), hand);
                    } else {
                        turnOffLight(player.getUUID(), hand);
                    }
                }
            }
        }

        for (UUID unfoundPlayer : unverifiedPlayers) {
            turnOffLights(unfoundPlayer);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity entity = event.getEntity();
        ItemStack stack = entity.getItem();
        Player player = event.getPlayer();
        Level level = player.level();

        if (stack.is(LuminousFlashlights.MOD_ITEMS.getItem("flashlight"))) {
            stack.set(LibDataComponents.TOGGLE.get(), false);
            if (!level.isClientSide()) {
                toggleOff(level, player);
            }
            ((Flashlight) stack.getItem()).turnOffLights(player.getUUID());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        Color color = stack.getOrDefault(ModDataComponents.COLOR.get(), Color.GLASS);
        java.awt.Color textColor = java.awt.Color.decode(String.valueOf(color.getHexColor()));
        float multiplier = color.getChatBrightness() / 4.0f;

        int r = Math.min(255, Math.max(0, (int) (textColor.getRed() * multiplier)));
        int g = Math.min(255, Math.max(0, (int) (textColor.getGreen() * multiplier)));
        int b = Math.min(255, Math.max(0, (int) (textColor.getBlue() * multiplier)));

        int rgbCombined = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        TextColor minecraftColor = TextColor.fromRgb(rgbCombined);

        tooltipComponents.add(
                Component.translatable("item.luminous_flashlights.flashlight.tooltip.%s".formatted(color.getSerializedName()))
                        .setStyle(Style.EMPTY.withColor(minecraftColor)
                        ));
        tooltipComponents.add(Component.translatable("item.luminous_flashlights.flashlight.tooltip").setStyle(
                Style.EMPTY.withColor(ChatFormatting.GRAY)
        ));
    }

    @SubscribeEvent
    public static void onItemSwap(LivingSwapItemsEvent.Hands event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack offStack = event.getItemSwappedToMainHand();
            if (offStack.is(LuminousFlashlights.MOD_ITEMS.getItem("flashlight"))) {
                ItemStack mainStack = event.getItemSwappedToOffHand();
                if (mainStack.is(FlashlightItemTags.GLASS_PANES) && mainStack.getCount() == 1) {
                    Color oldColor = offStack.getOrDefault(ModDataComponents.COLOR.get(), Color.GLASS);
                    Color newColor = PANE_MAP.get(mainStack.getItem());

                    event.setItemSwappedToMainHand(new ItemStack(COLOR_MAP.get(oldColor), 1));

                    ItemStack flashlight = offStack.copy();
                    flashlight.applyComponents(offStack.getComponents());
                    flashlight.set(LibDataComponents.TOGGLE.get(), false);
                    flashlight.set(ModDataComponents.COLOR.get(), newColor);
                    event.setItemSwappedToOffHand(flashlight);

                    Level level = player.level();
                    if (!level.isClientSide()) {
                        //player.playNotifySound(ModSounds.SWAP_LENS.get(), SoundSource.PLAYERS, 1f, 1f);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SWAP_LENS.get(), SoundSource.PLAYERS);
                    }
                }
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        ItemStack oldStackNoToggleNoFocus = oldStack.copy();
        oldStackNoToggleNoFocus.remove(LibDataComponents.TOGGLE.get());
        oldStackNoToggleNoFocus.remove(ModDataComponents.FOCUS.get());
        ItemStack newStackNoToggleNoFocus = newStack.copy();
        newStackNoToggleNoFocus.remove(LibDataComponents.TOGGLE.get());
        newStackNoToggleNoFocus.remove(ModDataComponents.FOCUS.get());

        if (ItemStack.matches(oldStackNoToggleNoFocus, newStackNoToggleNoFocus) && !slotChanged) {
            return false;
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FocusOverlay.showOverlay(40, true);
        }
        return true;
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (LuminousFlashlights.Keybinds.FLASHLIGHT_FOCUS.isDown() && event.getScrollDeltaY() != 0) {
            Player player = Minecraft.getInstance().player;
            if (player != null &&
                    player.getMainHandItem().is(LuminousFlashlights.MOD_ITEMS.getItem("flashlight"))) {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection != null) {
                    connection.send(new ScrollPayload(event.getScrollDeltaY()/0.5));
                }
                event.setCanceled(true);
            }
        }
    }

    public static void handleScroll(ScrollPayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();

        if (stack.is(LuminousFlashlights.MOD_ITEMS.getItem("flashlight"))) {
            double focusOffset = ModDataComponents.Helper.scrollStack(stack, payload.scrollDelta());
            double newFocus = stack.getOrDefault(ModDataComponents.FOCUS.get(), 0.0);
            //float volume = newFocus % 1 == 0 ? 1f : 0.5f;
            if (newFocus % 1 == 0 && Math.abs(focusOffset) > 0) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FOCUS_CHANGE.get(), SoundSource.PLAYERS, 1f, 1f);
            }
        }
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        FocusOverlay.clientTick();
        if (Minecraft.getInstance().player != null) {
            if (LuminousFlashlights.Keybinds.FLASHLIGHT_FOCUS.isDown()) {
                FocusOverlay.showOverlay(10, false);
            }
        }
    }
}
