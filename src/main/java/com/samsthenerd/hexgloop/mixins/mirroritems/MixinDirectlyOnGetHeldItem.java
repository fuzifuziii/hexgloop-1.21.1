package com.samsthenerd.hexgloop.mixins.mirroritems;

import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment.HeldItemInfo;

// Hex 0.12 replaced the inline ServerPlayer#getItemInHand calls with CastingEnvironment#getHeldItemToOperateOn(Predicate),
// so the mirror hook moved here: if vanilla finds nothing usable, offer the mirrored hand stack instead.
@Mixin(CastingEnvironment.class)
public class MixinDirectlyOnGetHeldItem {

    @ModifyReturnValue(
        method = "getHeldItemToOperateOn(Ljava/util/function/Predicate;)Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment$HeldItemInfo;",
        at = @At("RETURN")
    )
    public HeldItemInfo getAlternateHandStack(HeldItemInfo original, Predicate<ItemStack> predicate){
        CastingEnvironment env = (CastingEnvironment)(Object)this;
        LivingEntity caster = env.getCastingEntity();
        if(caster == null) return original;
        for(InteractionHand hand : InteractionHand.values()){
            ItemStack altStack = SyncedItemHandling.getAlternateHandStack(caster, hand, env);
            if(altStack != null && !altStack.isEmpty() && predicate.test(altStack)){
                return new HeldItemInfo(altStack, hand);
            }
        }
        return original;
    }

    // TODO(1.21 port): the old _init_$lambda$3/4 hooks injected mirror stacks into the operative-slot discoverer
    // lists. Those lambdas are gone; the modern equivalent is a hook into getUsableStacks(StackDiscoveryMode).
}
