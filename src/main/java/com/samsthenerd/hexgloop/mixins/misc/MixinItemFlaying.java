package com.samsthenerd.hexgloop.mixins.misc;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — OperatorUtils#getVillager is gone and execute() now returns
// SpellAction.Result instead of kotlin.Triple, so the item-flaying hooks need to be rebuilt against the new type.

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.samsthenerd.hexgloop.blocks.IDynamicFlayTarget;
import com.samsthenerd.hexgloop.casting.BrainsweepSpellInvoker;
import com.samsthenerd.hexgloop.items.IFlayableItem;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.RenderedSpell;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.casting.actions.spells.great.OpBrainsweep;
import at.petrak.hexcasting.api.HexAPI;
import kotlin.Triple;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(OpBrainsweep.class)
public class MixinItemFlaying {

    @WrapOperation(method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;",
    at=@At(value="INVOKE", target="at/petrak/hexcasting/api/casting/OperatorUtils.getVillager (Ljava/util/List;II)Lnet/minecraft/world/entity/npc/Villager;"))
    public Villager findDifferentSacrifice(List<? extends Iota> args, int index, int argc, Operation<Villager> original, 
        List<? extends Iota> methodArgsIgnore, CastingEnvironment ctx, @Share("itement") LocalRef<ItemEntity> itemEntRef){
        // i mean it should always be zero but still
        itemEntRef.set(null); // just reset it in case it still has anything from the last call
        if(index >= 0 && index < args.size()){
            Iota sacrificeIota = args.get(index);
            if(sacrificeIota instanceof EntityIota eIota && eIota.getEntity(ctx.getWorld()) instanceof ItemEntity itemEnt){
                ItemStack stack = itemEnt.getItem();
                if(stack.getItem() instanceof IFlayableItem flayable){
                    Villager villager = flayable.getFlayableVillager(stack, itemEnt, ctx);
                    if(villager != null){
                        itemEntRef.set(itemEnt);
                        return villager;
                    }
                }
            }
        }
        return original.call(args, index, argc);
    }

    // @Inject(method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;",
    // at=@At("RETURN"))
    // public void brainsweepItem(@NotNull List<? extends Iota> args, @NotNull CastingEnvironment ctx, 
    //     CallbackInfo ci, @Share("itement") LocalRef<ItemEntity> itemEntRef){
    //     ItemEntity itemEnt = itemEntRef.get();
    //     if(itemEnt == null) return;
    //     ItemStack stack = itemEnt.getStack();
    //     // it should be, but still just to safely cast
    //     if(stack.getItem() instanceof IFlayableItem flayable){
    //         flayable.handleBrainsweep(stack, itemEnt, ctx);
    //     }
    // }

    // @WrapOperation(method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;",
    // at=@At(value="INVOKE", target="kotlin/Triple.<init> (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"), remap=false)
    // public void addSacrificeProviderToSpell(Triple<Object, Object, Object> originalTriple, Object spell, Object cost, Object particleSprays, 
    //     Operation<Void> original, @Share("itement") LocalRef<ItemEntity> itemEntRef){

    //     ItemEntity itemEnt = itemEntRef.get();
    //     if(spell instanceof BrainsweepSpellInvoker spellMixin && itemEnt != null){
    //         spellMixin.setSacrificeProvider(itemEnt);
    //     }
    //     original.call(originalTriple, spell, cost, particleSprays);
    // }

    @ModifyArg(method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;",
    at=@At(value="INVOKE", target="kotlin/Triple.<init> (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"), remap=false, index=0)
    public Object addSacrificeProviderToSpell(Object spell, @Share("itement") LocalRef<ItemEntity> itemEntRef){
        ItemEntity itemEnt = itemEntRef.get();
        if(spell instanceof BrainsweepSpellInvoker spellMixin && itemEnt != null){
            spellMixin.setSacrificeProvider(itemEnt);
        }
        return spell;
    }


