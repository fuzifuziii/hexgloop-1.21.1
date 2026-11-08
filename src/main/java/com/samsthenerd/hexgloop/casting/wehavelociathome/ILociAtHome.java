package com.samsthenerd.hexgloop.casting.wehavelociathome;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// meant to be implemented on a Block class.
// probably going to be heavily reworked for 1.20 port so just,, don't implement too much with it
// also not really a good way to stop the circle 
public interface ILociAtHome {
    // override this to add whatever logic or probably better to just have it return some variable 
    // you modify later if there's an error
    default public boolean shouldStopCircle(){
        return false;
    }

    default public Component getStopCircleError(){
        return null;
    }

    // try not to break stuff + try to be aware of consideration-type stuff i guess
    default public void rawLociCall(BlockPos pos, BlockState bs, Level world, CastingVM harness){}

    default public void waveEnter(BlockPos pos, BlockState bs, Level world, BlockEntityAbstractImpetus impetus){}

    default public void waveExit(BlockPos pos, BlockState bs, Level world, BlockEntityAbstractImpetus impetus){}

    // called when the circle stops, either early or on time.
    default public void circleStopped(BlockPos pos, BlockState bs, Level world, BlockEntityAbstractImpetus impetus){}
}
