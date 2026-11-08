package com.samsthenerd.hexgloop.blocks;

import com.samsthenerd.hexgloop.HexGloop;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Block;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

public class BlockTestFlayTarget extends Block implements IDynamicFlayTarget {
    public BlockTestFlayTarget(Properties settings) {
        super(settings);
    }

    @Override
    public void absorbVillagerMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx){
        HexGloop.logPrint("absorbed villager mind !");
    }
    
    @Override
    public boolean canAcceptMind(Villager sacrifice, BlockPos flayPos, CastingEnvironment ctx){
        HexGloop.logPrint("can accept villager mind !");
        return true;
    }
}
