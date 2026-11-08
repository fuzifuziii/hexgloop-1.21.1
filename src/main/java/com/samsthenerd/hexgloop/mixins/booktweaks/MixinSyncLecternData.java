package com.samsthenerd.hexgloop.mixins.booktweaks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

@Mixin(BlockEntity.class)
public abstract class MixinSyncLecternData {

    @Shadow
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
        throw new AssertionError();
    }

    @ModifyReturnValue(
        method="getUpdateTag(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;",
        at=@At("RETURN")
    )
    public CompoundTag lecternInitialNbt(CompoundTag nbt, HolderLookup.Provider registries){
        if(((Object)this) instanceof LecternBlockEntity lecternBE){
            saveAdditional(nbt, registries);
            return nbt;
        }
        return nbt;
    }

    @ModifyReturnValue(
        method="getUpdatePacket()Lnet/minecraft/network/protocol/Packet;",
        at=@At("RETURN")
    )
    public Packet<ClientGamePacketListener> lecternUpdatePacket(Packet<ClientGamePacketListener> packet){
        if(((Object)this) instanceof LecternBlockEntity lecternBE){
            if(packet == null) // this feature really isn't that important, don't want to break other mods over it. TODO: figure out how to merge my stuff into here
            return ClientboundBlockEntityDataPacket.create(lecternBE);
        }
        return packet;
    }
}
