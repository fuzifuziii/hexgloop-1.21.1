package com.samsthenerd.hexgloop.mixins.booktweaks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.network.HexGloopNetwork;

import at.petrak.hexcasting.common.entities.EntityWallScroll;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.item.ItemModBook;

@Mixin(ItemModBook.class)
public class MixinCopyPatternToScrolls {
    @Inject(method="use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
    at=@At("HEAD"), cancellable = true)
    public void handleClickOnScroll(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        // HexGloop.logPrint("clicked with book");
        ItemStack stack = player.getItemInHand(hand);
		Book book = ItemModBook.getBook(stack);
        if(book == null){
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, stack));
            return;
        }
        if(player instanceof ServerPlayer sPlayer){
            if(book.id.toString().equals("hexcasting:thehexbook")){
                if(isLookingAtScroll(player)){
                    // HexGloop.logPrint("looking at scroll maybe ?");
                    RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.core.RegistryAccess.EMPTY);
                    buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
                    NetworkManager.sendToPlayer(sPlayer, HexGloopNetwork.PROMPT_REPLACE_SCROLL_ID, buf);
                    cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.SUCCESS, stack));
                } else {
                    // HexGloop.logPrint("not looking at scroll");
                }
            }
        }
    }

    private boolean isLookingAtScroll(Player player){
        Vec3 origin = player.getEyePosition();
        Vec3 endPos = origin.add(player.getLookAngle().normalize().scale(5));
        // just yoink this bit from hexcasting entity raycast
        EntityHitResult entResult = ProjectileUtil.getEntityHitResult(player, origin, endPos, new AABB(origin, endPos), (ent) -> {
            return ent instanceof EntityWallScroll;
        }, 1000000);
        if(entResult == null || entResult.getEntity() == null) return false;
        HitResult result = player.pick(5, 0, false);
        // if(result.getType() == HitResult.Type.ENTITY){
        //     EntityHitResult entResult = (EntityHitResult) result;
        //     if(entResult.getEntity() instanceof EntityWallScroll){
        //         return true;
        //     }
        // }
        if(result.getType() == HitResult.Type.MISS || result.distanceTo(player) > entResult.distanceTo(player)){
            return true;
        }
        return false;
    }
}
