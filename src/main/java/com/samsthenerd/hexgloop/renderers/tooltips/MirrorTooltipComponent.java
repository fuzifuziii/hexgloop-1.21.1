package com.samsthenerd.hexgloop.renderers.tooltips;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.items.tooltips.MirrorTooltipData;
import com.samsthenerd.hexgloop.renderers.HUDOverlay;
import com.samsthenerd.hexgloop.utils.GloopyRenderUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

// yoinked and modified from hex

public class MirrorTooltipComponent implements ClientTooltipComponent {

    private static final float RENDER_SIZE = 64f;

    private final ItemStack storedItem;

    public MirrorTooltipComponent(MirrorTooltipData tt) {
        this.storedItem = tt.storedItem();
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics gg) {
        PoseStack ps = gg.pose();
        int pBlitOffset = 0;
        var width = this.getWidth(font);
        var height = this.getHeight();

        // far as i can tell "mouseX" and "mouseY" are actually the positions of the corner of the tooltip
        ps.pushPose();
        ps.translate(mouseX, mouseY, 500);
        RenderSystem.enableBlend();
        renderBG(gg, HUDOverlay.SELECTED_HAND_MIRROR_INDICATOR, pBlitOffset);

        // renderText happens *before* renderImage for some asinine reason
//                RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int itemSize = 32;

        GloopyRenderUtils.renderGuiItemIcon(storedItem, mouseX + width /2 - itemSize/2, mouseY + height / 2 - itemSize/2, itemSize);
        // pItemRenderer.renderGuiItemIcon(storedItem, mouseX + width /2, mouseY + height / 2);

        ps.popPose();
    }

    private static void renderBG(GuiGraphics gg, ResourceLocation background, int blitOffset, float u, float v, int textWidth, int textHeight, 
    int sWidth, int sHeight) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, background);
        PoseStack ps = gg.pose();
        ps.pushPose();
        ps.scale(RENDER_SIZE / (float)sWidth, RENDER_SIZE / (float)sHeight, 1f);
        gg.blit(background, 0, 0, blitOffset, u, v, sWidth, sHeight, textWidth,
            textHeight);
        ps.popPose();
    }

    private static void renderBG(GuiGraphics gg, ResourceLocation background, int blitOffset){
        renderBG(gg, background, blitOffset, 0, 0, (int)RENDER_SIZE, (int) RENDER_SIZE, (int) RENDER_SIZE, (int) RENDER_SIZE);
    }

    // idk just use this for now 
    private static void renderBG(GuiGraphics gg, HUDOverlay overlay, int blitOffset){
        Tuple<Integer, Integer> size = overlay.getTextureSize();
        renderBG(gg, overlay.getTextureId(), blitOffset, overlay.getMinU(), overlay.getMinV(), size.getA(), size.getB(), 
            overlay.getTWidth(), overlay.getTHeight());
    }

    @Override
    public int getWidth(Font pFont) {
        return (int) RENDER_SIZE;
    }

    @Override
    public int getHeight() {
        return (int) RENDER_SIZE;
    }
}

