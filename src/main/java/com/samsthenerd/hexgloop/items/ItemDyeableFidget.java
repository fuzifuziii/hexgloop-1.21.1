package com.samsthenerd.hexgloop.items;

// 1.21: dyeing is handled by DataComponents.DYED_COLOR + the minecraft:dyeable item tag (no DyeableLeatherItem)
public class ItemDyeableFidget extends ItemFidget {
    public ItemDyeableFidget(Properties settings, FidgetSettings fidgetSettings){
        super(settings, fidgetSettings);
    }
}
