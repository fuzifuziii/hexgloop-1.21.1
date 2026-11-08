package com.samsthenerd.hexgloop.mixins.castingfrog;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.network.HexGloopNetwork;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;

/*
 * this is yoinked hard from artifact's cloud in a bottle:
 * https://github.com/florensie/artifacts-fabric/blob/3a4e29d152172a5424a84b11ee2b9755c4be6c56/src/main/java/artifacts/mixin/item/cloudinabottle/LivingEntityMixin.java
 */
@Mixin(LivingEntity.class)
public abstract class MixinCastOnDoubleJump extends Entity{
    @Shadow
	protected boolean jumping;
	// Is entity double jumping in this tick
	@Unique
	private boolean isDoubleJumping = false;
	// Has entity released jump key since last jump
	@Unique
	private boolean jumpWasReleased = false;
	// Has entity double jumped during current airtime
	@Unique
	private boolean hasDoubleJumped = false;

    @Shadow
	public abstract boolean onClimbable(); // 1.21: renamed from isClimbing()

    @Inject(method="aiStep()V", at=@At("HEAD"))
    public void tryFrogCasting(CallbackInfo info){
        LivingEntity self = (LivingEntity) (Object) this;
		jumpWasReleased |= !this.jumping;

		if ((this.onGround() || this.onClimbable()) && !this.isInWater()) {
			this.hasDoubleJumped = false;
		}

		boolean flying = self instanceof Player player && player.getAbilities().flying;
		if (this.jumping && this.jumpWasReleased && !this.isInWater() && !this.onGround() && !this.isPassenger()
				&& !this.hasDoubleJumped && !flying) {
                
            this.hasDoubleJumped = true;
            List<ItemStack> frogStacks = HexGloopItems.CASTING_FROG_ITEM.get().getEquippedFrogs(self);
            HexGloop.logPrint("is kinda double jumping");
            for(ItemStack frogStack : frogStacks){
                if(frogStack == null || frogStack.isEmpty()){
                    continue;
                }
                sendFrogCastPacket(frogStack);
            }
		}
    }

    private void sendFrogCastPacket(ItemStack frogStack){
        RegistryFriendlyByteBuf bufC2S = new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.client.Minecraft.getInstance().level.registryAccess());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(bufC2S, frogStack);
        NetworkManager.sendToServer(HexGloopNetwork.C2S_FROG_CASTING, bufC2S);
        return;
    }

    @Inject(method = "jumpFromGround", at = @At("RETURN"))
	private void setJumpReleased(CallbackInfo info) {
		this.jumpWasReleased = false;
	}

    // garbage to shut the compiler up
    public MixinCastOnDoubleJump(EntityType<?> type, Level world){
        super(type, world);
    }
}
