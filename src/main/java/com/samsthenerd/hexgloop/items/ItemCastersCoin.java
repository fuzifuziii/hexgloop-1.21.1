package com.samsthenerd.hexgloop.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import com.samsthenerd.hexgloop.utils.StackNBT;

public class ItemCastersCoin extends Item implements IotaHolderItem {
    public ItemCastersCoin(Properties properties) {
        super(properties);
    }

    // 0 = empty, 1 = written
    public static final ResourceLocation OVERLAY_PRED = ResourceLocation.fromNamespaceAndPath(HexAPI.MOD_ID, "overlay_layer");

    public static final String TAG_DATA = "data";
    // these may be garbage data if the data tag isn't set !
    public static final String TAG_CASTER = "caster";
    public static final String TAG_CASTER_DISPLAY = "caster_display";

    @Override
    public @Nullable Iota readIota(ItemStack stack) {
        return StackNBT.getIota(stack, TAG_DATA);
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return true;
    }
    
    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + (isBound(stack) ? ".bound" : "");
    }

    public boolean isBound(ItemStack stack) {
        CompoundTag nbt = StackNBT.getCompound(stack, TAG_DATA);
        return nbt != null && !nbt.isEmpty();
    }

    // item stack after it's used 
    public ItemStack useCoin(ItemStack stack){
        if(isBound(stack)){
            stack.shrink(1);
            ItemStack blankCoin = this.getDefaultInstance();
            blankCoin.setCount(1);
            return blankCoin;
        }
        return ItemStack.EMPTY;
    }

    public void setBoundCaster(ItemStack stack, Player player){
        StackNBT.putUUID(stack, TAG_CASTER, player.getUUID());
        StackNBT.putString(stack, TAG_CASTER_DISPLAY, player.getName().getString());
    }

    @Nullable
    public Tuple<UUID, String> getBoundCaster(ItemStack stack){
        if(!isBound(stack)) return null;
        CompoundTag nbt = StackNBT.get(stack);
        if(nbt == null) return null;
        String displayName = "";
        UUID uuid = null;
        if(nbt.hasUUID(TAG_CASTER)){
            uuid = nbt.getUUID(TAG_CASTER);
        }
        if(nbt.contains(TAG_CASTER_DISPLAY, Tag.TAG_STRING)){
            displayName = nbt.getString(TAG_CASTER_DISPLAY);
        }
        return new Tuple<>(uuid, displayName);
    }

    @Override
    public @Nullable Iota emptyIota(ItemStack stack) {
        return new NullIota();
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return true;
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (datum == null) {
            StackNBT.remove(stack, TAG_DATA);
            StackNBT.remove(stack, TAG_CASTER);
            StackNBT.remove(stack, TAG_CASTER_DISPLAY);
        } else {
            StackNBT.putIota(stack, TAG_DATA, datum);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
        CompoundTag nbt = StackNBT.get(pStack);
        if(nbt != null && isBound(pStack) && nbt.contains(TAG_CASTER_DISPLAY, Tag.TAG_STRING)){
            MutableComponent actualCasterText = Component.literal(nbt.getString(TAG_CASTER_DISPLAY));
            Style casterStyle = actualCasterText.getStyle();
            casterStyle = casterStyle.withItalic(true).withColor(ChatFormatting.AQUA);
            actualCasterText.setStyle(casterStyle);
            MutableComponent boundCasterMessage = Component.translatable("item.hexgloop.casters_coin.bound_caster", actualCasterText);
            Style style = boundCasterMessage.getStyle();
            style = style.withItalic(true).withColor(ChatFormatting.GRAY);
            boundCasterMessage.setStyle(style);
            pTooltipComponents.add(boundCasterMessage);
        }
    }
}
