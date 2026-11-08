package com.samsthenerd.hexgloop.mixins.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.items.ItemLibraryCard;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

@Mixin(targets = {
    "at/petrak/hexcasting/common/casting/actions/akashic/OpAkashicRead",
})
public class MixinHonorLibraryCardReads {
    @WrapOperation(method = {
        "execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Ljava/util/List;",
    },
    at = @At(value = "INVOKE", target="at/petrak/hexcasting/api/casting/eval/CastingEnvironment.getWorld ()Lnet/minecraft/server/level/ServerLevel;"))
    public ServerLevel getAlternateLibraryDim(CastingEnvironment ctx, Operation<ServerLevel> original){
        ServerLevel originalWorld = original.call(ctx);
        if(!(ctx.getCastingEntity() instanceof net.minecraft.world.entity.player.Player caster)) return originalWorld;
        Inventory pInv = caster.getInventory();
        for(int i = 0; i < pInv.getContainerSize(); i++){
            ItemStack stack = pInv.getItem(i);
            if(stack.getItem() instanceof ItemLibraryCard libCard){
                ResourceKey<Level> dim = libCard.getDimension(stack);
                if(dim != null){
                    ServerLevel newWorld = originalWorld.getServer().getLevel(dim);
                    if(newWorld != null) return newWorld;
                }
            }
        }
        return originalWorld;
    }
}
