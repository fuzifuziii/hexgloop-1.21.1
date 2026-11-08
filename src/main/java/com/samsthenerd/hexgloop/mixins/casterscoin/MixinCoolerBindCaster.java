package com.samsthenerd.hexgloop.mixins.casterscoin;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.items.ItemCastersCoin;

import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.casting.actions.rw.OpTheCoolerWrite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(OpTheCoolerWrite.class)
public class MixinCoolerBindCaster {
    @Inject(method ="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;",
    at = @At("RETURN"), remap=false)
    private void bindCoinToCaster(@NotNull List<? extends Iota> args, @NotNull CastingEnvironment ctx, CallbackInfoReturnable<SpellAction.Result> cir){
        Entity ent = OperatorUtils.getEntity(args, ctx.getWorld(), 0, ((OpTheCoolerWrite)(Object)(this)).getArgc());
        // we know it passed all the checks already since we're at return ?
        if(cir.getReturnValue() == null) return; // gonna add this just in case it tries to call here from a mishap?
        if(ent instanceof ItemEntity itemEnt){
            ItemStack stack = itemEnt.getItem();
            if(stack.getItem() instanceof ItemCastersCoin readOnlyItem && !stack.isEmpty()){
                // the rest is absolutely none of my business.
                // there shouldn't be anything wrong with a blank coin having a caster in it ? just check that it's not blank before doing anything with it
                Player caster = ctx.getCastingEntity() instanceof Player p ? p : null;
                if(caster != null) 
                    readOnlyItem.setBoundCaster(stack, caster);
            }
        }
    }
}
