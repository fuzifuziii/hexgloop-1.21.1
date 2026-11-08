package com.samsthenerd.hexgloop.mixins.casterscoin;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.items.ItemCastersCoin;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.casting.actions.rw.OpRead;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

@Mixin(OpRead.class)
public class MixinReadCoinOnce {
    // there was an issue with MixinCoolerReadCoinOnce where it was emptying the item stack before reading it. 
    // it *seems* to not be an issue here, I'm guessing the behind the scenes hex stuff just doesn't check for 0 count when reading straight from an item?
    // anyways, just something to look out for if it pops up again.
    @WrapOperation(method ="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Ljava/util/List;",
    // at = @At(value="INVOKE", target="at/petrak/hexcasting/api/addldata/ADIotaHolder.readIota (Lnet/minecraft/server/level/ServerLevel;)Lat/petrak/hexcasting/api/casting/iota/Iota;", ordinal=0))
    at = @At(value="INVOKE", target="at/petrak/hexcasting/xplat/IXplatAbstractions.findDataHolder (Lnet/minecraft/world/item/ItemStack;)Lat/petrak/hexcasting/api/addldata/ADIotaHolder;", ordinal=0))
    private ADIotaHolder readCoinOnce(IXplatAbstractions abstractionInstance, ItemStack stack, Operation<ADIotaHolder> original, @NotNull List<? extends Iota> args, @NotNull CastingEnvironment ctx){
        ItemStack cloneStack = stack.copy();
        // get the holder based off of a duplicate stack so we can return the proper ADIotaHolder while still decrementing the count
        ADIotaHolder holder = original.call(abstractionInstance, cloneStack);
        if(stack.getItem() instanceof ItemCastersCoin readOnlyItem){
            if(holder != null){
                Iota iota = holder.readIota();
                // if it's gonna mishap then don't do the read once stuff
                if(iota == null){
                    if(holder.emptyIota() == null){ 
                        // need to do a silly maybe ?
                        return holder;
                    }
                }
                // shouldn't read mishap so do the read once stuff
                ItemStack newStack = readOnlyItem.useCoin(stack);
                if(ctx.getCastingEntity() instanceof ServerPlayer player)
                    player.getInventory().placeItemBackInInventory(newStack);
            }
        }
        return holder;
    }
}
