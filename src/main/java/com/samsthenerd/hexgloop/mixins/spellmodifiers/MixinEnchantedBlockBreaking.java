package com.samsthenerd.hexgloop.mixins.spellmodifiers;

import java.util.Set;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.casting.gloopifact.ICADHarnessStorage;

import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;

@Mixin(Level.class)
public class MixinEnchantedBlockBreaking {
    @ModifyExpressionValue(
        method="destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
        at=@At(value="FIELD", target="net/minecraft/world/item/ItemStack.EMPTY : Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack modifyBreakingBlock(ItemStack original, BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth){
        if(breakingEntity instanceof ServerPlayer sPlayer){
            Set<CastingVM> harnesses = ((ICADHarnessStorage)(Object)sPlayer).getHarnesses();
            if(!harnesses.iterator().hasNext()) return original;
            CastingVM harness = harnesses.iterator().next();
            ItemStack maybeStack = ((IContextHelper)(Object)(harness.getEnv())).getCastingItem();
            if(maybeStack != null){
                return maybeStack;
            }
        }

        return original;
    }
}
