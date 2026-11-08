package com.samsthenerd.hexgloop.casting;

import java.util.List;

import org.jetbrains.annotations.NotNull;

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
import kotlin.Triple;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class OpConjureTastyTreat implements SpellAction {

    public OpConjureTastyTreat(){
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
        // position to summon snacks
        Vec3 pos = OperatorUtils.getVec3(args, 0, getArgc());
        context.assertVecInRange(pos);
        ItemStack snackStack = HexGloopItems.HEX_SNACK.get().getDefaultInstance();
        snackStack.setCount(1);
        ItemEntity snack = new ItemEntity(context.getWorld(), pos.x(), pos.y(), pos.z(), snackStack);
        
        return new SpellAction.Result(new Spell(snack), MediaConstants.DUST_UNIT, List.of(), 1L);
        // return List.of(new EntityIota(snack));
    }

    public class Spell implements RenderedSpell{
        private ItemEntity snack;
        public Spell(ItemEntity snack){
            this.snack = snack;
        }

        public void cast(CastingEnvironment ctx){
            ctx.getWorld().addFreshEntity(snack);
        }
    }

}
