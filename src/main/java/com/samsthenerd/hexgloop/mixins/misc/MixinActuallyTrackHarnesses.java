package com.samsthenerd.hexgloop.mixins.misc;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.casting.gloopifact.ICADHarnessStorage;

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;

// Hex 0.12: executeIotas/ControllerInfo -> queueExecuteAndWrapIotas/ExecutionClientView, getCtx()/getCaster() -> getEnv()/getCastingEntity()
@Mixin(ItemPackagedHex.class)
public class MixinActuallyTrackHarnesses {
    @WrapOperation(method="use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
    at=@At(value="INVOKE", target="at/petrak/hexcasting/api/casting/eval/vm/CastingVM.queueExecuteAndWrapIotas (Ljava/util/List;Lnet/minecraft/server/level/ServerLevel;)Lat/petrak/hexcasting/api/casting/eval/ExecutionClientView;"))
    public ExecutionClientView trackHarnesses(CastingVM harness, List<Iota> instrs, ServerLevel sWorld, Operation<ExecutionClientView> original){
        ServerPlayer sPlayer = harness.getEnv().getCastingEntity() instanceof ServerPlayer sp ? sp : null;
        if(sPlayer != null){
            ItemStack castingStackProbably = sPlayer.getItemInHand(harness.getEnv().getCastingHand());
            ((IContextHelper)(Object)harness.getEnv()).setCastingItem(castingStackProbably);
            ICADHarnessStorage storage = (ICADHarnessStorage)(Object)sPlayer;
            storage.addHarness(harness);
        }
        ExecutionClientView info = original.call(harness, instrs, sWorld);
        if(sPlayer != null){
            ICADHarnessStorage storage = (ICADHarnessStorage)(Object)sPlayer;
            storage.removeHarness(harness);
        }
        return info;
    }
}
