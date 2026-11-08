package com.samsthenerd.wnboi.utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL30;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
import org.joml.Matrix4fStack;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.samsthenerd.wnboi.WNBOI;

/*
 * just some functions for rendering math.
 * mostly point calculations 
 */
public class RenderUtils {
    
    // returns points that make up the outline of an arc.
    // returns numDivisons+1 points
    // angle starts at 0 and goes counter clockwise to 2pi
    // invert will give you a concave arc that still goes counter clockwise
    public static List<Vec3> calcArcPoints(int numDivisions, double radius, double startAngle, double endAngle, boolean invert){
        List<Vec3> points = new ArrayList<Vec3>();
        double angle = startAngle;
        double angleStep = (endAngle - startAngle) / numDivisions;
        Vec3 posCorrection = new Vec3(0, 0, 0); // normally nothing, but if we're inverted we need to correct the position
        if(invert){ // need to start at other side and go clockwise
            angle = endAngle+Math.PI;
            angleStep *= -1;
            posCorrection = new Vec3(radius*(Math.cos(startAngle)-Math.cos(endAngle+Math.PI)), 
                radius*(Math.sin(startAngle)-Math.sin(endAngle+Math.PI)), 0);
        }
        for(int i = 0; i <= numDivisions; i++){
            points.add(new Vec3(radius * Math.cos(angle), radius * Math.sin(angle), 0).add(posCorrection));
            angle += angleStep;
        }
        return points;
    }

    public static List<Vec3> calcArcPoints(int numDivisions, double radius, double startAngle, double endAngle){
        return calcArcPoints(numDivisions, radius, startAngle, endAngle, false);
    }

    // used for the polygon curve type described below
    public static List<Vec3> calcPolyPoints(int numDivisions, double curveRadius, double startAngle, double endAngle, boolean invert){
        Vec3 pointA = new Vec3(curveRadius * Math.cos(startAngle), curveRadius * Math.sin(startAngle), 0);
        Vec3 pointB = new Vec3(curveRadius * Math.cos(endAngle), curveRadius * Math.sin(endAngle), 0);
        Vec3 center = pointB.subtract(pointA).scale(0.5).add(pointA); 
        Vec3 adjA = pointA.subtract(center); 
        double aAngle = Math.atan2(adjA.y, adjA.x);
        double bAngle = aAngle + Math.PI;
        double adjRad = adjA.length();
        // gets points as if they're on a circle centered between our endpoints.
        List<Vec3> polyPoints = calcArcPoints(numDivisions, adjRad, aAngle, bAngle, invert); 
        // all are relative to center so add it back
        for(int i = 0; i < polyPoints.size(); i++){
            polyPoints.set(i, polyPoints.get(i).add(center));
        }
        return polyPoints;

    }

    public static Vec3 calcNormal(Vec3 point){
        return new Vec3(-point.y, point.x, 0);
    }

