package com.samsthenerd.hexgloop.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;

public class ItemGloopDye extends ItemMediaHolder implements IotaHolderItem{
    public ItemGloopDye(Properties settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if(world.isClientSide) return;
        if(!stack.has(HexDataComponents.MEDIA.get())){
            withMedia(stack, 0, 64*MediaConstants.DUST_UNIT);
        }
    }

    @Override
    public boolean canRecharge(ItemStack stack){
        return true;
    }

    @Override
    public boolean canProvideMedia(ItemStack stack){
        return false;
    }

    public boolean decrementMedia(ItemStack stack, int amt){
        if(getMedia(stack) < amt)
            return false;
        setMedia(stack, getMedia(stack) - amt);
        return true;
    }

    // readIota is inherited from IotaHolderItem (HexDataComponents.IOTA_HOLDER_IOTA)

    @Override
    public boolean writeable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return datum == null || datum.getType() == HexIotaTypes.VEC3;
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (datum == null) {
            stack.remove(HexDataComponents.IOTA_HOLDER_IOTA.get());
        } else if(datum.getType() == HexIotaTypes.VEC3) {
            stack.set(HexDataComponents.IOTA_HOLDER_IOTA.get(), datum);
        }
    }

    public static int getDyeColor(ItemStack stack){
        Iota iota = stack.get(HexDataComponents.IOTA_HOLDER_IOTA.get());
        if(!(iota instanceof Vec3Iota vecIota)){
            return 0xFFFFFF;
        }
        Vec3 rgbVec = vecIota.getVec3();
        return (((int) (rgbVec.x)) << 16) + (((int) (rgbVec.y)) << 8) + ((int) (rgbVec.z));
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pIsAdvanced);
        int argbColor = getDyeColor(pStack);
        Component dyeBadge = Component.literal("◈").withStyle(style -> style.withColor(argbColor));
        Component dyeLabel = Component.literal("<" + ARGB32.red(argbColor) + ", " + ARGB32.green(argbColor) + ", " + ARGB32.blue(argbColor) + ">").withStyle(ChatFormatting.GRAY);
        pTooltipComponents.add(dyeBadge.copy().append(dyeLabel).append(dyeBadge));
    }
}
