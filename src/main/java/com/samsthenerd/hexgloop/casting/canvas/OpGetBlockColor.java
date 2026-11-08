package com.samsthenerd.hexgloop.casting.canvas;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.minecraft.world.phys.Vec3;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;

public class OpGetBlockColor implements ConstMediaAction{

    public OpGetBlockColor(){
    }

    @Override
    public int getArgc(){ return 1; }

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        Brightness brightness = Brightness.NORMAL;
        int argOffset = 0;
        if(args.get(0) instanceof DoubleIota doubleIota){
            double brightnessVal = doubleIota.getDouble();
            if(brightnessVal >= 0 && brightnessVal <= 3){
                brightness = Brightness.byId((int)brightnessVal);
                argOffset = 1;
            }
        }
        BlockPos pos = OperatorUtils.getBlockPos(args, argOffset, getArgc());
        BlockState state = context.getWorld().getBlockState(pos);
        MapColor mapColor = state.getMapColor(context.getWorld(), pos);
        int rgb = mapColor.calculateRGBColor(brightness);
        return List.of(new Vec3Iota(new Vec3((rgb >>> 16) & 0xFF, (rgb >>> 8) & 0xFF, rgb & 0xFF)));
    }

    
}
