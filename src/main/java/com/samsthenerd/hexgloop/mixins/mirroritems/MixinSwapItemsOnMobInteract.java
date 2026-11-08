package com.samsthenerd.hexgloop.mixins.mirroritems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.items.ItemAbstractPassThrough;
import com.samsthenerd.hexgloop.items.ItemAbstractPassThrough.SimplePTUContext;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Mob.class)
public class MixinSwapItemsOnMobInteract {
    @WrapOperation(method="interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
    at=@At(value="INVOKE", target="net/minecraft/world/entity/Mob.checkAndHandleImportantInteractions (Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    public InteractionResult swapItemsOnInteractWithItem(Mob mob, Player player, InteractionHand hand, Operation<InteractionResult> original){
        return wrapInteract(mob, player, hand, original);
    }

    @WrapOperation(method="interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
    at=@At(value="INVOKE", target="net/minecraft/world/entity/Mob.mobInteract (Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    public InteractionResult swapItemsOnInteractMob(Mob mob, Player player, InteractionHand hand, Operation<InteractionResult> original){
        return wrapInteract(mob, player, hand, original);
    }

    // they're all the same signatures, don't think i can target multiple locations 
    private static InteractionResult wrapInteract(Mob mob, Player player, InteractionHand hand, Operation<InteractionResult> original){
        ItemStack heldItem = player.getItemInHand(hand);
        // keep this up here to just exit early if we need to -- although it seems like 
        if(!(heldItem.getItem() instanceof ItemAbstractPassThrough passThroughItem)){
            return original.call(mob, player, hand);
        }
        SimplePTUContext<InteractionResult> useContext = new SimplePTUContext<>(player.level(), player, hand, passThroughItem, (ctx)->{
            InteractionResult result = original.call(mob, player, hand);
            ItemStack newStackToStore = player.getItemInHand(hand);
            if(newStackToStore != ctx.storedItemRef) newStackToStore = newStackToStore.copy(); // copy incase it's somehow getting cleared elsewhere or something ?
            ctx.storedItemRef = newStackToStore;
            return new Tuple<>(result, ctx.storedItemRef);
        });
        InteractionResult result = useContext.call();
        return useContext.didSucceed ? result : original.call(mob, player, hand);
    }
}
