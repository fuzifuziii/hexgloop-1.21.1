package com.samsthenerd.hexgloop.mixins.booktweaks;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.patchouli.client.book.template.component.ComponentText;

@Mixin(ComponentText.class)
public interface MixinClientGetTemplateText {
    @Accessor("actualText")
    Component getActualText();
}
