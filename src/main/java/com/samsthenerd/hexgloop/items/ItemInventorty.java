package com.samsthenerd.hexgloop.items;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.casting.IContextHelper;
import com.samsthenerd.hexgloop.casting.MishapThrowerWrapper;
import com.samsthenerd.hexgloop.casting.inventorty.InventortyUtils.KittyContext;
import com.samsthenerd.hexgloop.mixins.misc.MixinExposeCursorStackRef;

import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;

public class ItemInventorty extends ItemPackagedHex{
    public static final List<SoundEvent> POSSIBLE_CAT_NOISES = List.of(SoundEvents.CAT_PURR, SoundEvents.CAT_PURREOW);

    public ItemInventorty(Properties settings){
        super(settings);
    }

    public boolean canDrawMediaFromInventory(ItemStack stack){
        return false;
    }

    public boolean breakAfterDepletion(){
        return false;
    }

    // for hexxycraft backport
    public int cooldown(){
        return 0;
    }

    // don't hand cast
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    // inventorty is in the slot
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if(player.getCooldowns().isOnCooldown(this)|| clickType != ClickAction.SECONDARY || !hasHex(stack)){
            return false;
        }

        kittyCast(stack, otherStack, slot, clickType, player, cursorStackReference, false);

        return true;
    }

    // inventorty is in the hand
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
        if(player.getCooldowns().isOnCooldown(this) || clickType != ClickAction.SECONDARY || !hasHex(stack)){
            return false;
        }
        SlotAccess maybeCursorStackRef = null;
        if(player.containerMenu instanceof MixinExposeCursorStackRef cursorHandler){
            maybeCursorStackRef = cursorHandler.invokeGetCursorStackReference();
        }
        kittyCast(stack, slot.getItem(), slot, clickType, player, maybeCursorStackRef, true);
        return true;
    }

    // mostly yoinked from ItemPackagedHex#use
    public void kittyCast(ItemStack tortyStack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess stackRef, boolean tortyInHand){
        if(!(player.level() instanceof ServerLevel sWorld)) return;

        List<Iota> instrs = getHex(tortyStack, sWorld);
        if (instrs == null) {
            return;
        }
        var sPlayer = (ServerPlayer) player;
        // Hex 0.12: CastingEnvironment is abstract; packaged hexes cast through PackagedItemCastEnv + CastingVM.empty
        var ctx = new PackagedItemCastEnv(sPlayer, InteractionHand.MAIN_HAND);
        ((IContextHelper)(Object)ctx).setCursorRef(stackRef);
        ((IContextHelper)(Object)ctx).setKitty(tortyStack);
        KittyContext kCtx = new KittyContext(player.getInventory());
        for(Slot slotToTrack : player.containerMenu.slots){
            kCtx.addSlot(slotToTrack);
        }
        for(int i = 0; i < kCtx.getInventoryCount(); i++){
            HexGloop.logPrint("\t" + (kCtx.getInventory(i) instanceof Nameable nameableInv ? nameableInv.getName() : kCtx.getInventory(i) + ": " + kCtx.getSlots(i).size() + " slots"));
        }
        ((IContextHelper)(Object)ctx).setKittyContext(kCtx);
        var harness = CastingVM.empty(ctx);
        harness.setImage(MishapThrowerWrapper.withStack(harness.getImage(), TreeList.from(List.of(new BooleanIota(tortyInHand), new DoubleIota(kCtx.getFullSlotIndex(slot))))));

        sWorld.playSound(sPlayer, sPlayer.blockPosition(),
            POSSIBLE_CAT_NOISES.get((int)(sPlayer.getRandom().nextIntBetweenInclusive(0, POSSIBLE_CAT_NOISES.size()-1))),
            SoundSource.AMBIENT, 0.2F + 0.3F * (sPlayer.getRandom().nextFloat() - sPlayer.getRandom().nextFloat()), 1.0F);

        harness.queueExecuteAndWrapIotas(instrs, sWorld);

        player.awardStat(Stats.ITEM_USED.get(this));

        sPlayer.getCooldowns().addCooldown(this, 5);
    }
}
