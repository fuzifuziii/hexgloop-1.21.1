package com.samsthenerd.hexgloop.mixins.ring;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import net.minecraft.world.item.ItemStack;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;

// Hex 0.12: CastingVM.withdrawMedia(IZ)I and CastingEnvironment.CastSource are gone.
// Ring part dropped: StaffCastEnv.extractMediaEnvironment(JZ)J has no staff-tag gate in 0.12,
// it unconditionally calls PlayerBasedCastEnv.extractMediaFromInventory, which already
// draws via MediaHelper.scanPlayerForMediaStuff (incl. DiscoveryHandlers extra equipment).
// Kitty/frog part moved here: PackagedItemCastEnv.extractMediaEnvironment looks up the
// ADHexHolder/ADMediaHolder from caster.getItemInHand(castingHand), but the torty/frog
// stack is not in the hand, so substitute it or no media is drawn from it.
@Mixin(PackagedItemCastEnv.class)
public class MixinLetRingDrawMedia {
    @ModifyExpressionValue(
        method = "extractMediaEnvironment(JZ)J",
        at = @At(value = "INVOKE", target = "net/minecraft/server/level/ServerPlayer.getItemInHand (Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack allowCastingCatMediaDraw(ItemStack original) {
        if (((Object) this) instanceof IContextHelper helper) {
            if (helper.isKitty()) {
                return helper.getKitty();
            }
            // and also forg
            if (helper.isFrogCasting()) {
                return helper.getFrog();
            }
        }
        return original;
    }
}
