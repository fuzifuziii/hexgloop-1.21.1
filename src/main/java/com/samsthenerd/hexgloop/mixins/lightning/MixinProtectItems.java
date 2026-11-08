package com.samsthenerd.hexgloop.mixins.lightning;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinProtectItems {
    
    // maybe not the *best* way to do this but uh, oh well i guess
    @Inject(at = @At("HEAD"), method="thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V", cancellable = true)
    public void protectItems(ServerLevel world, LightningBolt lightning, CallbackInfo ci){
        if(((Object) this) instanceof ItemEntity itemEnt && itemEnt.isInWater()){
            itemEnt.setInvulnerable(true);
        }
    }
}
