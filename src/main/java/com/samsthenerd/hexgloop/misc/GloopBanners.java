package com.samsthenerd.hexgloop.misc;

import com.samsthenerd.hexgloop.HexGloop;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class GloopBanners {
    // 1.21: banner_pattern is a data-driven (synced) registry, patterns are no longer
    // registered in code. The old Registrar lookup crashed in <clinit> with
    // "Registry ResourceKey[minecraft:root / minecraft:banner_pattern] does not exist!".
    // hexgloop:hermes is now defined in data/hexgloop/banner_pattern/hermes.json
    // (asset_id hexgloop:hermes, translation_key block.minecraft.banner.hexgloop.hermes,
    // matching the existing block.minecraft.banner.hexgloop.hermes.<color> lang keys).
    public static final TagKey<BannerPattern> HERMES_PATTERN_ITEM_KEY = TagKey.create(Registries.BANNER_PATTERN, ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "pattern_item/hermes"));
}
