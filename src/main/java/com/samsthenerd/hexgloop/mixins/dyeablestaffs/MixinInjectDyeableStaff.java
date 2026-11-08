package com.samsthenerd.hexgloop.mixins.dyeablestaffs;

import org.spongepowered.asm.mixin.Mixin;

import com.samsthenerd.hexgloop.misc.StaffColorLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import at.petrak.hexcasting.common.items.ItemStaff;

// 1.21: DyeableLeatherItem is gone; dye colour lives in DataComponents.DYED_COLOR.
// The old fallback chain is kept: dyed colour -> StaffColorLoader map -> default pink.
@Mixin(ItemStaff.class)
public class MixinInjectDyeableStaff {

    public int getColor(ItemStack stack) {
        return StaffColorLoader.getStaffColor(stack);
    }
}
