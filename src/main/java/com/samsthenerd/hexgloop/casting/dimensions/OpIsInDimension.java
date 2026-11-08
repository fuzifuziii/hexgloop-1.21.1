package com.samsthenerd.hexgloop.casting.dimensions;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class OpIsInDimension implements ConstMediaAction {
    private ResourceKey<Level> worldRef;

    public OpIsInDimension(ResourceKey<Level> worldRef){
        this.worldRef = worldRef;
    }

    @Override
    public int getArgc(){ return 1;}

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        Entity entity = OperatorUtils.getEntity(args, context.getWorld(), 0, getArgc());
        // entity should be nonnull from getEntity 
        Boolean isInDim = entity.level().dimension() == worldRef;
        
        return List.of(new BooleanIota(isInDim));
    }

}