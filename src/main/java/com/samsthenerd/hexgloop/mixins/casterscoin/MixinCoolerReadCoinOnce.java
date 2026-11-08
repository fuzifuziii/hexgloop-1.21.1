package com.samsthenerd.hexgloop.mixins.casterscoin;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.items.ItemCastersCoin;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.casting.actions.rw.OpTheCoolerRead;

@Mixin(OpTheCoolerRead.class)
public class MixinCoolerReadCoinOnce {
    @WrapOperation(method ="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Ljava/util/List;",
    at = @At(value="INVOKE", target="at/petrak/hexcasting/api/addldata/ADIotaHolder.readIota ()Lat/petrak/hexcasting/api/casting/iota/Iota;", ordinal=0))
    private Iota readCoinOnce(ADIotaHolder holder, Operation<Iota> original, @NotNull List<? extends Iota> args, @NotNull CastingEnvironment ctx){
        Iota iota = original.call(holder);
        Entity ent = OperatorUtils.getEntity(args, ctx.getWorld(), 0, ((OpTheCoolerRead)(Object)(this)).getArgc());
        ItemStack heldStack = ItemStack.EMPTY;
        Consumer<ItemStack> updateHeldItem = (stack) -> {};
        if(ent instanceof ItemEntity itemEnt){
            heldStack = itemEnt.getItem();
            updateHeldItem = (stack) -> {
                itemEnt.setItem(stack);
            };
        } else if(ent instanceof ItemFrame frameEnt){
            heldStack = frameEnt.getItem();
            updateHeldItem = (stack) -> {
                if(stack.isEmpty())
                    frameEnt.setItem(ItemStack.EMPTY);
                else
                    frameEnt.setItem(stack);
            };
        }
        if(heldStack != null && heldStack.getItem() instanceof ItemCastersCoin readOnlyItem){
            if(holder != null){
                // if it's gonna mishap then don't do the read once stuff
                if(iota == null){
                    if(holder.emptyIota() == null){ 
                        return iota;
                    }
                }
                // shouldn't read mishap so do the read once stuff
                ItemStack newStack = readOnlyItem.useCoin(heldStack);
                updateHeldItem.accept(heldStack); // incase it doesn't auto update 
                ItemEntity newEnt = new ItemEntity(ctx.getWorld(), ent.getX(), ent.getY(), ent.getZ(), newStack);
                ctx.getWorld().addFreshEntity(newEnt);
            }
        }
        return iota;
    }
}
