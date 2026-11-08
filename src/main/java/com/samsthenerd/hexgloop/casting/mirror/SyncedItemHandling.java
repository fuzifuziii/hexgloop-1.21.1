package com.samsthenerd.hexgloop.casting.mirror;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.samsthenerd.hexgloop.items.ItemHandMirror;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class SyncedItemHandling {
    // for nbt rendering stuff: 0 = no, 1 = yes from mirror, 2 = yes from pedestal
    public static final String IS_REFLECTED_TAG = "is_reflected";

    // null if no alternate
    @Nullable
    public static ItemStack getAlternateHandStack(LivingEntity livEnt, InteractionHand hand, @Nullable CastingEnvironment context){
        ItemEntity itemEnt = getAlternateEntity(livEnt, hand, context);
        if(itemEnt != null){
            return itemEnt.getItem();
        }
        return null;
    }

    // if context is null then we just don't check ambit - important for getting it for arbitrary rendering stuff
    @Nullable
    public static ItemEntity getAlternateEntity(LivingEntity livEnt, InteractionHand hand, @Nullable CastingEnvironment context){
        ItemStack originalStack = livEnt.getItemInHand(hand);
        if(originalStack.getItem() instanceof ItemHandMirror mirrorItem && livEnt.level() instanceof ServerLevel sWorld){
            ResourceKey<Level> itemDim = mirrorItem.getMirroredItemDimension(originalStack);
            if(itemDim == null) itemDim = sWorld.dimension();
            ServerLevel itemWorld = sWorld.getServer().getLevel(itemDim);
            Iota iota = mirrorItem.readIota(originalStack);
            if(iota instanceof EntityIota entIota && entIota.getEntity(itemWorld) instanceof ItemEntity itemEnt){
                return itemEnt;
            }
        }
        if(originalStack.isEmpty()){
            // try to get the bound stack
            IMirrorBinder binder = null;
            // implicit null check and easy safe cast
            if(((Object)context) instanceof IMirrorBinder mirrorContext){
                binder = mirrorContext;
            } else if(livEnt instanceof IMirrorBinder mirrorEnt){ // same but for the maybe player
                binder = mirrorEnt;
            }
            if(binder == null) return null;
            BoundMirror boundMirror = binder.getBoundMirror();
            if(boundMirror != null){
                ItemEntity itemEnt = boundMirror.getItemEntity(livEnt.getServer());
                return itemEnt;
            }
        }
        // handle player specific bound stuff down here i guess
        return null;
    }

    // boolean is if it's from a hand or from a bound pedestal. null if there's just nothing relevant there
    // if you call this on both hands, if there is a bound pedestal it will only return the item for a single hand, with preference for the offhand
    @Nullable
    public static Tuple<ItemStack, Boolean> getAlternateStackClient(LivingEntity ent, InteractionHand hand){
        // handle hand mirror first
        ItemStack originalStack = ent.getItemInHand(hand);
        if(originalStack.getItem() instanceof ItemHandMirror mirrorItem){
            ItemStack mirrorStack = mirrorItem.getMirroredItemStack(originalStack);
            if(!mirrorStack.isEmpty() && mirrorItem.isMirrorActivated(originalStack)) return new Tuple<>(mirrorStack, true);
        }
        // handle pedestal thing here later i guess
        if(originalStack.isEmpty()){
            // go ahead for offhand then only use main hand if there's something in the offhand
            if(hand == InteractionHand.OFF_HAND || !ent.getItemInHand(InteractionHand.OFF_HAND).isEmpty()){
                // try to get the bound stack
                // implicit null check and easy safe cast
                if(ent instanceof IShallowMirrorBinder mirrorEnt){
                    ItemStack trackedStack = mirrorEnt.getTrackedStack();
                    if(!trackedStack.isEmpty()) return new Tuple<>(trackedStack, false);
                }
            }
        }
        return null;
    }
}
