package com.samsthenerd.hexgloop.neoforge;

import java.util.Map.Entry;
import java.util.function.Function;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.renderers.tooltips.HexGloopTooltips;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public class HexGloopForgeClient {
    public static void init(IEventBus modBus){
        modBus.addListener(HexGloopForgeClient::registerTooltipComponents);
    }

    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent evt) {
        HexGloop.logPrint("registering tooltip components");
        HexGloopTooltips.init();
        for(Entry<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>> entry : HexGloopTooltips.tooltipDataToComponent.entrySet()){
            evt.register(entry.getKey(), entry.getValue());
        }
    }
}
