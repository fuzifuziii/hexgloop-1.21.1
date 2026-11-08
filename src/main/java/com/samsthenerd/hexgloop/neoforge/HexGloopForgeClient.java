package com.samsthenerd.hexgloop.neoforge;

import java.util.Map.Entry;
import java.util.function.Function;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.renderers.tooltips.HexGloopTooltips;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import com.samsthenerd.hexgloop.keybinds.HexGloopKeybinds;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class HexGloopForgeClient {
    public static void init(IEventBus modBus){
        modBus.addListener(HexGloopForgeClient::registerTooltipComponents);
        modBus.addListener(HexGloopForgeClient::registerKeyMappings);
    }

    // Key mappings must be registered from RegisterKeyMappingsEvent, which fires before
    // FMLClientSetupEvent. Registering them later (the old Architectury path from client setup)
    // only appended them to options.keyMappings after the event ("registered after event"),
    // so they did not reliably show up in Controls -> Key Binds.
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(HexGloopKeybinds.IOTA_WHEEL_KEYBIND);
        event.register(HexGloopKeybinds.CASTING_RING_KEY_BINDING);
        event.register(HexGloopKeybinds.HEX_SCROLL_UP);
        event.register(HexGloopKeybinds.HEX_SCROLL_DOWN);
        event.register(HexGloopKeybinds.OPEN_HEX_BOOK);
    }

    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent evt) {
        HexGloop.logPrint("registering tooltip components");
        HexGloopTooltips.init();
        for(Entry<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>> entry : HexGloopTooltips.tooltipDataToComponent.entrySet()){
            evt.register(entry.getKey(), entry.getValue());
        }
    }
}
