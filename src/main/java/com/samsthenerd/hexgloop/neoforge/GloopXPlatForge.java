package com.samsthenerd.hexgloop.neoforge;

import com.samsthenerd.hexgloop.utils.GloopXPlat;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class GloopXPlatForge implements GloopXPlat {
    public SpawnEggItem getForgeSpawnEggForEntity(EntityType<?> type){
        SpawnEggItem egg = SpawnEggItem.byId(type);
        return egg != null ? egg : DeferredSpawnEggItem.deferredOnlyById(type);
    }
}
