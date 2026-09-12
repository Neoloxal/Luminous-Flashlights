package com.neoloxal.luminous_flashlights.item;

import com.neoloxal.paint_palette_lib.builtin.LibDataComponents;
import com.neoloxal.paint_palette_lib.utils.DataComponentUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Flashlight extends Item {
    public Flashlight(Properties properties) {
        super(properties);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        stack.set(LibDataComponents.TOGGLE.get(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        DataComponentUtils.toggleStack(stack);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!isSelected) {
            stack.set(LibDataComponents.TOGGLE.get(), false);
        }
    }
}
