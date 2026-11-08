package com.samsthenerd.hexgloop.blockentities;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.HexGloop;

import at.petrak.hexcasting.api.pigment.FrozenPigment;

public class BERConjuredRedstone implements BlockEntityRenderer<BlockEntityConjuredRedstone> {

    public static Map<Direction, Vec3i> startingVectors = new HashMap<Direction, Vec3i>();

    static {
        startingVectors.put(Direction.DOWN, new Vec3i(1, -1, 1));
        startingVectors.put(Direction.UP, new Vec3i(-1, 1, -1));
        startingVectors.put(Direction.NORTH, new Vec3i(-1, -1, -1));
        startingVectors.put(Direction.SOUTH, new Vec3i(1, -1, 1));
        startingVectors.put(Direction.WEST, new Vec3i(-1, -1, 1));
        startingVectors.put(Direction.EAST, new Vec3i(1, -1, -1));
    }

    public BERConjuredRedstone(BlockEntityRendererProvider.Context ctx){

    }

    private static final float minOpac = 0.25f;
    private static final float maxOpac = 1f;

    @Override
    public void render(BlockEntityConjuredRedstone be, float tickDelta, PoseStack stack, MultiBufferSource provider, int light, int overlay){
        VertexConsumer buffer = provider.getBuffer(Sheets.translucentCullBlockSheet());
        int power = be.getPower();
        int transPower = (int)Math.floor((minOpac + ((maxOpac-minOpac)*power/15f)) * 255);
        FrozenPigment colorizer = be.getColorizer();
        ResourceLocation textureId = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "block/conjured_redstone");
        TextureAtlasSprite sprite = new Material(InventoryMenu.BLOCK_ATLAS, textureId).sprite();
        float time = be.getLevel().getGameTime();
        for(Direction dir : Direction.values()){
            Vec3i normal = dir.getNormal();
            Vec3i nextVec = startingVectors.get(dir);
            Vec3 baseWorldVec = new Vec3(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ());
            for(int i = 0; i < 4; i++){
                Vec3 worldVec = baseWorldVec.add(new Vec3((nextVec.getX()+1)/2, (nextVec.getY()+1)/2, (nextVec.getZ()+1)/2));
                int color = colorizer.getColorProvider().getColor(time+tickDelta, worldVec);
                buffer.addVertex(stack.last().pose(), (nextVec.getX()+1)/2, (nextVec.getY()+1)/2, (nextVec.getZ()+1)/2)
                    .setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, transPower)
                    .setUv(((i >> 1) & 1) == 0 ? sprite.getU0() : sprite.getU1(), (((i >> 1) & 1) ^ (i & 1)) == 0 ? sprite.getV0() : sprite.getV1())
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(stack.last(), normal.getX(), normal.getY(), normal.getZ());
                nextVec = normal.offset(normal.cross(nextVec));
            }
        }
    }
}