    @Inject(method="execute(Ljava/util/List;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)Lat/petrak/hexcasting/api/casting/castables/SpellAction$Result;", cancellable=true,
    at=@At(value="INVOKE", target="net/minecraft/server/level/ServerLevel.getBlockState (Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public void tryFlayIntoDynamicBlockTarget(@NotNull List<? extends Iota> args, @NotNull CastingEnvironment ctx, 
        CallbackInfoReturnable< Triple<RenderedSpell, Long, List<ParticleSpray>> > cir,
        @Local(ordinal = 0) BlockPos pos, @Local(ordinal=0) Villager sacrifice, @Share("itement") LocalRef<ItemEntity> itemEntRef){
        
        BlockState state = ctx.getWorld().getBlockState(pos);
        if(!(state.getBlock() instanceof IDynamicFlayTarget target)) return;
        // maybe move this into a spell ?
        boolean accepted = target.canAcceptMind(sacrifice, pos, ctx);
        if(accepted){
            cir.setReturnValue(new Triple<>(
                new FlaySpell(sacrifice, pos, itemEntRef.get()),
                10 * MediaConstants.CRYSTAL_UNIT,
                List.of(ParticleSpray.cloud(sacrifice.position(), 1.0, 20), ParticleSpray.burst(Vec3.atCenterOf(pos), 0.3, 100))
            ));
        }
    }

    private class FlaySpell implements RenderedSpell{
        private Villager sacrifice;
        private BlockPos pos;
        private ItemEntity sacrificeProvider;

        public FlaySpell(Villager sacrifice, BlockPos pos, @Nullable ItemEntity sacrificeProvider){
            this.sacrifice = sacrifice;
            this.pos = pos;
            this.sacrificeProvider = sacrificeProvider;
        }
        public void cast(CastingEnvironment ctx) {
            // ctx.getWorld().setBlockAndUpdate(pos, BrainsweepRecipe.copyProperties(state, recipe.result))
            BlockState state = ctx.getWorld().getBlockState(pos);
            if(!(state.getBlock() instanceof IDynamicFlayTarget target)) return;
            target.absorbVillagerMind(sacrifice, pos, ctx);

            // handle the actual sacrifice
            if(sacrificeProvider == null){
                HexAPI.instance().brainsweep(sacrifice);
                if (HexConfig.server().doVillagersTakeOffenseAtMindMurder()) {
                    // sacrifice.notifyDeath(ctx.getCaster());
                }
                ctx.getWorld().playSound((Player)null, sacrifice.blockPosition(), SoundEvents.VILLAGER_DEATH, SoundSource.AMBIENT, 0.8f, 1f);
            } else {
                ItemStack providerStack = sacrificeProvider.getItem();
                if(providerStack.getItem() instanceof IFlayableItem flayableItem){
                    Consumer<ItemStack> resultConsumer = (result) -> {
                        ItemEntity resultEnt = new ItemEntity(ctx.getWorld(), sacrificeProvider.getX(), sacrificeProvider.getY(), sacrificeProvider.getZ(), result, 0, 0, 0);
                        ctx.getWorld().addFreshEntity(resultEnt);
                    };
                    flayableItem.handleBrainsweep(providerStack, sacrificeProvider, ctx, resultConsumer);
                }
            }

            ctx.getWorld().playSound((Player)null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 0.5f, 0.8f);
        }
    }


    @Mixin(targets="at.petrak.hexcasting.common.casting.actions.spells.great.OpBrainsweep$Spell")
    public static class MixinBrainsweepSpell implements BrainsweepSpellInvoker{
        private ItemEntity sacrificeProvider = null;

        public void setSacrificeProvider(ItemEntity provider){
            sacrificeProvider = provider;
        }

        @WrapOperation(method="cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V", 
        at=@At(value="INVOKE", target="at/petrak/hexcasting/api/HexAPI.brainsweep (Lnet/minecraft/world/entity/Mob;)V"))
        public void wrapBrainsweep(Mob sacrifice, Operation<Void> original, CastingEnvironment ctx){
            if(sacrificeProvider == null){
                original.call(sacrifice);
            } else {
                ItemStack providerStack = sacrificeProvider.getItem();
                if(providerStack.getItem() instanceof IFlayableItem flayableItem){
                    Consumer<ItemStack> resultConsumer = (result) -> {
                        ItemEntity resultEnt = new ItemEntity(ctx.getWorld(), sacrificeProvider.getX(), sacrificeProvider.getY(), sacrificeProvider.getZ(), result, 0, 0, 0);
                        ctx.getWorld().addFreshEntity(resultEnt);
                    };
                    flayableItem.handleBrainsweep(providerStack, sacrificeProvider, ctx, resultConsumer);
                }
            }
        }

        @WrapOperation(method="cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V", 
        at=@At(value="INVOKE", target="at/petrak/hexcasting/ktxt/AccessorWrappers.tellWitnessesThatIWasMurdered (Lnet/minecraft/world/entity/npc/Villager;Lnet/minecraft/world/entity/Entity;)V"))
        public void maybeCancelNotifyDeath(Villager sacrifice, Entity murderer, Operation<Void> original, CastingEnvironment ctx){
            if(sacrificeProvider != null){
                ItemStack providerStack = sacrificeProvider.getItem();
                if(providerStack.getItem() instanceof IFlayableItem flayableItem){
                    if(!flayableItem.wasMurderous(providerStack, sacrificeProvider, ctx)) return;
                }
            }
            original.call(sacrifice);
        }


        @WrapOperation(method="cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V", 
        at=@At(value="INVOKE", target="net/minecraft/server/level/ServerLevel.playSound (Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
        public void maybeDontMakeVillagerDeathNoise(ServerLevel world, Player player, Entity entity, SoundEvent soundEvent, SoundSource soundCategory, float probablyVolume, float probablyPitch, Operation<Void> original, CastingEnvironment ctx){
            if(sacrificeProvider != null && soundEvent.equals(SoundEvents.VILLAGER_DEATH)){
                ItemStack providerStack = sacrificeProvider.getItem();
                if(providerStack.getItem() instanceof IFlayableItem flayableItem){
                    if(!flayableItem.wasMurderous(providerStack, sacrificeProvider, ctx)) return;
                }
            }
            original.call(world, player, entity, soundEvent, soundCategory, probablyVolume, probablyPitch);
        }
    }
}
