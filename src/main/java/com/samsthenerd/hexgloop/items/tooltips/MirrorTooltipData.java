package com.samsthenerd.hexgloop.items.tooltips;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record MirrorTooltipData(ItemStack storedItem) implements TooltipComponent{
    
}
