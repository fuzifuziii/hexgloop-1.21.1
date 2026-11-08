package com.samsthenerd.hexgloop.items;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.casting.gloopifact.ICADHarnessStorage;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.utils.TreeList;

public class ItemHexMiningTool extends ItemHexTool implements IExtendedEnchantable.IToolEnchantable{

    private final Set<TagKey<Block>> effectiveBlocks;
    private final float realAttackDamage;
    private final boolean castOnAttack;
    private final float miningSpeed = 10.0F;

    public ItemHexMiningTool(Properties pProperties, float attackDamage, float attackSpeed, Set<TagKey<Block>> effectiveBlocks, boolean castOnAttack) {
        super(pProperties, 2.0f, attackSpeed);
        this.effectiveBlocks = effectiveBlocks;
        this.realAttackDamage = attackDamage;
        this.castOnAttack = castOnAttack;
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if(!hasMediaToUse(stack)){
            return 0.5F;
        }
        for(TagKey<Block> tag : effectiveBlocks){
            if(state.is(tag)){
                return miningSpeed;
            }
        }
        return 1.0F;
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(hasMediaToUse(stack)){
            withdrawMedia(stack, Math.min(MediaConstants.DUST_UNIT, getMedia(stack)), false);
            if(!(target.level() instanceof ServerLevel sWorld)) return false;
            // 1.21: EnchantmentHelper.getDamageBonus(stack, mobType) and DamageSource.MAGIC are gone;
            // modifyDamage applies the item enchantments to the base damage for this source/target
            float damage = EnchantmentHelper.modifyDamage(sWorld, stack, target, sWorld.damageSources().magic(), realAttackDamage);
            target.hurt(sWorld.damageSources().magic(), damage);
            if(!(attacker instanceof ServerPlayer sPlayer)) return false;
            if(castOnAttack){
                List<Iota> instrs = getHex(stack, sWorld);
                if (instrs == null) {
                    return false;
                }
                InteractionHand hand = attacker.getItemInHand(InteractionHand.MAIN_HAND) == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                // Hex 0.12: CastingEnvironment is abstract; packaged hexes cast through PackagedItemCastEnv + CastingVM.empty
                var env = new PackagedItemCastEnv(sPlayer, hand);
                if(((Object)env) instanceof IContextHelper ctxHelper){
                    ctxHelper.setCastingItem(stack);
                }
                var harness = CastingVM.empty(env);
                harness.setImage(MishapThrowerWrapper.withStack(harness.getImage(), TreeList.from(List.of(new EntityIota(target)))));
                harness.queueExecuteAndWrapIotas(instrs, sWorld);
            }
            sPlayer.awardStat(Stats.ITEM_USED.get(this));
            return true;
        }
        return false;
    }

    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if(hasMediaToUse(stack)){
            if (state.getDestroySpeed(world, pos) != 0.0F) {
                withdrawMedia(stack, Math.min(2 * MediaConstants.DUST_UNIT, getMedia(stack)), false);
            }
            if(!(miner instanceof ServerPlayer sPlayer)) return false;
            if(!(world instanceof ServerLevel sWorld)) return false;
            List<Iota> instrs = getHex(stack, sWorld);
            if (instrs == null) {
                return false;
            }
            InteractionHand hand = miner.getItemInHand(InteractionHand.MAIN_HAND) == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            var env = new PackagedItemCastEnv(sPlayer, hand);
            if(((Object)env) instanceof IContextHelper ctxHelper){
                ctxHelper.setCastingItem(stack);
            }
            var harness = CastingVM.empty(env);
            harness.setImage(MishapThrowerWrapper.withStack(harness.getImage(), TreeList.from(List.of(new Vec3Iota(new Vec3(pos.getX(), pos.getY(), pos.getZ()))))));
            ((ICADHarnessStorage)(Object)sPlayer).addHarness(harness);
            harness.queueExecuteAndWrapIotas(instrs, sWorld);
            ((ICADHarnessStorage)(Object)sPlayer).removeHarness(harness);
            sPlayer.awardStat(Stats.ITEM_USED.get(this));
            return true;
        }
        return false;
    }

    // 1.21: isCorrectToolForDrops now takes the stack as well
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        for(TagKey<Block> tag : effectiveBlocks){
            if(state.is(tag)){
                return true;
            }
        }
        return false;
    }
}
