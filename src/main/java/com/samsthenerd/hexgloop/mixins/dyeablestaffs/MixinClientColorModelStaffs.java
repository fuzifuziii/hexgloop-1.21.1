package com.samsthenerd.hexgloop.mixins.dyeablestaffs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.samsthenerd.hexgloop.misc.StaffColorLoader;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.items.ItemStaff;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStaff.class)
public class MixinClientColorModelStaffs {
    @Inject(method ="<init>(Lnet/minecraft/world/item/Item$Properties;)V", at=@At("RETURN"))
    public void injectProviders(Item.Properties settings, CallbackInfo ci){
        ItemStaff staff = (ItemStaff)(Object)this;
        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if(tintIndex == 1){
                return StaffColorLoader.getStaffColor(stack);
            }
            return 0xFFFFFFFF;
        }, staff);

        ItemPropertiesRegistry.register(staff, ResourceLocation.parse("hexgloop:is_casting"),
            (ItemStack itemStack, ClientLevel clientWorld, LivingEntity livingEntity, int i) -> {
            if(Minecraft.getInstance().screen instanceof GuiSpellcasting && livingEntity != null && livingEntity.isHolding(itemStack::equals)){
                return 1.0F;
            }
            return 0.0F;
        });
    }
}
