package com.samsthenerd.hexgloop.casting.mirror;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import com.samsthenerd.hexgloop.blockentities.BlockEntityPedestal;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation;

public class OpBindMirror implements ConstMediaAction {
    private boolean temp;

    public OpBindMirror(boolean temp){
        this.temp = temp;
    }

    @Override
    public int getArgc(){ return 1;}

    @Override
    public long getMediaCost(){
        return (temp ? 10 : 4 * MediaConstants.DUST_UNIT);
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        if(!((Object)context instanceof IMirrorBinder binder)) return List.of();
        if(args.get(0) instanceof NullIota){
            // received null, want to unbind
            binder.bindTo(null, temp);
            return List.of();
        }
        Vec3 pos = OperatorUtils.getVec3(args, 0, getArgc());
        if(context.isVecInRange(pos)){
            BlockPos bPos = BlockPos.containing(pos);
            BlockEntityPedestal pedestal = context.getWorld().getBlockEntity(bPos, HexGloopBEs.PEDESTAL_BE.get()).orElse(null);
            if(pedestal == null){
                // need to mishap about no pedestal found,, agony
                // MishapThrowerWrapper.throwMishap(new MishapBadBlock(bPos, HexGloopBlocks.MIRROR_PEDESTAL_BLOCK.get().getName()));
                binder.bindTo(null, temp);
            } else {
                UUID uuid = pedestal.getPersistentUUID();
                BoundMirror mirror = new BoundMirror(context.getWorld(), bPos, uuid);
                binder.bindTo(mirror, temp);
            }
        } else {
            // need to range mishap
            MishapThrowerWrapper.throwMishap(new MishapBadLocation(pos, "too_far"));
        }
        return List.of();
    }

}
