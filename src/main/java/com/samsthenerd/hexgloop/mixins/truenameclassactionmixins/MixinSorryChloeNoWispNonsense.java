package com.samsthenerd.hexgloop.mixins.truenameclassactionmixins;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.misc.worldData.TruenameLockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@Pseudo
@Mixin(targets="ram.talia.hexal.common.entities.BaseCastingWisp")
public class MixinSorryChloeNoWispNonsense {
    private UUID keyUuid = null;
    private static final String KEY_UUID_KEY = "keyUuid";

    @Inject(method="getCaster()Lnet/minecraft/world/entity/player/Player;", at=@At("RETURN"), cancellable = true)
    public void NullifyCasterIfLockChanged(CallbackInfoReturnable<Player> cir) {
        Player player = cir.getReturnValue();
        if(player instanceof ServerPlayer sPlayer){
            ServerLevel world = sPlayer.serverLevel();
            if(world == null) return;
            MinecraftServer server = world.getServer();
            if(server == null) return;
            UUID lockUUID = TruenameLockState.getServerState(server).getLockUUID(player.getUUID());
            if (lockUUID == null) return;
            if (!lockUUID.equals(keyUuid)) {
                cir.setReturnValue(null);
            }
        }
    }

    @Inject(method="setCaster(Lnet/minecraft/world/entity/player/Player;)V", at=@At("TAIL"))
    public void SetDefaultUUIDKey(Player player, CallbackInfo ci){
        if(keyUuid != null) return;
        if(player instanceof ServerPlayer sPlayer){
            ServerLevel world = sPlayer.serverLevel();
            if(world == null) return;
            MinecraftServer server = world.getServer();
            if(server == null) return;
            this.keyUuid = TruenameLockState.getServerState(server).getLockUUID(player.getUUID());
        }
    }

    @Inject(method="readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at=@At("TAIL"))
    public void ReadUUIDKeyFromNBT(CompoundTag nbt, CallbackInfo ci){
        if(nbt.hasUUID(KEY_UUID_KEY)){
            keyUuid = nbt.getUUID(KEY_UUID_KEY);
        }
    }

    @Inject(method="addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at=@At("TAIL"))
    public void WriteUUIDKeyToNBT(CompoundTag nbt, CallbackInfo ci){
        if(keyUuid != null){
            nbt.putUUID(KEY_UUID_KEY, keyUuid);
        }
    }
}
