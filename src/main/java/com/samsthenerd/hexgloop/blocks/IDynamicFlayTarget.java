package com.samsthenerd.hexgloop.blocks;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.Villager;

public interface IDynamicFlayTarget {
    // politely ask you to not modify the sacrifice
    public void absorbVillagerMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx);
    
    // return true if it can be accepted
    public boolean canAcceptMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx);
}
