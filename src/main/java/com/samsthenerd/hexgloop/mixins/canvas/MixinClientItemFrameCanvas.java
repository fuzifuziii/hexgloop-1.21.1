package com.samsthenerd.hexgloop.mixins.canvas;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(ItemFrame.class)
public class MixinClientItemFrameCanvas {
    // 1.21: getFramedMapId() is gone — it just reads DataComponents.MAP_ID from the stack now,
    // and ItemSlateCanvas extends MapItem and carries that component itself, so no hook is needed.

    @WrapOperation(
        method="interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
        at=@At(value="INVOKE", target="net/minecraft/world/item/ItemStack.is (Lnet/minecraft/world/item/Item;)Z")
    )
    public boolean canvasIsBasicallyAMapIThinkDontYouAgreeItemFrameInteractionMethod(ItemStack stack, Item item, Operation<Boolean> original){
        if(item == Items.FILLED_MAP && stack.is(HexGloopItems.SLATE_CANVAS_ITEM.get())){
            return true;
        }
        return original.call(stack, item);
    }


}
