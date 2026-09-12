package com.neoloxal.luminous_flashlights.item;

import com.neoloxal.paint_palette_lib.utils.DataComponentUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Flashlight extends Item {
    public Flashlight(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        DataComponentUtils.toggleStack(stack);

        return InteractionResultHolder.success(stack);
    }
}
