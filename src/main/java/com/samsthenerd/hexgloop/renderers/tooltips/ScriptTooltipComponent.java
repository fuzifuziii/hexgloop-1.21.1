package com.samsthenerd.hexgloop.renderers.tooltips;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kotlin.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.items.tooltips.ScriptTooltipData;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.RenderLib;

public class ScriptTooltipComponent implements ClientTooltipComponent {

    private static final float RENDER_SIZE = 128f;

    private final ResourceLocation background;
    private final List<HexPattern> patterns;
    private final List<RenderablePattern> renderablePatterns;
    private final int cols;
    private final int rows;
    private final float size;

    public ScriptTooltipComponent(ScriptTooltipData tt) {
        this.patterns = tt.patterns;
        this.background = tt.background;

        int patternCount = patterns.size();
        cols = (int) Math.ceil(Math.sqrt(patternCount));
        rows = (int) Math.ceil(patternCount / (float) cols);
        size = (RENDER_SIZE*0.8f) / Math.max(cols, rows);
        renderablePatterns = new ArrayList<>();
        for(HexPattern pat : patterns){
            renderablePatterns.add(new RenderablePattern(pat, size));
        }
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
        renderBG(gg, this.background, pBlitOffset);

        // renderText happens *before* renderImage for some asinine reason
//                RenderSystem.disableBlend();
        float top = (height - (size * rows))/2f;
        float left = (width - (size * cols))/2f;
        ps.translate(left, top, 0);
        for(int i = 0; i < patterns.size(); i++){
            int row = i / cols;
            int col = i % cols;
            ps.pushPose();
            renderablePatterns.get(i).render(ps, col*size, row*size, pBlitOffset);
            ps.popPose();
        }
        ps.popPose();
    }

    private static void renderBG(GuiGraphics gg, ResourceLocation background, int blitOffset) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, background);
        // x y blitoffset sw sh w h ... ?
        // parchment doesn't have this mapped
        gg.blit(background, 0, 0, blitOffset, 0f, 0f, (int) RENDER_SIZE, (int) RENDER_SIZE, (int) RENDER_SIZE,
            (int) RENDER_SIZE);
    }

    @Override
    public int getWidth(Font pFont) {
        return (int) RENDER_SIZE;
    }

    @Override
    public int getHeight() {
        return (int) RENDER_SIZE;
    }

    public static class RenderablePattern{
        public final HexPattern pattern;
        public final List<Vec2> zappyPoints;
        public final List<Vec2> pathfinderDots;
        public final float scale;
        public final float size;

        // size is the size of this pattern
        public RenderablePattern(HexPattern pattern, float size){
            this.pattern = pattern;
            this.size = size;
            var pair = RenderLib.getCenteredPattern(pattern, size, size, 16f);
            this.scale = pair.getFirst();
            var dots = pair.getSecond();
            this.zappyPoints = RenderLib.makeZappy(
                dots, RenderLib.findDupIndices(pattern.positions()),
                10, 0.8f, 0f, 0f, RenderLib.DEFAULT_READABILITY_OFFSET, RenderLib.DEFAULT_LAST_SEGMENT_LEN_PROP,
                0.0);
            this.pathfinderDots = dots.stream().distinct().collect(Collectors.toList());
        }

        // x and y should have offsets baked in
        public void render(PoseStack ps, float x, float y, int blitOffset){
            ps.pushPose();
            ps.translate(x, y, 100);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableCull();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            ps.translate(size / 2f, size / 2f, 1);

            var mat = ps.last().pose();
            var outer = 0xff_d2c8c8;
            var innerLight = 0xc8_aba2a2;
            var innerDark = 0xc8_322b33;
            float widthScale = (float)Math.sqrt(size / (RENDER_SIZE*0.8));
            RenderLib.drawLineSeq(mat, this.zappyPoints, 6f * widthScale, 0,
                outer, outer);
            RenderLib.drawLineSeq(mat, this.zappyPoints, 6f * 0.4f * widthScale, 0,
                innerDark, innerLight);
            RenderLib.drawSpot(mat, this.zappyPoints.get(0), 2.5f*widthScale, 1f, 0.1f, 0.15f, 0.6f);

            for (var dot : this.pathfinderDots) {
                RenderLib.drawSpot(mat, dot, 1.5f*widthScale, 0.82f, 0.8f, 0.8f, 0.5f);
            }

            ps.popPose();
        }
    }
}
