package com.samsthenerd.hexgloop.mixins.mirroritems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

import java.util.function.Predicate;

// Hex 0.12: OpMakeBattery's spell no longer calls ServerPlayer#setItemInHand directly — it goes through
// CastingEnvironment#replaceItem(Predicate, ItemStack, InteractionHand). Mirror items hook that instead.
@Mixin(targets = "at/petrak/hexcasting/common/casting/actions/spells/OpMakeBattery$Spell")
public class MixinPhialToEnt {
    @WrapOperation(method = "cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V",
    at=@At(value="INVOKE", target="at/petrak/hexcasting/api/casting/eval/CastingEnvironment.replaceItem (Ljava/util/function/Predicate;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Z"))
    public boolean redirectSetHandStackPhial(CastingEnvironment env, Predicate<ItemStack> predicate, ItemStack stack, InteractionHand hand, Operation<Boolean> original){
        LivingEntity caster = env.getCastingEntity();
        if(caster != null){
            ItemStack altStack = SyncedItemHandling.getAlternateHandStack(caster, hand, env);
            if(altStack != null){
                ItemEntity ent = SyncedItemHandling.getAlternateEntity(caster, hand, env);
                if(ent != null){
                    // probably fine ?
                    ent.setItem(stack);
                    return true;
                }
            }
        }
        return original.call(env, predicate, stack, hand);
    }
}
