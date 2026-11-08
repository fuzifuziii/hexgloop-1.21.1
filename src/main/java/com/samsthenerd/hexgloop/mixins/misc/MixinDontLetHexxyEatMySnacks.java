package com.samsthenerd.hexgloop.mixins.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.samsthenerd.hexgloop.items.ItemSimpleMediaProvider;
import net.minecraft.world.item.ItemStack;

import at.petrak.hexcasting.common.casting.actions.spells.OpMakeBattery;
import at.petrak.hexcasting.common.casting.actions.spells.OpMakePackagedSpell;
import at.petrak.hexcasting.common.casting.actions.spells.OpRecharge;

// Hex 0.12 moved the "is this stack media?" check out of CastingVM's lambdas and into the battery ops themselves.
@Mixin({
    OpMakeBattery.class,
    OpMakePackagedSpell.class,
    OpRecharge.class
})
public class MixinDontLetHexxyEatMySnacks {
    @WrapOperation(
    method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;",
    at=@At(value = "INVOKE", target="at/petrak/hexcasting/api/utils/MediaHelper.isMediaItem (Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean dontEatSnacks(ItemStack stack, Operation<Boolean> original){
        if(stack.getItem() instanceof ItemSimpleMediaProvider simpleItem && !simpleItem.shouldGrabFromInventory(stack)){
            return false;
        } else {
            return original.call(stack);
        }
    }
}
