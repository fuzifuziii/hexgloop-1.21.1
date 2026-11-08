package com.samsthenerd.hexgloop.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import com.mojang.math.Axis;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.blocks.BlockSlateChest;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

// code just yoinked from ChestBlockEntityRenderer
public class BERHexChest<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {
   private static final String BASE = "bottom";
   private static final String LID = "lid";
   private static final String LATCH = "lock";
   private final ModelPart singleChestLid;
   private final ModelPart singleChestBase;
   private final ModelPart singleChestLatch;
   private final ModelPart doubleChestLeftLid;
   private final ModelPart doubleChestLeftBase;
   private final ModelPart doubleChestLeftLatch;
   private final ModelPart doubleChestRightLid;
   private final ModelPart doubleChestRightBase;
   private final ModelPart doubleChestRightLatch;

   
   public BERHexChest(BlockEntityRendererProvider.Context ctx) {

      ModelPart modelPart = ctx.bakeLayer(ModelLayers.CHEST);
      this.singleChestBase = modelPart.getChild("bottom");
      this.singleChestLid = modelPart.getChild("lid");
      this.singleChestLatch = modelPart.getChild("lock");
      // i think most of our chests will just be single but uhh, doesn't hurt to keep this in ?
      ModelPart modelPart2 = ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
      this.doubleChestLeftBase = modelPart2.getChild("bottom");
      this.doubleChestLeftLid = modelPart2.getChild("lid");
      this.doubleChestLeftLatch = modelPart2.getChild("lock");
      ModelPart modelPart3 = ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
      this.doubleChestRightBase = modelPart3.getChild("bottom");
      this.doubleChestRightLid = modelPart3.getChild("lid");
      this.doubleChestRightLatch = modelPart3.getChild("lock");
   }

   public static LayerDefinition getSingleTexturedModelData() {
      MeshDefinition modelData = new MeshDefinition();
      PartDefinition modelPartData = modelData.getRoot();
      modelPartData.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
      modelPartData.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      modelPartData.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
      return LayerDefinition.create(modelData, 64, 64);
   }

   public static LayerDefinition getRightDoubleTexturedModelData() {
      MeshDefinition modelData = new MeshDefinition();
      PartDefinition modelPartData = modelData.getRoot();
      modelPartData.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
      modelPartData.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      modelPartData.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
      return LayerDefinition.create(modelData, 64, 64);
   }

   public static LayerDefinition getLeftDoubleTexturedModelData() {
      MeshDefinition modelData = new MeshDefinition();
      PartDefinition modelPartData = modelData.getRoot();
      modelPartData.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
      modelPartData.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      modelPartData.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
      return LayerDefinition.create(modelData, 64, 64);
   }

