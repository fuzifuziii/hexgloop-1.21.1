package com.samsthenerd.hexgloop.mixins.textpatterns;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.samsthenerd.hexgloop.screens.PatternStyle;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.RenderLib;
import at.petrak.hexcasting.common.lib.HexItems;
import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;


// heavy influence/mild copying from https://github.com/Snownee/TextAnimator/blob/1.19.2-fabric/src/main/java/snownee/textanimator/mixin/StyleMixin.java
// note that all of style's style.withProperty methods won't preserve the pattern
@Mixin(Style.class)
public class MixinPatternStyle implements PatternStyle{

    private HexPattern pattern = null;
    private List<Vec2> zappyPoints = null;
    private List<Vec2> pathfinderDots = null;
    private float patScale; // maybe want to have this exposed somewhere?
    private boolean _isHidden = false;

    private static final String PATTERN_KEY = "hexPatternStyle";
    private static final String PATTERN_START_DIR_KEY = "startDir";
    private static final String PATTERN_ANGLE_SIG_KEY = "angleSig";
    private static final String PATTERN_HIDDEN_KEY = "isHidden";
    private static final float RENDER_SIZE = 128f;

    @Override
    public HexPattern getPattern() {
        return pattern;
    }

    @Override
    public boolean isHidden(){
        return _isHidden;
    }

    @Override
    public Style setPattern(HexPattern pattern) {
        // yoinked from PatternTooltipComponent
        this.pattern = pattern;
        Pair<Float, List<Vec2> > pair = RenderLib.getCenteredPattern(pattern, RENDER_SIZE, RENDER_SIZE, 16f);
        this.patScale = pair.getFirst();
        List<Vec2> dots = pair.getSecond();
        this.zappyPoints = RenderLib.makeZappy(
            dots, RenderLib.findDupIndices(pattern.positions()),
            10, 0.8f, 0f, 0f, RenderLib.DEFAULT_READABILITY_OFFSET, RenderLib.DEFAULT_LAST_SEGMENT_LEN_PROP,
            0.0);
        this.pathfinderDots = dots;
        return (Style)(Object)this;
    }

