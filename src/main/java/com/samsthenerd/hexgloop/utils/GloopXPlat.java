package com.samsthenerd.hexgloop.utils;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

public interface GloopXPlat {
    // i hate forge
    public SpawnEggItem getForgeSpawnEggForEntity(EntityType<?> type);
}
