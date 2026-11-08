package com.samsthenerd.hexgloop.mixins.truenameclassactionmixins;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.samsthenerd.hexgloop.casting.truenameclassaction.ISetImpetusKey;
import com.samsthenerd.hexgloop.casting.truenameclassaction.MishapClearedTruename;
import com.samsthenerd.hexgloop.misc.worldData.TruenameLockState;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;

// Hex 0.12: BlockEntityStoredPlayerImpetus / BlockStoredPlayerImpetus removed, and
// BlockEntityAbstractImpetus.performSpellCasting() no longer exists. The truename
// lock now hooks startExecution(ServerPlayer) instead; the key can no longer be
// attached via the removed stored-player impetus (MixinAttachImpetusKey deleted),
// so a null key means "no key attached" and the check is skipped.
@Mixin(BlockEntityAbstractImpetus.class)
public class MixinClearImpetus implements ISetImpetusKey {
    private static final String KEY_UUID_KEY = "keyUuid";
    private UUID keyUuid = null;

    // so we can change it from the block class
    public void setKeyUUID(UUID keyUuid){
        this.keyUuid = keyUuid;
    }

    @Inject(method="startExecution(Lnet/minecraft/server/level/ServerPlayer;)V", at=@At("HEAD"), remap = false, cancellable = true)
    public void CheckLockBeforeCircleCast(ServerPlayer caster, CallbackInfo ci){
        if(keyUuid == null) return;
        if(caster == null) return;
        Player player = caster;
        ServerLevel world = caster.serverLevel();
        if(world == null) return;
        UUID lockUuid = TruenameLockState.getServerState(world.getServer()).getLockUUID(player.getUUID());
        if(lockUuid == null) return;
        if(!lockUuid.equals(keyUuid)){
            BlockEntityAbstractImpetus impetus = (BlockEntityAbstractImpetus)(Object)this;
            impetus.postMishap((new MishapClearedTruename(player)).makeError(Component.translatable("hexgloop.generic_circle_spell"), player));
            ci.cancel();
        }
    }

    @Inject(method="saveModData(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V", at=@At("TAIL"))
    public void SaveImpetusKeyUuid(CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci){
        if(keyUuid != null){
            nbt.putUUID(KEY_UUID_KEY, keyUuid);
        }
    }

    @Inject(method="loadModData(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V", at=@At("TAIL"))
    public void LoadImpetusKeyUuid(CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci){
        if(nbt.hasUUID(KEY_UUID_KEY)){
            keyUuid = nbt.getUUID(KEY_UUID_KEY);
        }
    }

}
