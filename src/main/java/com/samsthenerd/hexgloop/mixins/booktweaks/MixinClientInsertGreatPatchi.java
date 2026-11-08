package com.samsthenerd.hexgloop.mixins.booktweaks;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.samsthenerd.hexgloop.misc.clientgreatbook.GreatBook;
import com.samsthenerd.hexgloop.misc.clientgreatbook.PatternCompGetter;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.resources.ResourceKey;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.RenderLib;
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent;
import at.petrak.hexcasting.interop.utils.PatternDrawingUtil;
import at.petrak.hexcasting.interop.utils.PatternEntry;
import vazkii.patchouli.api.IComponentRenderContext;

// 1.21/Hex 0.12 port notes: render() now takes GuiGraphics instead of PoseStack, and
// AbstractPatternComponent stores a plain List<HexPattern> (the zappy points moved into PatternEntry).
@Mixin(AbstractPatternComponent.class)
public abstract class MixinClientInsertGreatPatchi implements PatternCompGetter {

    @Shadow
    protected transient float hexSize;
    @Shadow
    private transient List<HexPattern> patterns;

    @Shadow
    public boolean showStrokeOrder(){
        throw new AssertionError();
    }

    private HexPattern savedGreatPattern;

    @Inject(
        method="render(Lnet/minecraft/client/gui/GuiGraphics;Lvazkii/patchouli/api/IComponentRenderContext;FII)V",
        at=@At("HEAD")
    )
    public void checkForPatternUpdate(net.minecraft.client.gui.GuiGraphics guiGraphics, IComponentRenderContext ctx, float partialTicks, int mouseX, int mouseY, CallbackInfo info){
        // make sure it's only a lookup pattern with a great pattern
        if(((Object)this) instanceof MixinClientAccessLookupPatternComp lpComp && !lpComp.getStrokeOrder()){
            HexPattern newPattern = GreatBook.INSTANCE.getPattern(lpComp.getOpName());
            if(newPattern != null && !newPattern.equals(savedGreatPattern)){
                savedGreatPattern = newPattern; // save it here
                // just yoink from base
                var data = PatternDrawingUtil.loadPatterns(
                List.of(new Pair<HexPattern, HexCoord>(newPattern, HexCoord.getOrigin())), RenderLib.DEFAULT_READABILITY_OFFSET, RenderLib.DEFAULT_LAST_SEGMENT_LEN_PROP);
                this.hexSize = data.hexSize();
                this.patterns = data.patterns().stream().map(PatternEntry::pattern).toList();
            }
            // restore the original
            if(savedGreatPattern != null && newPattern == null){
                savedGreatPattern = null;
                var entry = IXplatAbstractions.INSTANCE.getActionRegistry().get(ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), lpComp.getOpName()));
                if(entry == null) return;
                var data = PatternDrawingUtil.loadPatterns(
                    List.of(new Pair<>(entry.prototype(), HexCoord.getOrigin())), 0f, 1f);
                this.hexSize = data.hexSize();
                this.patterns = data.patterns().stream().map(PatternEntry::pattern).toList();
            }
        }
    }

    @ModifyExpressionValue(
        method="render(Lnet/minecraft/client/gui/GuiGraphics;Lvazkii/patchouli/api/IComponentRenderContext;FII)V",
        at=@At(value="INVOKE", target="at/petrak/hexcasting/interop/patchouli/AbstractPatternComponent.showStrokeOrder ()Z")
    )
    public boolean modifyShowStrokeOrder(boolean original){
        return original || savedGreatPattern != null;
    }

    public List<HexPattern> getCopyablePatterns(){
        if(savedGreatPattern != null){
            return List.of(savedGreatPattern);
        }
        if(!showStrokeOrder()){
            return new ArrayList<HexPattern>();
        }
        return new ArrayList<>(patterns);
    }
}
