package com.samsthenerd.hexgloop.items.tooltips;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public class ScriptTooltipData implements TooltipComponent{
    public List<HexPattern> patterns;
    public ResourceLocation background;

    public ScriptTooltipData(List<HexPattern> patterns, ResourceLocation background){
        this.patterns = patterns;
        this.background = background;
    }
}
