package com.samsthenerd.hexgloop.items;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

public class ItemCastingPotion extends ItemPackagedHex{
    // legacy name from the 1.19 version; in Hex 0.12 the pigment lives in the hexcasting PIGMENT data component
    public static String TAG_COLORIZER = "colorizer";
    public static final UUID zeroUuid = new UUID(0,0);

    public ItemCastingPotion(Properties settings){
        super(settings);
    }

    @Override
    public int cooldown(){
        return 0;
    }

    @Override
    public boolean canDrawMediaFromInventory(ItemStack stack) {
        return false;
    }

    @Override
    public boolean breakAfterDepletion() {
        return false;
    }

    public ItemStack withColorizer(ItemStack existingStack, FrozenPigment colorizer){
        ItemStack newStack = existingStack.copy();
        newStack.set(HexDataComponents.PIGMENT.get(), colorizer);
        return newStack;
    }

    public FrozenPigment getColorizer(ItemStack stack){
        return getPigment(stack);
    }

    // shibva's idea for syncing pigment to player
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if(world.isClientSide) return;
        FrozenPigment pigment = getPigment(stack);
        if(pigment != null && zeroUuid.equals(pigment.owner())){
            stack.set(HexDataComponents.PIGMENT.get(), new FrozenPigment(pigment.item(), entity.getUUID()));
        }
    }

    // make it work like a potion
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if(!hasHex(user.getItemInHand(hand))){
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        Player playerEntity = user instanceof Player ? (Player)user : null;
        ItemStack oldStack = stack.copy();
        if (playerEntity instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)playerEntity, stack);
        }

        if (playerEntity != null) {
            FrozenPigment storedPigment = IXplatAbstractions.INSTANCE.getPigment(playerEntity);
            FrozenPigment potionPigment = getColorizer(stack);
            if(potionPigment == null) potionPigment = storedPigment;
            IXplatAbstractions.INSTANCE.setPigment(playerEntity, potionPigment);
            super.use(world, playerEntity, user.getUsedItemHand());
            FrozenPigment currentPigment = IXplatAbstractions.INSTANCE.getPigment(playerEntity);
            // only set it back if it didn't change from the potion, so we don't break hexbound's recall pigment
            if(Objects.equals(currentPigment, potionPigment))
                IXplatAbstractions.INSTANCE.setPigment(playerEntity, storedPigment);

            playerEntity.awardStat(Stats.ITEM_USED.get(this));
            if (!playerEntity.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        if (playerEntity == null || !playerEntity.getAbilities().instabuild) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (playerEntity != null) {
                playerEntity.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                // need to make sure it still has media if it was in a stack
                setMedia(stack, getMaxMedia(oldStack));
            }
        }
        user.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        if(hasHex(stack))
            return 32;
        return 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if(hasHex(stack))
            return UseAnim.DRINK;
        return UseAnim.NONE;
    }
}
