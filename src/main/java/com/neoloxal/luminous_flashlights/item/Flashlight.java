package com.neoloxal.luminous_flashlights.item;

import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.paint_palette_lib.builtin.LibDataComponents;
import com.neoloxal.paint_palette_lib.utils.DataComponentUtils;
import com.neoloxal.paint_palette_lib.utils.Vec3Utils;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.SpotLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.*;
import java.util.List;

@EventBusSubscriber
public class Flashlight extends Item {
    public Flashlight(Properties properties) {
        super(properties);
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, LightRenderHandle<SpotLightData>> ACTIVE_LIGHTS = new HashMap<>();

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        //stack.set(LibDataComponents.TOGGLE.get(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            stack.set(LibDataComponents.TOGGLE.get(), !stack.getOrDefault(LibDataComponents.TOGGLE.get(), false));
        }

        return InteractionResultHolder.success(stack);
    }

    public void toggleOn(ItemStack stack, Player player) {
        LOGGER.debug("Toggle on!");
    }

    private void addLightData(ItemStack stack, Player player) {
        SpotLightData lightData = new SpotLightData();
        lightData.setColor(
                stack.getOrDefault(ModDataComponents.COLOR.get(), Color.WHITE).getHexColor()
        );
        lightData.setDistance(50);
        lightData.setSize(0.5f);
        lightData.setOcclusionEnabled(true);

        if (ACTIVE_LIGHTS.containsKey(player.getUUID())) {
            ACTIVE_LIGHTS.get(player.getUUID()).free();
            ACTIVE_LIGHTS.remove(player.getUUID());
        }

        ACTIVE_LIGHTS.put(player.getUUID(), VeilRenderSystem.renderer().getLightRenderer().addLight(lightData));
    }

    public void toggleOff(Player player) {
        LOGGER.debug("Toggle off!");
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
        if (!isSelected && stack.getOrDefault(LibDataComponents.TOGGLE.get(), false)) {
            if (entity instanceof Player player) {
                stack.set(LibDataComponents.TOGGLE.get(), false);
                turnOffLight(player);
                toggleOff(player);
            }
        }
    }

    public void updateLight(Player player, float partialTicks, ItemStack stack) {
        boolean toggleState = stack.getOrDefault(LibDataComponents.TOGGLE.get(), false);
        if (!ACTIVE_LIGHTS.containsKey(player.getUUID())) {
            if (toggleState) {
                addLightData(stack, player);
                toggleOn(stack, player);
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

            Vec3 position = player.getEyePosition(partialTicks);
            lightData.getPositionMutable().set(Vec3Utils.toVector3d(position));
        }
    }

    @SubscribeEvent
    public static void onCameraMove(ViewportEvent.ComputeCameraAngles event) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Level level = Minecraft.getInstance().level;

        assert level != null;
        if (level.isClientSide()) {
            assert connection != null;
            Collection<PlayerInfo> playerList = connection.getListedOnlinePlayers();
            List<Player> players = new ArrayList<>();
            playerList.forEach(playerInfo ->
                players.add(level.getPlayerByUUID(playerInfo.getProfile().getId())));

            for (Player player : players) {
                if (player == null) continue;

                ItemStack stack = player.getMainHandItem();
                Item flashlight = LuminousFlashlights.MOD_ITEMS.getItem("flashlight").get();
                if (!stack.is(flashlight)) {
                    stack = player.getOffhandItem();
                    if (!stack.is(flashlight)) {
                        continue;
                    }
                }
                ((Flashlight) stack.getItem()).updateLight(player, (float) event.getPartialTick(), stack);
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity entity = event.getEntity();
        ItemStack stack = entity.getItem();
        Player player = event.getPlayer();

        if (stack.is(LuminousFlashlights.MOD_ITEMS.getItem("flashlight"))) {
            stack.set(LibDataComponents.TOGGLE.get(), false);
            ((Flashlight) stack.getItem()).turnOffLight(player);
        }
    }
}
