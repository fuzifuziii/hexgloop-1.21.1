package com.samsthenerd.hexgloop.mixins.textpatterns;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;

// 1.21 / Hex 0.12: IotaType.display(Tag) and ListIota$1 are gone; display() now lives on
// ListIota itself and builds the text from Iota objects. The comma-removal half of the old
// mixin is also dead: ListIota.display() only appends "," when usesListCommas() is true for
// both neighbours, and PatternIota's type returns false, so pattern-pattern pairs already
// get no comma. Only the click-to-copy behaviour is kept, rewritten over Iota objects.
@Mixin(ListIota.class)
public class MixinChangeListDisplay {
    @Inject(method = "display()Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
    public void copyfullText(CallbackInfoReturnable<Component> cir){
        ListIota self = (ListIota)(Object)this;
        String copyText = getCopyText(self);
        MutableComponent listText = cir.getReturnValue().copy();
        Style clickEventStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText));
        listText.setStyle(clickEventStyle.applyTo(listText.getStyle()));
        cir.setReturnValue(listText);
    }

    public String getCopyText(Iota iota){
        if(iota instanceof ListIota listIota){
            StringBuilder copyText = new StringBuilder("[");
            var list = listIota.getList();
            for (int i = 0; i < list.size(); i++) {
                copyText.append(getCopyText(list.get(i)));
                if (i < list.size() - 1) {
                    copyText.append(", ");
                }
            }
            copyText.append("]");
            return copyText.toString();
        } else if(iota instanceof PatternIota patternIota){
            HexPattern pattern = patternIota.getPattern();
            if(pattern == null) return "";
            return "<" + pattern.getOrientation().toString().replace("_", "").toLowerCase() + "," + pattern.getSignature().toAnglesString() + ">";
        } else {
            return iota.display().getString();
        }
    }
}