    @Override
    public Style withPattern(HexPattern pattern, boolean withPatternHoverEvent, boolean withPatternClickEvent) {
        Style style = (Style)(Object)this;

        if (withPatternHoverEvent) {
            StringBuilder bob = new StringBuilder();
            bob.append(pattern.getOrientation());
            var sig = pattern.getSignature().toAnglesString();
            if (!sig.isEmpty()) {
                bob.append(" ");
                bob.append(sig);
            }
            Component hoverText = Component.translatable("hexcasting.tooltip.pattern_iota",
                Component.literal(bob.toString())).setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD));
            ItemStack scrollStack = new ItemStack(HexItems.SCROLL_LARGE.get());
            scrollStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, hoverText);
            HexItems.SCROLL_LARGE.get().writeDatum(scrollStack, new PatternIota(pattern));
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(scrollStack)));
        }
        if(withPatternClickEvent){
            style = style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "<" + 
                pattern.getOrientation().toString().replace("_", "").toLowerCase() + "," + pattern.getSignature().toAnglesString() + ">"));
        }
        return style.applyTo(PatternStyle.fromPattern(pattern));
    }

    
    @Override
    public Style setHidden(boolean hidden){
        this._isHidden = hidden;
        return (Style)(Object)this;
    }

    @Override
    public Style withHidden(boolean hidden){
        return ((Style)(Object)this).applyTo(((PatternStyle)Style.EMPTY.withBold(null)).setHidden(hidden));
    }

    @Override
    public List<Vec2> getZappyPoints(){
        return zappyPoints;
    }

    @Override
    public List<Vec2> getPathfinderDots(){
        return pathfinderDots;
    }

    @Inject(at=@At("TAIL"), method="<init>(Lnet/minecraft/network/chat/TextColor;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Lnet/minecraft/network/chat/ClickEvent;Lnet/minecraft/network/chat/HoverEvent;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;)V")
    private void HexPatDefaultStyleConstructor(@Nullable TextColor color, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough, @Nullable Boolean obfuscated, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String insertion, @Nullable ResourceLocation font, CallbackInfo cinfo){
        this.pattern = null;
        this.zappyPoints = null;
        this.pathfinderDots = null;
        this._isHidden = false;
        this.patScale = 1f;
    }

    @Inject(method = "applyTo", at = @At("RETURN"), cancellable = true)
	private void HexPatStyWithParent(Style parent, CallbackInfoReturnable<Style> cir) {
        Style rstyle = cir.getReturnValue();
        if(this.getPattern() != null){
            ((PatternStyle) rstyle).setPattern(this.getPattern());
        } else { // no pattern on this style, try falling back to inherit parent
            HexPattern parentPattern = ((PatternStyle) parent).getPattern();
            if(parentPattern != null){
                ((PatternStyle) rstyle).setPattern(parentPattern);
            }
        }
        // i guess?
        if(this.isHidden() || ((PatternStyle) parent).isHidden()){
            ((PatternStyle) rstyle).setHidden(true);
        }
		cir.setReturnValue(rstyle);
	}

	@Inject(method = "equals", at = @At("HEAD"), cancellable = true)
	private void HexPatStyEquals(Object obj, CallbackInfoReturnable<Boolean> cir) {
		if (this != obj && (obj instanceof PatternStyle style)) {
			if (!Objects.equals(this.getPattern(), style.getPattern())) {
				cir.setReturnValue(false);
			}
            if(this.isHidden() != style.isHidden()){
                cir.setReturnValue(false);
            }
		}
	}

	// TODO(1.21 port): DISABLED in hexgloop-common.mixins.json — Style.Serializer is codec-based now, there is no
	// Gson deserialize/serialize to hook. Custom style JSON needs a MapCodec wrapper instead.
	@Mixin(Style.Serializer.class)
	public static class MixinPatternStyleSerializer {
		@ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
		private Style HexPatStyDeserialize(Style initialStyle, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			if (!jsonElement.isJsonObject() || initialStyle == null) {
				return initialStyle;
			}
			JsonObject json = jsonElement.getAsJsonObject();
			if (!json.has("hexPatternStyle")) {
				return initialStyle;
			}
            Boolean hiddenFromJson = GsonHelper.isBooleanValue(json, PATTERN_HIDDEN_KEY) ? GsonHelper.getAsBoolean(json, PATTERN_HIDDEN_KEY) : false;
            
            JsonObject patternObj = GsonHelper.getAsJsonObject(json, PATTERN_KEY);
            
            String startDirString = GsonHelper.isStringValue(patternObj, PATTERN_START_DIR_KEY) ? GsonHelper.getAsString(patternObj, PATTERN_START_DIR_KEY) : null;
            String angleSigString = GsonHelper.isStringValue(patternObj, PATTERN_ANGLE_SIG_KEY) ? GsonHelper.getAsString(patternObj, PATTERN_ANGLE_SIG_KEY) : null;

            if(startDirString == null || angleSigString == null) return initialStyle;

            HexDir startDir = HexDir.fromString(startDirString);
            HexPattern pattern = HexPattern.fromAngleString(angleSigString, startDir);
            return ((PatternStyle)(Object)((PatternStyle)(Object)initialStyle).withPattern(pattern)).setHidden(hiddenFromJson);
		}

		@ModifyReturnValue(method = "serialize", at = @At("RETURN"))
		private JsonElement HexPatStySerialize(JsonElement jsonElement, Style style, Type type, JsonSerializationContext jsonSerializationContext) {
			PatternStyle pStyle = (PatternStyle) style;
			if (jsonElement == null || !jsonElement.isJsonObject() || pStyle.getPattern() == null) {
				return jsonElement;
			}
			JsonObject json = jsonElement.getAsJsonObject();
            json.add(PATTERN_HIDDEN_KEY, new JsonPrimitive(pStyle.isHidden()));
            JsonObject patternObj = new JsonObject();
            patternObj.addProperty(PATTERN_START_DIR_KEY, pStyle.getPattern().getOrientation().toString());
            patternObj.addProperty(PATTERN_ANGLE_SIG_KEY, pStyle.getPattern().getSignature().toAnglesString());
			json.add(PATTERN_KEY, patternObj);
            return json;
		}
	}

    // meant to be called at the 
    private Style keepPattern(Style returnedStyle){
        PatternStyle pStyle = (PatternStyle)(Object)this;
        if(pStyle.getPattern() != null){
            ((PatternStyle) returnedStyle).setPattern(pStyle.getPattern());
        }
        if(pStyle.isHidden()){
            ((PatternStyle) returnedStyle).setHidden(true);
        }
        return returnedStyle;
    }

    @Inject(method = "withColor(Lnet/minecraft/network/chat/TextColor;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithColor(TextColor color, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withBold(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithBold(Boolean boldBool, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }
    
    @Inject(method = "withItalic(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithItalic(Boolean boldBool, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withUnderlined(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithUnderline(Boolean boldBool, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withStrikethrough(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithStrikethrough(Boolean boldBool, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withObfuscated(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithObfuscated(Boolean boldBool, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withClickEvent(Lnet/minecraft/network/chat/ClickEvent;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithClickEvent(ClickEvent clickEvent, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withHoverEvent(Lnet/minecraft/network/chat/HoverEvent;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithHoverEvent(HoverEvent hoverEvent, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withInsertion(Ljava/lang/String;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithInsertion(String insertionString, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "withFont(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithFont(ResourceLocation fontID, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "applyFormat(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithFormatting(ChatFormatting formatting, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }

    @Inject(method = "applyLegacyFormat(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/Style;",
    at=@At("RETURN"), cancellable = true)
    private void fixWithExclusiveFormatting(ChatFormatting formatting, CallbackInfoReturnable<Style> cir){
        cir.setReturnValue(keepPattern(cir.getReturnValue()));
    }




}
