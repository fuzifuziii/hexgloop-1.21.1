package com.samsthenerd.hexgloop.mixins.mirroritems;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.samsthenerd.hexgloop.casting.mirror.BoundMirror;
import com.samsthenerd.hexgloop.casting.mirror.IMirrorBinder;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

@Mixin(CastingEnvironment.class)
public class MixinMirrorContext implements IMirrorBinder{
    BoundMirror tempMirror = null;

    // 1.21: CastingEnvironment no longer keeps a `caster` field.
    private ServerPlayer caster(){
        return ((CastingEnvironment)(Object)this).getCastingEntity() instanceof ServerPlayer p ? p : null;
    }

    public void bindTo(@Nullable BoundMirror mirror, boolean temp){
        if(temp){
            tempMirror = mirror;
        } else {
            if(((Object)caster()) instanceof IMirrorBinder casterBinder){
                casterBinder.bindTo(mirror);
            }
        }
    }

    @Nullable
    public BoundMirror getBoundMirror(){
        if(tempMirror != null) return tempMirror;
        if(((Object)caster()) instanceof IMirrorBinder casterBinder){
            return casterBinder.getBoundMirror();
        }
        return null;
    }

    public ItemStack getTrackedStack(){
        if(((Object)caster()) instanceof IMirrorBinder casterBinder){
            return casterBinder.getTrackedStack();
        }
        return ItemStack.EMPTY;
    }
}
