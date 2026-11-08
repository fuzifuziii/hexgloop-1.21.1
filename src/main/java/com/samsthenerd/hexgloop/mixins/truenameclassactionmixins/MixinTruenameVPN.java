package com.samsthenerd.hexgloop.mixins.truenameclassactionmixins;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — EntityIota$1#deserialize went away with the codec rewrite.

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.casting.truenameclassaction.ILockedIota;
import com.samsthenerd.hexgloop.misc.worldData.TruenameLockState;

import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.utils.HexUtils;

@Mixin(targets = "at.petrak.hexcasting.api.casting.iota.EntityIota$1")
public class MixinTruenameVPN {
    // make the entity iota deserialization return null if the lockUUID is not the same as the keyUUID
    @Inject(method="deserialize(Lnet/minecraft/nbt/Tag;Lnet/minecraft/server/level/ServerLevel;)Lat/petrak/hexcasting/api/casting/iota/EntityIota;"
    , at=@At("RETURN"), cancellable = true)
    public void checkUUIDLockOnEntityIotaDeser(Tag tag, ServerLevel sWorld, CallbackInfoReturnable<EntityIota> cir){
        // HexGloop.logPrint("in checkUUIDLockOnEntityIotaDeser");
        if(cir.getReturnValue() == null) return;
        // HexGloop.logPrint("nonnull return value");
        Entity ent = cir.getReturnValue().getEntity(sWorld);
        if(ent == null) return;
        // HexGloop.logPrint("nonnull entity");
        if(ent instanceof Player){
            UUID lockUUID = TruenameLockState.getServerState(sWorld.getServer()).getLockUUID(ent.getUUID());
            // HexGloop.logPrint("lockUUID: " + (lockUUID == null ? "null" : lockUUID.toString()));
            CompoundTag nbt = HexUtils.downcast(tag, CompoundTag.TYPE);
            UUID keyUUID = null;
            if(nbt.hasUUID("keyUUID")){
                keyUUID = nbt.getUUID("keyUUID");
            }
            ((ILockedIota)(cir.getReturnValue())).setUUIDKey(keyUUID);
            // HexGloop.logPrint("keyUUID: " + (keyUUID == null ? "null" : keyUUID.toString()));
            if(lockUUID != null && !lockUUID.equals(keyUUID)){
                // HexGloop.logPrint("lockUUID != keyUUID");
                cir.setReturnValue(null);
            }
        }
    }
}
