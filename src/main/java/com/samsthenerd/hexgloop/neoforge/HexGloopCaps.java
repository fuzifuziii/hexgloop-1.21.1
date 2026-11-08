package com.samsthenerd.hexgloop.neoforge;

import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;
import com.samsthenerd.hexgloop.items.ItemSimpleMediaProvider;

import at.petrak.hexcasting.forge.cap.HexCapabilities;
import com.samsthenerd.hexgloop.blockentities.IReallyHateForgeWhyWouldAnInventoryInterfaceNotBeAnInterfaceThatsWhatAnInterfaceIsFor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

// 1.21 NeoForge: AttachCapabilitiesEvent is gone, capabilities are registered per item / block entity type.
public class HexGloopCaps {

    public static void registerCaps(RegisterCapabilitiesEvent evt){
        for(ItemSimpleMediaProvider item : ItemSimpleMediaProvider.allSimpleMediaItems){
            evt.registerItem(HexCapabilities.Item.MEDIA, (stack, ctx) -> item.getProvider(stack), item);
        }

        // block entities that expose their inventory (slate chest, pedestals, ...)
        for(var beType : HexGloopBEs.inventoryBlockEntityTypes()){
            evt.registerBlockEntity(Capabilities.ItemHandler.BLOCK, beType,
                (be, side) -> be instanceof IReallyHateForgeWhyWouldAnInventoryInterfaceNotBeAnInterfaceThatsWhatAnInterfaceIsFor inv ? new InvWrapper(inv) : null);
        }
    }
}
