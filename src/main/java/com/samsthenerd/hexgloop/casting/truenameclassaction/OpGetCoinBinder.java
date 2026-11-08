package com.samsthenerd.hexgloop.casting.truenameclassaction;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.compat.moreIotas.MoreIotasMaybeIotas;
import com.samsthenerd.hexgloop.items.HexGloopItems;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.OperatorUtils;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem;
import dev.architectury.platform.Platform;

public class OpGetCoinBinder implements ConstMediaAction {
    boolean checkEquals;
    boolean isCooler; // for whether we get an entity or just from offhand

    public OpGetCoinBinder(boolean checkEquals){
        this(checkEquals, false);
    }

    public OpGetCoinBinder(boolean checkEquals, boolean isCooler){
        this.checkEquals = checkEquals;
        this.isCooler = isCooler;
    }

    @Override
    public int getArgc(){ 
        int argSum = 0;
        if(isCooler) argSum += 1; // get the entity
        if(checkEquals) argSum += 1; // get the thing to compare to
        return argSum;
    }

    @Override
    public long getMediaCost(){
        return 0;
    }





    @Override
    public List<Iota> execute(List<? extends Iota> args, CastingEnvironment context){
        ServerPlayer player = (ServerPlayer) context.getCastingEntity();
        ItemStack maybeCoinStack;
        if(!isCooler){
            CastingEnvironment.HeldItemInfo result = context.getHeldItemToOperateOn(stack -> {
                return stack.getItem() == HexGloopItems.CASTERS_COIN.get();
            });
            maybeCoinStack = result.stack();
            if(maybeCoinStack.getItem() != HexGloopItems.CASTERS_COIN.get()){
                MishapThrowerWrapper.throwMishap(new MishapBadOffhandItem(maybeCoinStack, Component.translatable("hexcasting.mishap.bad_offhand_item.caster_bound")));
            }
        } else {
            ItemEntity ent = OperatorUtils.getItemEntity(args, context.getWorld(), checkEquals ? 1 : 0, getArgc());
            maybeCoinStack = ent.getItem();
            if(maybeCoinStack.getItem() != HexGloopItems.CASTERS_COIN.get()){
                MishapThrowerWrapper.throwMishap(new MishapBadItem(ent, Component.translatable("hexcasting.mishap.bad_offhand_item.caster_bound")));
            }
        }

        Tuple<UUID, String> casterInfo = HexGloopItems.CASTERS_COIN.get().getBoundCaster(maybeCoinStack);

        if(checkEquals){
            Entity ent = OperatorUtils.getEntity(args, context.getWorld(), 0, getArgc());
            if(casterInfo == null) return List.of(new BooleanIota(false));
            return List.of(new BooleanIota(casterInfo.getA().equals(ent.getUUID())));
        } else if(Platform.isModLoaded("moreiotas") && casterInfo != null){
            return List.of(MoreIotasMaybeIotas.makeStringIota(casterInfo.getB()));
        } else {
            return List.of(new NullIota());
        }
    }

}