    public static List<Vec3> calcStroke(List<Vec3> points, double outerStrokeWidth, double innerStrokeWidth){
        List<Vec3> stroke = new ArrayList<Vec3>();
        if(points.size() <= 1){
            return points; // figure this out when we figure out caps
        }
        // store values as we go so that we don't have to recalculate as much
        Vec3 del1 = points.get(1).subtract(points.get(0));
        Vec3 del2 = del1;
        // vars are parts that get multiplied by stroke width
        double AValVar;
        double AValConst;
        double BValVar = Math.sqrt(del2.x*del2.x + del2.y*del2.y);
        double BValConst = del1.y * points.get(0).x - del1.x * points.get(1).y;
        Vec3 p1;
        Vec3 p2 = points.get(0);
        Vec3 p3 = p2;
        boolean isClosed = false;
        for(int i = 0; i < points.size(); i++){
            del1 = del2;
            AValVar = BValVar;
            AValConst = BValConst;
            p1 = p2;
            p2 = p3;
            if(i == points.size() - 1){ // last point 
                if(points.get(0).equals(p3)){ //closed path, get first point
                    p3 = points.get(1);
                    isClosed = true;
                }
            } else { // not last point
                p3 = points.get(i+1);
            }
            del2 = p3.subtract(p2);
            BValVar = Math.sqrt(del2.x*del2.x + del2.y*del2.y);
            BValConst = del2.y * p2.x - del2.x * p2.y;

            double det = del1.x * del2.y - del1.y * del2.x;
            if(det == 0){ // straight line, add halfway points to avoid messing up color somehow
                Vec3 norm;
                if(i == 0){
                    norm = (new Vec3(del2.y, -del2.x,0)).normalize();
                } else {
                    norm = (new Vec3(del1.y, -del1.x,0)).normalize();
                }
                stroke.add(p2.add(norm.scale(outerStrokeWidth)));
                stroke.add(p2.add(norm.scale(-innerStrokeWidth)));
            } else {
                // double AValOuter = AValConst + outerStrokeWidth * AValVar;
                // double AValInner = AValConst - innerStrokeWidth * AValVar;
                // double BValOuter = BValConst + outerStrokeWidth * BValVar;
                // double BValInner = BValConst - innerStrokeWidth * BValVar;
                double AValOuter = AValConst + innerStrokeWidth * AValVar;
                double AValInner = AValConst - outerStrokeWidth * AValVar;
                double BValOuter = BValConst + innerStrokeWidth * BValVar;
                double BValInner = BValConst - outerStrokeWidth * BValVar;
                // get raw values, then we'll scale with stroke width to get the actual points
                double outerX = (BValOuter * del1.x - AValOuter * del2.x) / det;
                double outerY = (BValOuter * del1.y - AValOuter * del2.y) / det;
                stroke.add(new Vec3(outerX, outerY, 0));
                
                double innerX = (BValInner * del1.x - AValInner * del2.x) / det;
                double innerY = (BValInner * del1.y - AValInner * del2.y) / det;
                stroke.add(new Vec3(innerX, innerY, 0));

                if(isClosed){ // add first point again
                    stroke.set(0, (new Vec3(outerX, outerY, 0)));
                    stroke.set(1, new Vec3(innerX, innerY, 0));
                }

                // maybe need to add a thing to override first point if it's closed
            }
        }
        return stroke;
    }

    
    public static List<Vec3> drawStroke(List<Vec3> points, double outerStrokeWidth, double innerStrokeWidth){
        List<Vec3> stroke = calcStroke(points, outerStrokeWidth, innerStrokeWidth);
        // WNBOI.logPrint("stroke size: " + stroke.size() + " from points size: " + points.size());
        // draw stroke
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP , DefaultVertexFormat.POSITION_COLOR);
        for(int i = 0; i < stroke.size(); i++){
            // WNBOI.logPrint("stroke point: " + stroke.get(i).x + ", " + stroke.get(i).y);
            buffer.addVertex((float)stroke.get(i).x, (float)stroke.get(i).y, 0f).setColor(128,128,128,255);
        }
        return stroke; // pass it up just in case
    }

    public static void testCalcStroke(){
        List<Vec3> points1 = new ArrayList<Vec3>();
        points1.add(new Vec3(0,0,0));
        points1.add(new Vec3(0.5,1,0));
        points1.add(new Vec3(1,1,0));
        points1.add(new Vec3(1,0,0));
        points1.add(new Vec3(0,0,0));
        List<Vec3> stroke1 = calcStroke(points1, 0.1, 0.1);
        WNBOI.logPrint("test 1:");
        for(int s = 0; s < stroke1.size(); s++){
            WNBOI.logPrint("stroke point: " + stroke1.get(s).x + ", " + stroke1.get(s).y);
        }
    }

    // renders the item icon with more options available, might be bad for performance? use with caution for sure
    public static void renderItemIcon(GuiGraphics matrices, ItemStack stack, int x, int y, int argb){
        setupTransparencyFrameBuffer();
        Minecraft.getInstance().getItemRenderer();
        matrices.renderItem(stack, x, y);
        drawTransparencyBuffer(matrices, argb);
    }

    // inspired by https://github.com/wisp-forest/owo-lib/blob/1.19.3/src/main/java/io/wispforest/owo/ui/container/RenderEffectWrapper.java

    public static int initialFrameBuffer;
    public static RenderTarget transFrameBuffer; 

    // call this if you want to draw transparency
    public static void setupTransparencyFrameBuffer(){
        Window window = Minecraft.getInstance().getWindow();
        initialFrameBuffer = GlStateManager.getBoundFramebuffer();
        transFrameBuffer = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
        transFrameBuffer.setClearColor(0, 0, 0, 0);
        transFrameBuffer.bindWrite(false);
    }

    public static void drawTransparencyBuffer(GuiGraphics matrices, int argb){
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, initialFrameBuffer);
        RenderSystem.setShaderColor(ARGB32.red(argb)/255f, ARGB32.green(argb)/255f, ARGB32.blue(argb)/255f, ARGB32.alpha(argb)/255f);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, transFrameBuffer.getColorTextureId());

        Window window = Minecraft.getInstance().getWindow();
        // DrawableHelper.drawTexture(matrices, 0, 0, 0, window.getScaledHeight(), window.getWidth(), window.getHeight(), window.getScaledWidth(), window.getScaledHeight());
        // blit with a raw texture id (vertically flipped framebuffer), done by hand since GuiGraphics.blit needs a ResourceLocation
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        org.joml.Matrix4f mat = matrices.pose().last().pose();
        float w = window.getGuiScaledWidth(), h = window.getGuiScaledHeight();
        BufferBuilder bb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bb.addVertex(mat, 0, h, 0).setUv(0, 0);
        bb.addVertex(mat, w, h, 0).setUv(1, 0);
        bb.addVertex(mat, w, 0, 0).setUv(1, 1);
        bb.addVertex(mat, 0, 0, 0).setUv(0, 1);
        BufferUploader.drawWithShader(bb.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    // copied from InventoryScreen - expanded to be not just LivingEntity and removed the look at mouse thing
    public static void drawEntity(int x, int y, int size, Entity entity) {
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        matrixStack.translate(x, y, 1050.0f);
        matrixStack.scale(1.0f, 1.0f, -1.0f);
        RenderSystem.applyModelViewMatrix();
        PoseStack matrixStack2 = new PoseStack();
        matrixStack2.translate(0.0, 0.0, 1000.0);
        matrixStack2.scale(size, size, size);
        Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0f);
        // Quaternion quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(g * 20.0f);
        // quaternion.hamiltonProduct(quaternion2);
        matrixStack2.mulPose(quaternion);
        quaternion = new Quaternionf(quaternion);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion.conjugate();
        entityRenderDispatcher.overrideCameraOrientation(quaternion);
        entityRenderDispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, matrixStack2, immediate, 0xF000F0);
        immediate.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        matrixStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public static void renderText(GuiGraphics matrices, Component text, int x, int y, int color){
        matrices.drawString(Minecraft.getInstance().font, text, x, y, color, false);
    }


    // different types of built-in curves. can support up to 16 different types.
    // if you want to use your own curve it's probably best to ignore this and just override the getCurve methods directly

    /* pass curve options without having a whole other class for it
        curve types are just 4 bit values that map to default curve types:
            -arc, default, approximates a circle
            -polygon, approximates half of a polygon based on number of divisions, so 1 is flat, 
                2 is a triangle (half of a square), 3 is a trapezoid (half of a hexagon),
                 .., n divisons is half of a 2n sided polygon
        invert options are just booleans that invert the curves
    */
    public static int buildCurveOptions(int outerCurve, int innerCurve, boolean invertOuter, boolean invertInner){
        int options = 0;
        options += outerCurve;
        options += outerCurve << 4;
        options += (invertOuter ? 1 : 0) << 8;
        options += (invertInner ? 1 : 0) << 9;

        return options;
    }

    public static int getOuterCurve(int options){
        return options & 0xF;
    }

    public static int getInnerCurve(int options){
        return (options >>> 4) & 0xF;
    }

    public static boolean getInvertOuter(int options){
        return (options >>> 8) % 2 == 1;
    }

    public static boolean getInvertInner(int options){
        return (options >>> 9) % 2 == 1;
    }

    public static int setOuterCurve(int options, int curve){
        return (options & 0xFFF0) + curve;
    }

    public static int setInnerCurve(int options, int curve){
        return (options & 0xFF0F) + (curve << 4);
    }

    public static int setInvertOuter(int options, boolean invert){
        return (options & 0xFEFF) + ((invert ? 1 : 0) << 8);
    }

    public static int setInvertInner(int options, boolean invert){
        return (options & 0xFDFF) + ((invert ? 1 : 0) << 9);
    }


}
