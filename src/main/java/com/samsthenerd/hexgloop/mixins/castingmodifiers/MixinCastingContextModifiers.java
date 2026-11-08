package com.samsthenerd.hexgloop.mixins.castingmodifiers;

import java.util.function.BiFunction;
import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.hexgloop.casting.ContextModificationHandlers;
import com.samsthenerd.hexgloop.casting.ContextModificationHandlers.Modification;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

@Mixin(CastingEnvironment.class)
public class MixinCastingContextModifiers {
    @ModifyReturnValue(method="isVecInRange(Lnet/minecraft/world/phys/Vec3;)Z",
    at=@At("RETURN"))
    public boolean doAmbitModification(boolean original, Vec3 pos){
        boolean current = original;
        for(Tuple<TriFunction<CastingEnvironment, Vec3, Boolean, Modification>, Integer> pair : ContextModificationHandlers.AMBIT_MODIFIERS){
            TriFunction<CastingEnvironment, Vec3, Boolean, Modification> modifier = pair.getA();
            Modification mod = modifier.apply((CastingEnvironment)(Object)this, pos, current);
            if(mod != Modification.NONE){
                current = (mod == Modification.ENABLE);
            }
        }
        return current;
    }

    @ModifyReturnValue(method="isEnlightened()Z", at=@At("RETURN"), remap = false)
    public boolean doEnlightenmentModification(boolean original){
        boolean current = original;
        for(Tuple<BiFunction<CastingEnvironment, Boolean, Modification>, Integer> pair : ContextModificationHandlers.ENLIGHTENMENT_MODIFIERS){
            BiFunction<CastingEnvironment, Boolean, Modification> modifier = pair.getA();
            Modification mod = modifier.apply((CastingEnvironment)(Object)this, current);
            if(mod != Modification.NONE){
                current = (mod == Modification.ENABLE);
            }
        }
        return current;
    }

    // TODO(1.21 port): CastingEnvironment#getCanOvercast is gone in Hex 0.12 — overcast modifiers need a new hook.


}
