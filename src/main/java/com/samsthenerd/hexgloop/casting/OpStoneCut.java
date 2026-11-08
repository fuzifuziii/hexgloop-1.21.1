package com.samsthenerd.hexgloop.casting;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.hexgloop.compat.hexal.HexalMaybeIotas;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem;
import dev.architectury.platform.Platform;
import kotlin.Triple;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;

// entity, int | block type -> 
public class OpStoneCut implements SpellAction {

    public OpStoneCut(){
    }

    @Override
    public int getArgc(){ return 2;}

    public boolean hasCastingSound(@NotNull CastingEnvironment ctx){
        return false;
    }

    public boolean awardsCastingStat(@NotNull CastingEnvironment ctx){
        return true;
    }





    @Override
    public SpellAction.Result execute(List<? extends Iota> args, CastingEnvironment context){
        ItemEntity itemEnt = OperatorUtils.getItemEntity(args, context.getWorld(), 0, getArgc());
        context.assertEntityInRange(itemEnt);
        SingleRecipeInput inv = new SingleRecipeInput(itemEnt.getItem());
        List<StonecutterRecipe> matchingRecipes =  context.getWorld().getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, inv, context.getWorld())
            .stream().map(h -> h.value()).toList();
        if(matchingRecipes.isEmpty()){
            MishapThrowerWrapper.throwMishap(MishapBadItem.of(itemEnt, "no_stonecutting"));
            return null;
        }
        Iota selectorArg = args.get(1);
        StonecutterRecipe selectedRecipe = null;
        if(Platform.isModLoaded("hexal")){
            Item selectedItem = HexalMaybeIotas.getItemFromIota(selectorArg);
            if(selectedItem != null){
                for(StonecutterRecipe recipe : matchingRecipes){
                    if(recipe.getResultItem(context.getWorld().registryAccess()).getItem() == selectedItem){
                        selectedRecipe = recipe;
                        break;
                    }
                }
            }
        }
        if(selectedRecipe == null){
            int index = OperatorUtils.getInt(args, 1, getArgc()) % matchingRecipes.size();
            selectedRecipe = matchingRecipes.get(index);
        }



        return new SpellAction.Result(
            new Spell(itemEnt, selectedRecipe), 
            MediaConstants.DUST_UNIT / 8,
            List.of(), 1L);
    }

    public class Spell implements RenderedSpell{
        private ItemEntity itemEnt;
        private StonecutterRecipe recipe;

        public Spell(ItemEntity itemEnt, StonecutterRecipe recipe){
            this.itemEnt = itemEnt;
            this.recipe = recipe;
        }

        public void cast(CastingEnvironment ctx){
            ItemStack stack = itemEnt.getItem();
            ItemStack result = recipe.assemble(new SingleRecipeInput(stack), ctx.getWorld().registryAccess());
            result.setCount(stack.getCount()); // craft the whole thing ! 
            stack.setCount(0);
            ItemEntity resultEnt = new ItemEntity(ctx.getWorld(), itemEnt.getX(), itemEnt.getY(), itemEnt.getZ(), result, 0, 0, 0);
            ctx.getWorld().addFreshEntity(resultEnt);
        }
    }

}


