package com.samsthenerd.hexgloop.mixins.recipes;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.ItemStack;

@Mixin(targets="at.petrak.hexcasting.common.casting.actions.spells.OpMakePackagedSpell$Spell")
public class MixinSplitCastingCost {

    @Shadow
    @Final
    private ItemStack stack;


    @WrapOperation(
        method="cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V",
        at=@At(value="INVOKE", target="at/petrak/hexcasting/api/utils/MediaHelper.extractMedia$default (Lnet/minecraft/world/item/ItemStack;JZZILjava/lang/Object;)J")
    )
    private long splitMediaCost(ItemStack mediaStack, long cost, boolean drainForBatteries, 
        boolean godKnowsWhatIHateKotlin, int noIdeaSomeInt, Object yeahSureHaveAnObjectWhyNot,
        Operation<Long> original){
        long mediaCost = original.call(mediaStack, cost, drainForBatteries, godKnowsWhatIHateKotlin, noIdeaSomeInt, yeahSureHaveAnObjectWhyNot);
        long count = stack.getCount();
        return mediaCost / count;
    }
}
