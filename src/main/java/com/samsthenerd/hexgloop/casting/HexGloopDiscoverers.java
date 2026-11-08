package com.samsthenerd.hexgloop.casting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.samsthenerd.hexgloop.blocks.BlockThinkingCarpet;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;
import com.samsthenerd.hexgloop.casting.wehavelociathome.ILociHandler;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.IItemProviderLocus;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

public class HexGloopDiscoverers {
    public static void init(){
        // TODO(1.21 port): Hex 0.12 DiscoveryHandlers only has debug-item / extra-equipment discoverers.
        // The item slot, lens predicate, media holder and grid scale discoverers no longer exist, so these
        // need hooks elsewhere (mixins into CastingEnvironment#getUsableStacks, media holder capability, lens/grid scale checks):
        //   addItemSlotDiscoverer(HexGloopDiscoverers::getItemLoci)
        //   addLensPredicate(HexGloopDiscoverers::hasReflectedLens)
        //   addMediaHolderDiscoverer(HexGloopDiscoverers::getReflectedMediaHolder)
        //   addGridScaleModifier(HexGloopDiscoverers::getReflectedGridScaleModifier)
        //   addGridScaleModifier(player -> isOnThinkingCarpet(player) ? 0.9f : 1)
    }

    public static List<ItemStack> getItemLoci(CastingEnvironment ctx){
        List<ItemStack> items = new ArrayList<ItemStack>();
        if(ctx instanceof CircleCastEnv circleEnv){
            Level world = ctx.getWorld();
            BlockEntityAbstractImpetus impetus = circleEnv.getImpetus();
            if(impetus != null){
                ILociHandler lociHandler = ILociHandler.get(impetus);
                if(lociHandler == null) return items;
                for(Tuple<BlockPos, IItemProviderLocus> itemProviderPair : lociHandler.getTrackedModuleBlocks(IItemProviderLocus.class)){
                    NonNullList<ItemStack> newItems = itemProviderPair.getB().getProvidedItems(itemProviderPair.getA(), world, impetus);
                    for(ItemStack item : newItems){
                        if(item.isEmpty()) continue;
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    public static boolean hasReflectedLens(Player player){
        return getReflectedGridScaleModifier(player) < 1;
    }

    public static float getReflectedGridScaleModifier(Player player){
        ItemStack mainAlt = ItemStack.EMPTY;
        ItemStack offAlt = ItemStack.EMPTY;
        if(player instanceof ServerPlayer sPlayer){
            mainAlt = SyncedItemHandling.getAlternateHandStack(sPlayer, InteractionHand.MAIN_HAND, null);
            offAlt = SyncedItemHandling.getAlternateHandStack(sPlayer, InteractionHand.OFF_HAND, null);
        } else {
            Tuple<ItemStack, Boolean> mainRes = SyncedItemHandling.getAlternateStackClient(player, InteractionHand.MAIN_HAND);
            mainAlt = mainRes == null ? ItemStack.EMPTY : mainRes.getA();
            Tuple<ItemStack, Boolean> offRes = SyncedItemHandling.getAlternateStackClient(player, InteractionHand.OFF_HAND);
            offAlt = offRes == null ? ItemStack.EMPTY : offRes.getA();
        }
        mainAlt = mainAlt == null ? ItemStack.EMPTY : mainAlt;
        offAlt = offAlt == null ? ItemStack.EMPTY : offAlt;
        float scale = 1;
        if(mainAlt.getItem() instanceof ItemLens){
            scale *= 0.75;
        }
        if(mainAlt != offAlt && offAlt.getItem() instanceof ItemLens){
            scale *= 0.75;
        }
        return scale;
    }

    
    // I guess it'll get bound reflected holder no matter what
    public static List<ADMediaHolder> getReflectedMediaHolder(CastingVM harness){
        CastingEnvironment ctx = harness.getEnv();
        ItemStack mainAlt = SyncedItemHandling.getAlternateHandStack(ctx.getCastingEntity(), InteractionHand.MAIN_HAND, ctx);
        mainAlt = mainAlt == null ? ItemStack.EMPTY : mainAlt;
        ItemStack offAlt = SyncedItemHandling.getAlternateHandStack(ctx.getCastingEntity(), InteractionHand.OFF_HAND, ctx);
        offAlt = offAlt == null ? ItemStack.EMPTY : offAlt;
        List<ADMediaHolder> holders = new ArrayList<ADMediaHolder>();
        ADMediaHolder mainHolder = IXplatAbstractions.INSTANCE.findMediaHolder(mainAlt);
        ADMediaHolder offHolder = IXplatAbstractions.INSTANCE.findMediaHolder(offAlt);
        if(mainHolder != null) holders.add(mainHolder);
        if(offHolder != null && mainAlt != offAlt) holders.add(offHolder); // make sure we don't add both if they're both reflecting the same thing
        return holders;
    }

    public static boolean isOnThinkingCarpet(Player player){
        return player.level().getBlockState(player.blockPosition()).getBlock() instanceof BlockThinkingCarpet;
    }
}
