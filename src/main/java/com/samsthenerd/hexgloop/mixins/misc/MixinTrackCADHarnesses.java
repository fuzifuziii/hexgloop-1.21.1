package com.samsthenerd.hexgloop.mixins.misc;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

import com.samsthenerd.hexgloop.casting.gloopifact.ICADHarnessStorage;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;

@Mixin(ServerPlayer.class)
public class MixinTrackCADHarnesses implements ICADHarnessStorage{
    private Set<CastingVM> harnesses = new HashSet<CastingVM>();

    public void addHarness(CastingVM harness){
        harnesses.add(harness);
    }

    // get the harness that has this context
    public CastingVM getHarness(CastingEnvironment ctx){
        for(CastingVM harness : harnesses){
            if(harness.getEnv() == ctx) return harness;
        }
        return null;
    }

    public Set<CastingVM> getHarnesses(){
        return harnesses;
    }

    public void removeHarness(CastingVM harness){
        harnesses.remove(harness);
    }
}
