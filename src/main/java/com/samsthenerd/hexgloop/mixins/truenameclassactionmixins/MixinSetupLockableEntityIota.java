package com.samsthenerd.hexgloop.mixins.truenameclassactionmixins;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — EntityIota#serialize() is gone; iotas serialize via IotaType codecs now.

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.casting.truenameclassaction.ILockedIota;
import com.samsthenerd.hexgloop.misc.worldData.TruenameLockState;

import at.petrak.hexcasting.api.casting.iota.EntityIota;

@Mixin(EntityIota.class)
public class MixinSetupLockableEntityIota implements ILockedIota{
    private UUID keyUuid = null;

    // i guess maybe don't even need this as a part of the interface but whatever, good to have it external
    public UUID getUUIDKey(){
        return keyUuid;
    }

    public void setUUIDKey(UUID newKey){
        this.keyUuid = newKey;
    }

    @Inject(method = "serialize()Lnet/minecraft/nbt/Tag;", at=@At("RETURN"))
    public void addLockToEntityIotaSerialize(CallbackInfoReturnable<Tag> cir){
        CompoundTag nbt = (CompoundTag) cir.getReturnValue();
        // HexGloop.logPrint("in ent iota serialization");
        if(this.keyUuid != null){
            // HexGloop.logPrint("adding lockUUID to nbt: " + keyUuid.toString());
            nbt.putUUID("keyUUID", keyUuid);
        }
    }

    @Inject(method="<init>(Lnet/minecraft/world/entity/Entity;)V", at=@At("TAIL"))
    public void attachCurrentLockAtCreation(Entity ent, CallbackInfo ci){
        // HexGloop.logPrint("in ent iota creation");
        if(ent instanceof Player && ent.level() instanceof ServerLevel sWorld){
            this.keyUuid = TruenameLockState.getServerState(sWorld.getServer()).getLockUUID(ent.getUUID());
            // HexGloop.logPrint("putting lockUUID into ent iota: " + this.keyUuid);
        }
    }
}
