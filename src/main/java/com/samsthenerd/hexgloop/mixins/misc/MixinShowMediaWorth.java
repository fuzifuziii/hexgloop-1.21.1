package com.samsthenerd.hexgloop.mixins.misc;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.utils.ClientUtils;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

@Mixin(ItemStack.class)
public class MixinShowMediaWorth {
    
    private static DecimalFormat DUST_AMOUNT = new DecimalFormat("###,###.##");

    @Inject(method="getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
    at=@At("RETURN"))
    public void addMediaWorthTooltip(net.minecraft.world.item.Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir){
        ItemStack stack = (ItemStack)(Object)this;
        ADMediaHolder holder = IXplatAbstractions.INSTANCE.findMediaHolder(stack);
        ItemStack singleStack = stack.copy();
        singleStack.setCount(1);
        ADMediaHolder singleHolder = IXplatAbstractions.INSTANCE.findMediaHolder(singleStack);
        if(holder != null && holder.canConstructBattery()){
            long media = holder.getMedia();
            MutableComponent mediaAmount = Component.translatable("hexcasting.tooltip.media", Component.literal(DUST_AMOUNT.format(media / (float) MediaConstants.DUST_UNIT)));
            mediaAmount.withStyle(style -> style.withColor(ItemMediaHolder.HEX_COLOR));
            Component mediaText = Component.translatable("hexgloop.tooltip.media_worth", mediaAmount);
            cir.getReturnValue().add(mediaText);
        }
        if(stack.getCount() > 1 && singleHolder != null && singleHolder.canConstructBattery()){
            long media = singleHolder.getMedia();
            MutableComponent mediaAmount = Component.translatable("hexcasting.tooltip.media", Component.literal(DUST_AMOUNT.format(media / (float) MediaConstants.DUST_UNIT)));
            mediaAmount.withStyle(style -> style.withColor(ItemMediaHolder.HEX_COLOR));
            MutableComponent mediaText = Component.translatable("hexgloop.tooltip.media_worth_each", mediaAmount);
            mediaText.withStyle(style -> style.withItalic(true).withColor(ChatFormatting.GRAY));
            cir.getReturnValue().add(mediaText);
        }
    }

    private static String FULL_CHAR = "|";
    // private static String FULL_CHAR = "█";

    @Inject(method="getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
    at=@At("RETURN"))
    public void addPigmentDisplayTooltip(net.minecraft.world.item.Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir){
        ItemStack stack = (ItemStack)(Object)this;
        if(stack.getItem() instanceof PigmentItem pigment){
            // if(pigment instanceof ItemDyeColorizer dyePigment){
            //     dyePigment.getDyeColor();
            // }
            UUID uuid = new UUID(0,0);
            float time = 0;
            if(player != null){
                uuid = player.getUUID();
            }
            if(Platform.getEnvironment() == Env.CLIENT){
                time = ClientUtils.getClientTime();
            }
            MutableComponent colorBar = Component.literal("");
            int steps = 16;
            for(int i = 0; i < steps; i++){
                float progress = (float)i / (float)steps;
                int color = pigment.provideColor(stack, uuid).getColor(time, new Vec3(0, -4 + progress*8, 0));
                colorBar.append(Component.literal(FULL_CHAR).withStyle(style -> style.withColor(color)));
            }
            MutableComponent pigmentText = Component.translatable("hexgloop.tooltip.pigment", colorBar);
            pigmentText.withStyle(style -> style.withColor(ChatFormatting.GRAY));
            cir.getReturnValue().add(pigmentText);
        }
    }
}
