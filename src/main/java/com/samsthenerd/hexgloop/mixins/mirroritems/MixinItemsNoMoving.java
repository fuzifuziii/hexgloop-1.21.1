package com.samsthenerd.hexgloop.mixins.mirroritems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.blockentities.BlockEntityPedestal;
import com.samsthenerd.hexgloop.blockentities.HexGloopBEs;
import com.samsthenerd.hexgloop.misc.INoMoving;

@Mixin(Entity.class)
public class MixinItemsNoMoving implements INoMoving{
    private boolean noMoving = false;

    @Shadow
    private Vec3 deltaMovement;

    public void setNoMoving(boolean noMoving){
        this.noMoving = noMoving;
    }

    public boolean getNoMoving(){
        if(this.noMoving) return true;
        // handle client side pedestal check
        Entity thisEnt = (Entity)(Object)this;
        if(thisEnt instanceof ItemEntity && thisEnt.level().isClientSide && thisEnt.isNoGravity()){
            // find pedestal block nearby
            Level world = thisEnt.level();
            List<BlockPos> nearbyBlocks = new ArrayList<BlockPos>();
            BlockPos thisPos = thisEnt.blockPosition();
            nearbyBlocks.add(thisPos);
            for(Direction dir : Direction.values()){
                BlockPos newPos = thisPos.relative(dir);
                nearbyBlocks.add(newPos);
            }
            for(BlockPos pos : nearbyBlocks){
                BlockEntityPedestal pedestalBE = world.getBlockEntity(pos, HexGloopBEs.PEDESTAL_BE.get()).orElse(null);
                if(pedestalBE == null) continue;
                UUID persistentUuid = pedestalBE.getPersistentUUID();
                if(persistentUuid != null && persistentUuid.equals(thisEnt.getUUID())){
                    return true;
                }
            }
        }
        return false;
    }

    @Inject(method="setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
    at=@At("HEAD"), cancellable = true)
    public void cancelMoving(Vec3 newVelocity, CallbackInfo ci){
        if(getNoMoving()){
            deltaMovement = Vec3.ZERO;
            ci.cancel();
        }
    }

    @Inject(method="move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", 
    at=@At("HEAD"), cancellable = true)
    public void noMove(CallbackInfo ci){
        if(getNoMoving()){
            ci.cancel();
        }
    }

    // just put it on a random call in the try block
    @Inject(method="load(Lnet/minecraft/nbt/CompoundTag;)V",
    at=@At(value="INVOKE", target="net/minecraft/world/entity/Entity.setPosRaw (DDD)V"))
    public void readNoMoving(CompoundTag nbt, CallbackInfo ci){
        if(nbt.contains("NoMoving")){
            setNoMoving(nbt.getBoolean("NoMoving"));
        }
    }

    @Inject(method="saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;",
    at=@At("RETURN"), cancellable = true)
    public void writeNoMoving(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir){
        if(getNoMoving()){
            CompoundTag newNbt = cir.getReturnValue();
            newNbt.putBoolean("NoMoving", true);
            cir.setReturnValue(newNbt);
        }
    }

    @Inject(method="canChangeDimensions(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/Level;)Z", at=@At("RETURN"), cancellable = true)
    public void noTeleporting(Level fromLevel, Level toLevel, CallbackInfoReturnable<Boolean> cir){
        if(getNoMoving()){
            cir.setReturnValue(false);
        }
    }
}
