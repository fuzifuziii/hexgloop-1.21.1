package com.samsthenerd.hexgloop.mixins.misc;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.blocks.ICantBeRaycasted;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.casting.actions.raycast.OpBlockAxisRaycast;
import at.petrak.hexcasting.common.casting.actions.raycast.OpBlockRaycast;

@Mixin(value={
    OpBlockRaycast.class,
    OpBlockAxisRaycast.class
})
public class MixinRaycastIgnore {
    @WrapOperation(method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Ljava/util/List;",
    at=@At(value="INVOKE", target="net/minecraft/server/level/ServerLevel.clip (Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    public BlockHitResult ignoreBlockRaycast(ServerLevel world, ClipContext context, Operation<BlockHitResult> original, List<Iota> args, CastingEnvironment ctx){
        BlockHitResult hitResult = original.call(world, context);
        while(hitResult.getType() == BlockHitResult.Type.BLOCK 
            && world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof ICantBeRaycasted raycastModifierBlock){
            if(!raycastModifierBlock.passThrough(world, hitResult.getBlockPos())){
                return BlockHitResult.miss(hitResult.getLocation(), hitResult.getDirection(), hitResult.getBlockPos());
            }
            double soFarDistance = hitResult.getLocation().distanceTo(context.getFrom());
            if(soFarDistance >= 32) break;
            Vec3 direction = context.getTo().subtract(context.getFrom()).normalize();

            Vec3 newStart = hitResult.getLocation().add(direction);
            ClipContext newContext = new ClipContext(newStart, context.getTo(), Block.COLLIDER, Fluid.NONE, ctx.getCastingEntity());
            hitResult = original.call(world, newContext);
        }
        return hitResult;
    }
}
