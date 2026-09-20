package com.neoloxal.luminous_flashlights.item;

import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
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
    private static final Map<UUID, LightRenderHandle<SpotLightData>> ACTIVE_LIGHTS = new HashMap<>();

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
    private static final Map<Item, Color> PANE_MAP = PANE_COLOR_MAP.stream().collect(Collectors.toMap(Pair::getA, Pair::getB));;
    private static final Map<Color, Item> COLOR_MAP = PANE_COLOR_MAP.stream().collect(Collectors.toMap(Pair::getB, Pair::getA));;

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

        return InteractionResultHolder.consume(stack);
    }

    public static void toggleOn(Level level, Player player) {
        LOGGER.debug("toggle on");
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FLASHLIGHT_ON.get(), SoundSource.PLAYERS);
    }

    public static void toggleOff(Level level, Player player) {
        LOGGER.debug("toggle off");
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FLASHLIGHT_OFF.get(), SoundSource.PLAYERS);
    }

    private void addLightData(ItemStack stack, Player player) {
        SpotLightData lightData = new SpotLightData();
        Color color = stack.getOrDefault(ModDataComponents.COLOR.get(), Color.NULL);
        lightData.setColor(color.getHexColor());
        lightData.setBrightness(color.getBrightness());
        lightData.setDistance(50);
        lightData.setSize(0.75f);
        lightData.setOcclusionEnabled(true);
        lightData.setInscatteringStrength(2.5f);

        if (ACTIVE_LIGHTS.containsKey(player.getUUID())) {
            ACTIVE_LIGHTS.get(player.getUUID()).free();
            ACTIVE_LIGHTS.remove(player.getUUID());
        }

        ACTIVE_LIGHTS.put(player.getUUID(), VeilRenderSystem.renderer().getLightRenderer().addLight(lightData));
    }

    public void turnOffLight(Player player) {
        if (ACTIVE_LIGHTS.get(player.getUUID()) != null) {
            ACTIVE_LIGHTS.get(player.getUUID()).free();
            ACTIVE_LIGHTS.remove(player.getUUID());
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (entity instanceof Player player) {
            boolean selected = isSelected || player.getOffhandItem().is(stack.getItem());

            if (!selected && stack.getOrDefault(LibDataComponents.TOGGLE.get(), false)) {
                if (level.isClientSide()) {
                    turnOffLight(player);
                } else {
                    stack.set(LibDataComponents.TOGGLE.get(), false);
                    toggleOff(level, player);
                }
            }
        }
    }

    public void updateLight(Player player, float partialTicks, ItemStack stack, InteractionHand interactionHand) {
        boolean toggleState = stack.getOrDefault(LibDataComponents.TOGGLE.get(), false);
        if (!ACTIVE_LIGHTS.containsKey(player.getUUID())) {
            if (toggleState) {
                addLightData(stack, player);
            }
        }
        if (!toggleState) {
            turnOffLight(player);
        }

        if (toggleState){
            SpotLightData lightData = ACTIVE_LIGHTS.get(player.getUUID()).getLightData();

            Vector3f playerRotation = Vec3Utils.toVector3f(player.getViewVector(partialTicks));
            Quaternionf orientation = new Quaternionf();
            playerRotation.negate();
            orientation.lookAlong(playerRotation, new Vector3f(0, 1, 0));
            lightData.getOrientationMutable().set(orientation);

            boolean isFirstPersonAndLocal = player == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson();

            int direction = interactionHand.equals(InteractionHand.MAIN_HAND) ? 1 : -1;
            Vec3 localOffset = new Vec3(-0.4 * direction, -0.7, 0.8);
            float yRot = player.yBodyRot;
            if (isFirstPersonAndLocal) {
                localOffset = new Vec3(-0.9 * direction, -0.6, 0.9);
                yRot = player.getViewYRot(partialTicks);
            }

            Vec3 worldOffset = localOffset.xRot((float) Math.toRadians(-player.getViewXRot(partialTicks))).yRot((float) Math.toRadians(-yRot));
            Vec3 position = player.getEyePosition(partialTicks).add(worldOffset);
            lightData.getPositionMutable().set(Vec3Utils.toVector3d(position));
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

        for (Player player : players) {
            if (player == null) continue;

            ItemStack stack = player.getMainHandItem();
            InteractionHand interactionHand = InteractionHand.MAIN_HAND;
            Item flashlight = LuminousFlashlights.MOD_ITEMS.getItem("flashlight").get();
            if (!stack.is(flashlight)) {
                stack = player.getOffhandItem();
                interactionHand = InteractionHand.OFF_HAND;
                if (!stack.is(flashlight)) {
                    continue;
                }
            }

            if (level.isClientSide()) {
                ((Flashlight) stack.getItem()).updateLight(player, (float) event.getPartialTick(), stack, interactionHand);
            }
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
            ((Flashlight) stack.getItem()).turnOffLight(player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        Color color = stack.getOrDefault(ModDataComponents.COLOR.get(), Color.GLASS);
        tooltipComponents.add(
                Component.translatable("item.luminous_flashlights.flashlight.tooltip.%s".formatted(color.getSerializedName()))
                        .setStyle(Style.EMPTY.withColor(color.getHexColor())));
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
}