   public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
      Level world = entity.getLevel();
      boolean bl = world != null;
      BlockState blockState = bl ? entity.getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
      ChestType chestType = blockState.hasProperty(ChestBlock.TYPE) ? (ChestType)blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
      boolean isEnergized = blockState.hasProperty(BlockCircleComponent.ENERGIZED) ? blockState.getValue(BlockCircleComponent.ENERGIZED) : false;
      Block block = blockState.getBlock();
      // i guess skill issue if we want to add more later,, could just like,, abstract some stuff out to an interface or something i guess
      if (block instanceof BlockSlateChest abstractChestBlock && entity instanceof BlockEntitySlateChest chestEntity) {
         boolean bl2 = chestType != ChestType.SINGLE;
         matrices.pushPose();
         float f = ((Direction)blockState.getValue(ChestBlock.FACING)).toYRot();
         matrices.translate(0.5, 0.5, 0.5);
         matrices.mulPose(Axis.YP.rotationDegrees(-f));
         matrices.translate(-0.5, -0.5, -0.5);
         DoubleBlockCombiner.NeighborCombineResult propertySource;
         if (bl) {
            propertySource = abstractChestBlock.getBlockEntitySource(blockState, world, entity.getBlockPos(), true);
         } else {
            propertySource = DoubleBlockCombiner.Combiner::acceptNone;
         }

         float g = ((Float2FloatFunction)propertySource.apply(ChestBlock.opennessCombiner((LidBlockEntity)entity))).get(tickDelta);
         g = 1.0F - g;
         g = 1.0F - g * g * g;
         int i = ((Int2IntFunction)propertySource.apply(new BrightnessCombiner())).applyAsInt(light);
         // SpriteIdentifier spriteIdentifier = TexturedRenderLayers.getChestTexture(entity, chestType, false);
         Material spriteIdentifier = getChestTexture(chestEntity, chestType);
         VertexConsumer vertexConsumer = spriteIdentifier.buffer(vertexConsumers, RenderType::entityCutout);
         if (bl2) {
            if (chestType == ChestType.LEFT) {
               this.render(matrices, vertexConsumer, this.doubleChestLeftLid, this.doubleChestLeftLatch, this.doubleChestLeftBase, g, i, overlay);
            } else {
               this.render(matrices, vertexConsumer, this.doubleChestRightLid, this.doubleChestRightLatch, this.doubleChestRightBase, g, i, overlay);
            }
         } else {
            this.render(matrices, vertexConsumer, this.singleChestLid, this.singleChestLatch, this.singleChestBase, g, i, overlay);
            if(isEnergized){
               matrices.pushPose();
               matrices.translate(0.5f/16f, 0, 0.5f/16f);
               matrices.scale(15f/16f, 0.75f, 15f/16f);
               Matrix4f positionMatrix = matrices.last().pose();
               // Tessellator tess = Tessellator.getInstance();
               // BufferBuilder buf = tess.getBuffer();
               VertexConsumer buf = vertexConsumers.getBuffer(Sheets.translucentCullBlockSheet()); // no clue - POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
               TextureAtlasSprite sprite = new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.parse("block/white_concrete_powder")).sprite();
               // buf.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
               for(Direction dir : Direction.values()){
                  if(!dir.getAxis().isHorizontal()) continue;
                  Vec3i normal = dir.getNormal();
                  Vec3i nextVec = BERConjuredRedstone.startingVectors.get(dir);
                  // white_concrete_powder
                  for(int d = 0; d < 4; d++){
                     boolean closeToFloor = nextVec.getY() < 0;
                     int color;
                     if(chestEntity.isGloopy()){
                        color = closeToFloor ? 0xA0F2CD : 0x378A6D; // darker gloopy as it goes up  
                     } else {
                        color = !closeToFloor ? 0xb38ef3 : 0xFECBE6; // darker pink as it goes up
                     }
                     buf.addVertex(positionMatrix, (nextVec.getX()+1)/2, (nextVec.getY()+1)/2, (nextVec.getZ()+1)/2)
                        .setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, closeToFloor ? (int)(0.65*256) : 0)
                        .setUv( ((d >> 1) & 1) == 0 ? sprite.getU0() : sprite.getU1(), (((d >> 1) & 1) ^ (d & 1)) == 0 ? sprite.getV0() : sprite.getV1())
                        .setOverlay(0)
                        .setLight(255)
                        .setNormal(normal.getX(), normal.getY(), normal.getZ()); // no clue
                     nextVec = normal.offset(normal.cross(nextVec));
                  }
               }
               // RenderSystem.setShader(GameRenderer::getRenderTypeEntityTranslucentShader);
               // RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
               // RenderSystem.enableDepthTest();
               // RenderSystem.enableBlend();
            
               // tess.draw();
               matrices.popPose();
            }
         }

         matrices.popPose();
      }
   }

   private void render(PoseStack matrices, VertexConsumer vertices, ModelPart lid, ModelPart latch, ModelPart base, float openFactor, int light, int overlay) {
      lid.xRot = -(openFactor * 1.5707964F);
      latch.xRot = lid.xRot;
      lid.render(matrices, vertices, light, overlay);
      latch.render(matrices, vertices, light, overlay);
      base.render(matrices, vertices, light, overlay);
   }

    public Material getChestTexture(ChestBlockEntity cbe, ChestType type){
        if(cbe instanceof BlockEntitySlateChest slateChest){
            if(slateChest.isGloopy()){
                return getChestTextureId("gloopy_slate_chest");
            } else {
                return getChestTextureId("slate_chest");
            }
        }
        return Sheets.chooseMaterial(cbe, type, false);
    }

    public Material getChestTextureId(String variant){
        // we'll see if this works i guess
        return new Material(Sheets.CHEST_SHEET, ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "entity/chest/" + variant));
    }
}

