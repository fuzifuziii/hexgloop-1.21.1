package com.samsthenerd.hexgloop.renderers.tooltips;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import com.samsthenerd.hexgloop.items.tooltips.MirrorTooltipData;
import com.samsthenerd.hexgloop.items.tooltips.ScriptTooltipData;

public class HexGloopTooltips {

    public static final Map<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>> tooltipDataToComponent = new HashMap<>();

    public static ClientTooltipComponent getTooltipComponent(TooltipComponent data){
        Function<TooltipComponent, ClientTooltipComponent> ttFunc = tooltipDataToComponent.get(data.getClass());
        return ttFunc == null ? null : ttFunc.apply(data);
    }

    public static <C extends ClientTooltipComponent, D extends TooltipComponent> Function<TooltipComponent, C> convertTooltip(Class<D> dataClass, 
        Function<D, C> componentFactory){
        return (data) -> {
            if(dataClass.isInstance(data)){
                return componentFactory.apply(dataClass.cast(data));
            }
            return null;
        };
    }

    // should be called sided in tooltip registration stuff
    public static void init(){
        tooltipDataToComponent.put(MirrorTooltipData.class, convertTooltip(MirrorTooltipData.class, MirrorTooltipComponent::new));
        tooltipDataToComponent.put(ScriptTooltipData.class, convertTooltip(ScriptTooltipData.class, ScriptTooltipComponent::new));
    }
}
