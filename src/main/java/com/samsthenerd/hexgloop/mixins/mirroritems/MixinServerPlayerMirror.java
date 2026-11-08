package com.samsthenerd.hexgloop.mixins.mirroritems;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.samsthenerd.hexgloop.casting.mirror.BoundMirror;
import com.samsthenerd.hexgloop.casting.mirror.IMirrorBinder;
import com.samsthenerd.hexgloop.casting.mirror.IPlayerPTUContext;
import com.samsthenerd.hexgloop.items.ItemAbstractPassThrough.PassThroughUseContext;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerMirror extends Entity implements IMirrorBinder, IPlayerPTUContext{

    private BoundMirror boundMirror;

    public void bindTo(@Nullable BoundMirror mirror, boolean temp){
        if(!temp){
            this.boundMirror = mirror;
            updateTrackedStack();
        }
    }

    @Nullable
    public BoundMirror getBoundMirror(){
        return this.boundMirror;
    }

     // don't call this,,
     public MixinServerPlayerMirror(){
        super(null, null);
    }

    @Inject(method = "tick()V", at=@At("HEAD"))
    public void updateTrackedStackOnTick(CallbackInfo ci){
        updateTrackedStack();
    }

    public void updateTrackedStack(){
        if(this.boundMirror != null){
            this.entityData.set(HELD_STACK, this.boundMirror.getItemStack(getServer()));
        } else {
            this.entityData.set(HELD_STACK, ItemStack.EMPTY);
        }
    }

    @Inject(method="readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at=@At("TAIL"))
    public void readBoundMirror(CompoundTag tag, CallbackInfo ci){
        if(tag.contains(BoundMirror.NBT_KEY)){
            this.boundMirror = BoundMirror.fromNbt(tag.getCompound(BoundMirror.NBT_KEY));
        }
    }

    @Inject(method="addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at=@At("TAIL"))
    public void writeBoundMirror(CompoundTag tag, CallbackInfo ci){
        if(this.boundMirror != null){
            tag.put(BoundMirror.NBT_KEY, this.boundMirror.toNbt());
        }
    }
    
    private PassThroughUseContext<?,?> ptuContext;

    @Nullable
    public PassThroughUseContext<?,?> getPTUContext(){
        return this.ptuContext;
    }

    public void setPTUContext(@Nullable PassThroughUseContext<?,?> context){
        this.ptuContext = context;
    }
}
