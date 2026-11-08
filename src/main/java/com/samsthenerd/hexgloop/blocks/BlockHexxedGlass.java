package com.samsthenerd.hexgloop.blocks;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.mojang.serialization.MapCodec;

public class BlockHexxedGlass extends TransparentBlock implements ICantBeRaycasted, BeaconBeamBlock{
    
    public static final MapCodec<BlockHexxedGlass> CODEC = simpleCodec(BlockHexxedGlass::new);

    public BlockHexxedGlass(BlockBehaviour.Properties settings){
        super(settings);
    }

    @Override
    public MapCodec<BlockHexxedGlass> codec(){
        return CODEC;
    }

    public DyeColor getColor(){
        return DyeColor.PINK;
    }
}
