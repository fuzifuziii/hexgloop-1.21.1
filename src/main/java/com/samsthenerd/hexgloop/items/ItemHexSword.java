package com.samsthenerd.hexgloop.items;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.casting.gloopifact.ICADHarnessStorage;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;

public class ItemHexSword extends ItemHexTool implements IExtendedEnchantable.IWeaponEnchantable{

    public ItemHexSword(Properties pProperties) {
        super(pProperties, 2.0f, -2.4f);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(Blocks.COBWEB)) {
            return 15.0F;
        } else {
            // 1.21: BlockState.getMaterial() is gone; the old plant/leaves/vegetable check is BlockTags.SWORD_EFFICIENT (includes leaves)
            return state.is(BlockTags.SWORD_EFFICIENT) || state.is(BlockTags.LEAVES) ? 1.5F : 1.0F;
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return !miner.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(hasMediaToUse(stack)){
            withdrawMedia(stack, Math.min(MediaConstants.DUST_UNIT, getMedia(stack)), false);
            if(!(target.level() instanceof ServerLevel sWorld)) return false;
            // 1.21: EnchantmentHelper.getDamageBonus(stack, mobType) and DamageSource.MAGIC are gone;
            // modifyDamage applies the item's enchantments to the given base damage for this source/target
            float damage = EnchantmentHelper.modifyDamage(sWorld, stack, target, sWorld.damageSources().magic(), 8.0F);
            target.hurt(sWorld.damageSources().magic(), damage);
            List<Iota> instrs = getHex(stack, sWorld);
            if (instrs == null) {
                return false;
            }
            if(!(attacker instanceof ServerPlayer sPlayer)) return false;
            InteractionHand hand = attacker.getItemInHand(InteractionHand.MAIN_HAND) == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            // Hex 0.12: CastingEnvironment is abstract; packaged hexes cast through PackagedItemCastEnv + CastingVM.empty
            var env = new PackagedItemCastEnv(sPlayer, hand);
            if(((Object)env) instanceof IContextHelper ctxHelper){
                ctxHelper.setCastingItem(stack);
            }
            var harness = CastingVM.empty(env);
            harness.setImage(MishapThrowerWrapper.withStack(harness.getImage(), TreeList.from(List.of(new EntityIota(target)))));
            ((ICADHarnessStorage)(Object)sPlayer).addHarness(harness);
            harness.queueExecuteAndWrapIotas(instrs, sWorld);
            ((ICADHarnessStorage)(Object)sPlayer).removeHarness(harness);
            sPlayer.awardStat(Stats.ITEM_USED.get(this));
            return true;
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if(hasMediaToUse(stack)){
            if (state.getDestroySpeed(world, pos) != 0.0F) {
                withdrawMedia(stack, Math.min(2 * MediaConstants.DUST_UNIT, getMedia(stack)), false);
                // not sure if it needs a break sound or whatever ?
            }
            return true;
        }
        return false;
    }

    // 1.21: isCorrectToolForDrops now takes the stack as well
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(Blocks.COBWEB);
    }
}
