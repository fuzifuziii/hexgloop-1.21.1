package com.samsthenerd.hexgloop.mixins.wnboi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(MsgShiftScrollC2S.class)
public interface MixinScrollbookInvoker {
    @Invoker("spellbook")
    public void invokeHandleSpellbook(ServerPlayer sender, InteractionHand hand, ItemStack stack, double delta);
}
