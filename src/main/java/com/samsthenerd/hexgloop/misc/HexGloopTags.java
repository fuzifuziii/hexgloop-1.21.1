package com.samsthenerd.hexgloop.misc;

import com.samsthenerd.hexgloop.HexGloop;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class HexGloopTags {
    // for if it shouldn't pass through to the circle
    public static final TagKey<Item> NOT_PATTERN_PEDESTAL_PROVIDER = TagKey.create(net.minecraft.core.registries.Registries.ITEM, 
        ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "pedestal_pattern_item_blacklist"));

    public static final TagKey<Item> FORGE_LEATHERY = TagKey.create(net.minecraft.core.registries.Registries.ITEM, 
        ResourceLocation.fromNamespaceAndPath("forge", "leather"));

    public static final TagKey<Item> FABRIC_LEATHERY = TagKey.create(net.minecraft.core.registries.Registries.ITEM, 
        ResourceLocation.fromNamespaceAndPath("c", "leather"));

    public static final TagKey<Item> DONT_USE_AS_PASSTHROUGH = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "no_passthrough"));
}
