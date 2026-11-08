package com.samsthenerd.hexgloop.renderers;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.samsthenerd.hexgloop.HexGloop;

public class HUDOverlay {
    protected final ResourceLocation textureId;
    protected final int x;
    protected final int y;
    protected final int tWidth;
    protected final int tHeight;

    private static Map<ResourceLocation, Tuple<Integer, Integer>> textureSizes = new HashMap<>();
    public static final ResourceLocation MIRROR_WIDGET_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "textures/gui/mirrorwidgets.png");

    static {
        textureSizes.put(MIRROR_WIDGET_TEXTURE_ID, new Tuple<>(64, 64));
    }


    public static final HUDOverlay BOUND_MIRROR_INDICATOR = new HUDOverlay(MIRROR_WIDGET_TEXTURE_ID, 0, 0, 22, 22);
    public static final HUDOverlay HAND_MIRROR_INDICATOR = new HUDOverlay(MIRROR_WIDGET_TEXTURE_ID, 0, 24, 22, 22);
    public static final HUDOverlay SELECTED_BOUND_MIRROR_INDICATOR = new HUDOverlay(MIRROR_WIDGET_TEXTURE_ID, 24, 0, 22, 22);
    public static final HUDOverlay SELECTED_HAND_MIRROR_INDICATOR = new HUDOverlay(MIRROR_WIDGET_TEXTURE_ID, 24, 24, 22, 22);
    
    public void render(int atX, int atY, double width, double height, float tickDelta){
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex((float)atX, (float)(atY + height), 0f).setUv(getMinU(), getMaxV());
        buffer.addVertex((float)(atX + width), (float)(atY + height), 0f).setUv(getMaxU(), getMaxV());
        buffer.addVertex((float)(atX + width), (float)atY, 0f).setUv(getMaxU(), getMinV());
        buffer.addVertex((float)atX, (float)atY, 0f).setUv(getMinU(), getMinV());

        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public float getMinU(){
        return (float)x / (float)textureSizes.get(textureId).getA();
    }

    public float getMaxU(){
        return (float)(x + tWidth) / (float)textureSizes.get(textureId).getA();
    }

    public float getMinV(){
        return (float)y / (float)textureSizes.get(textureId).getB();
    }

    public float getMaxV(){
        return (float)(y + tHeight) / (float)textureSizes.get(textureId).getB();
    }

    public int getTWidth(){
        return tWidth;
    }

    public int getTHeight(){
        return tHeight;
    }

    public ResourceLocation getTextureId(){
        return textureId;
    }

    public Tuple<Integer, Integer> getTextureSize(){
        return textureSizes.get(textureId);
    }
   

    public HUDOverlay(ResourceLocation textureId, int x, int y, int tWidth, int tHeight){
        this.textureId = textureId;
        this.x = x;
        this.y = y;
        this.tWidth = tWidth;
        this.tHeight = tHeight;
    }
}
