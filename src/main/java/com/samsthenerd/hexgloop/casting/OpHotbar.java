package com.samsthenerd.hexgloop.casting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.casting.mirror.IPlayerPTUContext;
import com.samsthenerd.hexgloop.casting.mirror.MishapICanOnlyCodeSoMuchPlsDontDupe;
import com.samsthenerd.hexgloop.items.ItemAbstractPassThrough.PassThroughUseContext;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;

public class OpHotbar implements ConstMediaAction{
    // should probably just implement these as separate op classes but bleh
    private boolean isFlip; // if it's swapping main hand with offhand
    private boolean isRead; // if it's reading the hotbar or setting it

    public OpHotbar(boolean isFlip, boolean isRead){
        this.isFlip = isFlip;
        this.isRead = isRead;
    }

    @Override
    public int getArgc(){ return isFlip || isRead ? 0 : 1;}

    @Override
    public long getMediaCost(){
        return isFlip || !isRead ? MediaConstants.DUST_UNIT / 8 : 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        ServerPlayer player = (ServerPlayer) context.getCastingEntity();
        PassThroughUseContext<?,?> ptuContext = ((IPlayerPTUContext)(Object)player).getPTUContext();
        if(isFlip){
            if(ptuContext != null){
                // mishap for dupe reasons / stability reasons
                MishapThrowerWrapper.throwMishap(new MishapICanOnlyCodeSoMuchPlsDontDupe());
                return new ArrayList<>();
            }
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            player.setItemInHand(InteractionHand.MAIN_HAND, offHand);
            player.setItemInHand(InteractionHand.OFF_HAND, mainHand);
            if(ptuContext != null && ptuContext.ptSlot == player.getInventory().selected){
                ptuContext.ptSlot = Inventory.SLOT_OFFHAND;
            } else if(ptuContext != null && ptuContext.ptSlot == Inventory.SLOT_OFFHAND){
                ptuContext.ptSlot = player.getInventory().selected;
            }
        } else {
            if(isRead){
                int selectedSlot = player.getInventory().selected;
                return List.of(new DoubleIota(selectedSlot));
            } else {
                Double newSlot = OperatorUtils.getDoubleBetween(args, 0, 0, 8, getArgc());
                player.getInventory().selected = newSlot.intValue();
                player.connection.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
            }
        }
        return new ArrayList<>();
    }

    
}


