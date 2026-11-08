package com.samsthenerd.hexgloop.casting.gloopifact;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;

import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.common.lib.hex.HexImageComponents;
import net.minecraft.server.level.ServerPlayer;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

public class OpSyncRavenmindGloopifact implements Action {
    private boolean toOrFromStaff; //true if it's syncing gloopifact -> staff, false if it's syncing staff -> gloopifact
    
    public OpSyncRavenmindGloopifact(boolean toOrFromStaff){
        this.toOrFromStaff = toOrFromStaff;
    }




   

    @Override
    public OperationResult operate(CastingEnvironment env, CastingImage image, SpellContinuation continuation){
        GloopifactUtils.assertGloopcasting(env);
        ServerPlayer caster = (ServerPlayer) env.getCastingEntity();
        Iota ravenmind = image.getComponent(HexImageComponents.RAVENMIND.get());
        CastingImage newImage = image;
        // the "staff" side is the staffcast VM of the other hand
        CastingVM staffVM = IXplatAbstractions.INSTANCE.getStaffcastVM(caster, env.getOtherHand());
        if(toOrFromStaff){ // to staff
            if(ravenmind != null){
                MishapOthersName m = MishapOthersName.Companion.getTrueNameMishapFromDatum(env.getWorld(), ravenmind, caster);
                if(m != null) MishapThrowerWrapper.throwMishap(m);
            }
            CastingImage staffImage = staffVM.getImage().withComponent(HexImageComponents.RAVENMIND.get(), ravenmind);
            IXplatAbstractions.INSTANCE.setStaffcastImage(caster, staffImage);
        } else {
            Iota newRavenmind = staffVM.getImage().getComponent(HexImageComponents.RAVENMIND.get());
            if(newRavenmind != null){
                MishapOthersName m = MishapOthersName.Companion.getTrueNameMishapFromDatum(env.getWorld(), newRavenmind, caster);
                if(m != null) MishapThrowerWrapper.throwMishap(m);
            }
            newImage = image.withComponent(HexImageComponents.RAVENMIND.get(), newRavenmind);
        }
        return MishapThrowerWrapper.result(newImage, continuation);
    }
}
