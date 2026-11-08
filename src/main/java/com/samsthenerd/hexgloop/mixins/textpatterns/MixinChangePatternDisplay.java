package com.samsthenerd.hexgloop.mixins.textpatterns;

// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — iota display/deserialization moved to IotaType codecs;
// PatternIota$1#display(Tag) no longer exists.

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.samsthenerd.hexgloop.screens.PatternStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;

@Mixin(targets = "at.petrak.hexcasting.api.casting.iota.PatternIota$1")
public abstract class MixinChangePatternDisplay {
    @Inject(method = "display(Lnet/minecraft/nbt/Tag;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true)
    public void makePatternIotaVisual(Tag tag, CallbackInfoReturnable<Component> cir) {
        HexPattern pattern = null;
        if (at.petrak.hexcasting.api.casting.iota.IotaType.TYPED_CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().orElse(null) instanceof PatternIota pIota) {
            pattern = pIota.getPattern();
        }
        if (pattern == null) return;
        Style patternStyle = ((PatternStyle) Style.EMPTY.withBold(true).withStrikethrough(true).withColor(ChatFormatting.WHITE)).withPattern(pattern);
        Component originalText = PatternIota.display(pattern); // get the original text so we can keep the string the same
        String originalString = originalText.getString();
        MutableComponent patternTextStyled = Component.literal(originalString.substring(0, 1)).setStyle(patternStyle);
        String hiddenString = originalString.substring(1);
        patternTextStyled.append(Component.literal(hiddenString).setStyle(((PatternStyle) patternStyle).withHidden(true)));

        cir.setReturnValue(patternTextStyled);
    }
}
