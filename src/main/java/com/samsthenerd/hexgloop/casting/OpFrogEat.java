package com.samsthenerd.hexgloop.casting;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.hexgloop.casting.OpFrogEat.Spell;
import com.samsthenerd.hexgloop.casting.truenameclassaction.MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh;
import com.samsthenerd.hexgloop.items.HexGloopItems;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity;
import kotlin.Triple;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.item.ItemStack;

public class OpFrogEat implements SpellAction {

    public OpFrogEat(){
    }

    @Override
    public int getArgc(){ return 1;}

    public boolean hasCastingSound(@NotNull CastingEnvironment ctx){
        return false;
    }

    public boolean awardsCastingStat(@NotNull CastingEnvironment ctx){
        return true;
    }





    @Override
    public SpellAction.Result execute(List<? extends Iota> args, CastingEnvironment context){
        IContextHelper contextHelper = (IContextHelper)(Object)context;
        if(!contextHelper.isFrogCasting()){
            MishapThrowerWrapper.throwMishap(new MishapChloeIsGonnaFindSoManyWaysToBreakThisHuh(List.of("frog_casting")));
            return null;
        }
        ItemStack frogStack = contextHelper.getFrog();
        LivingEntity target = OperatorUtils.getLivingEntityButNotArmorStand(args, context.getWorld(), 0, getArgc());
        
        if(!Frog.canEat(target)){
            MishapThrowerWrapper.throwMishap(MishapBadEntity.of(target, "not_frog_food"));
            return null;
        }

        return new SpellAction.Result(
            new Spell(target, frogStack), 
            MediaConstants.SHARD_UNIT,
            List.of(), 1L);
    }

    public class Spell implements RenderedSpell{
        private LivingEntity target = null;
        private ItemStack frogStack = null;

        public Spell(LivingEntity target, ItemStack frogStack){
            this.target = target;
            this.frogStack = frogStack;
        }

        public void cast(CastingEnvironment ctx){
            if(target == null || frogStack == null) return;
            Frog fakeFrog = EntityType.FROG.create(ctx.getWorld());
            var frogVariant = HexGloopItems.CASTING_FROG_ITEM.get().getFrogVariant(frogStack);
            if(frogVariant != null) fakeFrog.setVariant(net.minecraft.core.Holder.direct(frogVariant));
            // not sure if this will target only slimes and whatnot 
            ctx.getWorld().playSound(null, fakeFrog, SoundEvents.FROG_EAT, SoundSource.NEUTRAL, 2.0f, 1.0f);
            fakeFrog.doHurtTarget(target);
            if (!target.isAlive()) {
                target.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

}

