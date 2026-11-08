package com.samsthenerd.hexgloop.items;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.casting.mirror.SyncedItemHandling;
import com.samsthenerd.hexgloop.items.tooltips.MirrorTooltipData;
import com.samsthenerd.hexgloop.misc.HexGloopTags;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ItemHandMirror extends ItemAbstractPassThrough implements IotaHolderItem {

    public static final ResourceLocation MIRROR_ACTIVATED_PRED = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "is_mirror_activated");
    public static final String MIRROR_ACTIVATED_TAG = "is_mirror_activated";
    public static final String ITEM_DISPLAY_TAG = "item_in_mirror_desc";
    public static final String ITEM_UUID_TAG = "item_in_mirror_uuid";
    public static final String ITEM_DIM_TAG = "item_dimension";

    public ItemHandMirror(Properties settings){
        super(settings);
    }

    // same key hex's focus used in 1.19 (ItemFocus.TAG_DATA no longer exists)
    public static final String TAG_DATA = "data";

    @Override
    @Nullable
    public Iota readIota(ItemStack stack){
        return StackNBT.getIota(stack, TAG_DATA);
    }

    @Override
    public boolean writeable(ItemStack stack){
        return true;
    }

    // Hex 0.12: EntityIota.getEntity needs a ServerLevel, so look the entity up across all server levels
    @Nullable
    private static Entity resolveEntity(EntityIota entityIota){
        var server = ServerLifecycleHooks.getCurrentServer();
        if(server == null) return null;
        for(ServerLevel lvl : server.getAllLevels()){
            Entity ent = entityIota.getEntity(lvl);
            if(ent != null) return ent;
        }
        return null;
    }

    // registries needed to (de)serialize stacks into nbt: server if we have one, otherwise the client level
    private static HolderLookup.Provider registries(){
        var server = ServerLifecycleHooks.getCurrentServer();
        if(server != null) return server.registryAccess();
        return ClientRegistries.get();
    }

    private static class ClientRegistries {
        static HolderLookup.Provider get(){
            var level = net.minecraft.client.Minecraft.getInstance().level;
            return level == null ? RegistryAccess.EMPTY : level.registryAccess();
        }
    }

    public boolean canWrite(ItemStack stack, @Nullable Iota iota){
        if(iota == null){
            return true; // ? i suppose? unless it's already empty
        }
        if(iota instanceof EntityIota entityIota && resolveEntity(entityIota) instanceof ItemEntity itemEnt){
            return true;
        }
        return false;
    }

    public ItemStack getStoredItem(ItemStack stack, LivingEntity ent, Level world, InteractionHand hand){
        ItemStack storedStack = null;
        if(world instanceof ServerLevel sWorld){
            storedStack = SyncedItemHandling.getAlternateHandStack(ent, hand, (CastingEnvironment)null);
        } else {
            // client side - should just be visual-ish ?
            if(!isMirrorActivated(stack)) return null;
            storedStack = getMirroredItemStack(stack);
        }
        if(storedStack != null && storedStack.is(HexGloopTags.DONT_USE_AS_PASSTHROUGH)){
            return null;
        }
        return storedStack;
    }

    // returns the itemstack that should be in the player's hand after the use, just if you need to modify it or something
    public ItemStack setStoredItem(ItemStack stack, LivingEntity ent, Level world, InteractionHand hand, ItemStack storedItem){
        if(world instanceof ServerLevel sWorld && !(ent.level().isClientSide())){
            ItemStack tempStack = ent.getItemInHand(hand); 
            ent.setItemInHand(hand, stack);
            ItemEntity itemEnt = SyncedItemHandling.getAlternateEntity(ent, hand, (CastingEnvironment)null);
            // HexGloop.logPrint("on server trying to set stored item");
            if(itemEnt != null){
                // HexGloop.logPrint("on server found item entity");
                itemEnt.setItem(storedItem);
            }
            ent.setItemInHand(hand, tempStack); // avoid modifying the hand here
        };
        return stack;
    }

    public ItemStack getStoredItemCopy(ItemStack stack){
        return getMirroredItemStack(stack);
    }

    public boolean shouldPassTools(ItemStack stack){
        return true;
    }

    public void writeDatum(ItemStack stack, @Nullable Iota iota){
        if(iota == null){
            StackNBT.remove(stack, TAG_DATA);
            StackNBT.remove(stack, ITEM_DISPLAY_TAG);
            StackNBT.remove(stack, ITEM_UUID_TAG);
            StackNBT.remove(stack, ITEM_DIM_TAG);
            StackNBT.putBoolean(stack, MIRROR_ACTIVATED_TAG, false);
        }
        if(iota instanceof EntityIota entityIota && resolveEntity(entityIota) instanceof ItemEntity itemEnt){
            StackNBT.putIota(stack, TAG_DATA, iota);
            StackNBT.putUUID(stack, ITEM_UUID_TAG, itemEnt.getUUID());
            StackNBT.putString(stack, ITEM_DIM_TAG, itemEnt.level().dimension().location().toString());
            if(itemEnt.getItem() != null){
                StackNBT.put(stack, ITEM_DISPLAY_TAG, itemEnt.getItem().saveOptional(itemEnt.level().registryAccess()));
                StackNBT.putBoolean(stack, MIRROR_ACTIVATED_TAG, true);
            } else {
                StackNBT.putBoolean(stack, MIRROR_ACTIVATED_TAG, false);
            }
        }
    }

    public boolean isMirrorActivated(ItemStack stack){
        if(StackNBT.contains(stack, MIRROR_ACTIVATED_TAG) && StackNBT.contains(stack, TAG_DATA)){
            return StackNBT.getBoolean(stack, MIRROR_ACTIVATED_TAG);
        }
        return false;
    }

    public ItemStack getMirroredItemStack(ItemStack stack){
        if(StackNBT.contains(stack, ITEM_DISPLAY_TAG)){
            return ItemStack.parseOptional(registries(), StackNBT.getCompound(stack, ITEM_DISPLAY_TAG));
        }
        return ItemStack.EMPTY;
    }

    // where it's stored
    public ResourceKey<Level> getMirroredItemDimension(ItemStack stack){
        if(StackNBT.hasString(stack, ITEM_DIM_TAG)){
            return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(StackNBT.getString(stack, ITEM_DIM_TAG)));
        }
        return null;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPYGLASS;
    }

    // to update the thingy
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
        if(world instanceof ServerLevel sWorld){
            if(StackNBT.hasUUID(stack, ITEM_UUID_TAG)){
                UUID uuid = StackNBT.getUUID(stack, ITEM_UUID_TAG);
                ServerLevel itemWorld = sWorld.getServer().getLevel(getMirroredItemDimension(stack));
                if(itemWorld == null) itemWorld = sWorld;
                Entity ent = itemWorld.getEntity(uuid);
                if(ent instanceof ItemEntity itemEnt){
                    if(itemEnt.getItem() != null){
                        StackNBT.put(stack, ITEM_DISPLAY_TAG, itemEnt.getItem().saveOptional(sWorld.registryAccess()));
                        StackNBT.putBoolean(stack, MIRROR_ACTIVATED_TAG, true);
                    } else {
                        // possible that calling putBoolean every tick is bad,, but i figure it's better than doing a check every tick ?
                        StackNBT.putBoolean(stack, MIRROR_ACTIVATED_TAG, false);
                    }
                } else {
                    StackNBT.putBoolean(stack, MIRROR_ACTIVATED_TAG, false);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag context) {
        ItemStack descStack = getMirroredItemStack(stack);
        if(descStack.isEmpty()) return;
        boolean activated = isMirrorActivated(stack);
        MutableComponent itemDesc = descStack.getHoverName().copy();
        Style itemStyle = itemDesc.getStyle();
        itemStyle = itemStyle.withItalic(false).withColor(ChatFormatting.AQUA);
        itemDesc.setStyle(itemStyle);
        MutableComponent tipText = Component.translatable(
                activated ? "item.hexgloop.hand_mirror.tooltip" : "item.hexgloop.hand_mirror.tooltip.not_activated", 
                itemDesc);
        Style style = tipText.getStyle();
        style = style.withItalic(true).withColor(ChatFormatting.GRAY);
        tipText.setStyle(style);
        tooltip.add(tipText);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ItemStack storedStack = getMirroredItemStack(stack).copy();
        // don't show if there's nothing to show
        if(storedStack.isEmpty()) return Optional.empty();
        if(isMirrorActivated(stack)){ // have this be mostly shown
                StackNBT.putDouble(storedStack, SyncedItemHandling.IS_REFLECTED_TAG, 1);
            } else { // vs this be much more transparent
                StackNBT.putDouble(storedStack, SyncedItemHandling.IS_REFLECTED_TAG, 2);
            }
        return Optional.of(new MirrorTooltipData(storedStack));
    }
}
