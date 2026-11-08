package com.samsthenerd.hexgloop.mixins.mirroritems;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — Hex 0.12 no longer calls ServerPlayer#getItemInHand inline;
// the mirror hook now lives in MixinDirectlyOnGetHeldItem (getHeldItemToOperateOn).

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.casting.actions.spells.OpColorize;
import at.petrak.hexcasting.common.casting.actions.spells.OpMakeBattery;
import at.petrak.hexcasting.common.casting.actions.spells.OpMakePackagedSpell;
import at.petrak.hexcasting.common.casting.actions.spells.OpRecharge;

@Pseudo
@Mixin(value={
    OpMakeBattery.class,
    OpMakePackagedSpell.class,
    OpRecharge.class,
    OpColorize.class
})
public class MixinEveryInlineGetHeldItemCall {
    @WrapOperation(method = {
        "execute(Ljava/util/List;Lat/petrak/hexcasting/api/spell/casting/CastingEnvironment;)Ljava/util/List;",
        "execute(Ljava/util/List;Lat/petrak/hexcasting/api/spell/casting/CastingEnvironment;)Lkotlin/Triple;"
    },
    at = @At(value = "INVOKE", target="net/minecraft/server/level/ServerPlayer.getItemInHand (Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack getAlternateHandStack(ServerPlayer player, InteractionHand hand, Operation<ItemStack> original, List<Iota> args, CastingEnvironment context){
        ItemStack altStack = SyncedItemHandling.getAlternateHandStack(player, hand, context);
        return altStack == null ? original.call(player, hand) : altStack;
    }
}
