package com.samsthenerd.hexgloop.renderers;

import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.blockentities.BERConjuredRedstone;
import com.samsthenerd.hexgloop.items.HexGloopItems;

// vscode just dies if we put <T, M> at the end of this,, i have absolutely no idea why !!!
public class HandThingFeatureRenderer<T extends LivingEntity, M extends EntityModel<?>> extends RenderLayer{

    private final HumanoidModel<LivingEntity> model;
    
    public HandThingFeatureRenderer(RenderLayerParent<T, EntityModel<T>> context, EntityModelSet loader, boolean slim) {
        super(context); 
        model = new HumanoidModel<LivingEntity>(loader.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER));
        // model = (BipedEntityModel<LivingEntity>)context.getModel(); // unsafe cast !
    }

    private boolean hasSlimArms(Player player){
        boolean isSlim = false;
        float slimVal = 1;
        if(player instanceof AbstractClientPlayer cPlayer){
            isSlim = cPlayer.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM;
            
        }
        return isSlim;
    }

    public void renderArm(PoseStack matrices, MultiBufferSource vertexConsumers, Player player, HumanoidModel<LivingEntity> entModel, HumanoidArm side, boolean firstPerson){
        matrices.pushPose();
        followBodyRotations(player, entModel);
        if(side == HumanoidArm.LEFT){
            translateToLeftArm(matrices, entModel, player, firstPerson);
        } else {
            translateToRightArm(matrices, entModel, player, firstPerson);
        }
        matrices.scale(0.135f, 0.135f, 0.135f); // looks like 0.125 puts it tight to a thick player hand
        // might need to adjust it a bit
        boolean isSlim = hasSlimArms(player);
        float slimVal = (isSlim ? 0.75f : 1f);
        if(isSlim){
                matrices.translate(0.25 * .125 / .135 * (side == HumanoidArm.LEFT ? -1 : 1), 0, 0);
            matrices.scale(slimVal, 1, 1);
        }
        Matrix4f positionMatrix = matrices.last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // VertexConsumer buf = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityTranslucentCull()); // no clue - POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        // VertexConsumer buf = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false)); // no clue - POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        // Sprite sprite = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("block/white_concrete_powder")).getSprite();
        // Hex-y pink: #b38ef3
        // bottom of hand

        float handOpacity = 0.75f;
        
        Vec3i firstNormal = Direction.DOWN.getNormal();
        buf.addVertex(positionMatrix, -1, 0, -1).setColor(0xb3/256f, 0x8e/256f, 0xf3/256f, handOpacity)
            // .texture( sprite.getMinU(), sprite.getMinV())
            // .overlay(0,0)
            // .light(255, 255)
            // .normal(firstNormal.getX(), firstNormal.getY(), firstNormal.getZ()) // no clue
            ;
        buf.addVertex(positionMatrix, -1, 0, 1).setColor(0xb3/256f, 0x8e/256f, 0xf3/256f, handOpacity)
            // .texture( sprite.getMinU(), sprite.getMaxV())
            // .overlay(0,0)
            // .light(255, 255)
            // .normal(firstNormal.getX(), firstNormal.getY(), firstNormal.getZ()) // no clue
            ;
        buf.addVertex(positionMatrix, 1, 0, 1).setColor(0xb3/256f, 0x8e/256f, 0xf3/256f, handOpacity)
            // .texture( sprite.getMaxU(), sprite.getMaxV())
            // .overlay(0,0)
            // .light(255, 255)
            // .normal(firstNormal.getX(), firstNormal.getY(), firstNormal.getZ()) // no clue
            ;
        buf.addVertex(positionMatrix, 1, 0, -1).setColor(0xb3/256f, 0x8e/256f, 0xf3/256f, handOpacity)
            // .texture( sprite.getMaxU(), sprite.getMinV())
            // .overlay(0,0)
            // .light(255, 255)
            // .normal(firstNormal.getX(), firstNormal.getY(), firstNormal.getZ()) // no clue
            ;

        for(Direction dir : Direction.values()){
            if(!dir.getAxis().isHorizontal()) continue;
            Vec3i normal = dir.getNormal();
            Vec3i nextVec = BERConjuredRedstone.startingVectors.get(dir);
            for(int i = 0; i < 4; i++){
                boolean closeToHand = nextVec.getY() > 0;
                int color = closeToHand ? 0xb38ef3 : 0xFECBE6; // lighter pink as it goes up
                buf.addVertex(positionMatrix, nextVec.getX(), (nextVec.getY()-1), nextVec.getZ())
                    .setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, closeToHand ? (int)(handOpacity*256) : 0)
                    // .texture( ((i >> 1) & 1) == 0 ? sprite.getMinU() : sprite.getMaxU(), (((i >> 1) & 1) ^ (i & 1)) == 0 ? sprite.getMinV() : sprite.getMaxV())
                    // .overlay(0,0)
                    // .light(closeToHand ? 255 : 0, closeToHand ? 255 : 0)
                    // .normal(normal.getX(), normal.getY(), normal.getZ()) // no clue
                    ;
                nextVec = normal.offset(normal.cross(nextVec));
            }
        }

        // RenderSystem.setShader(GameRenderer::getRenderTypeEntityTranslucentShader);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

    
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
        
        matrices.popPose();
    }

    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, 
        Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch){
        if(!(entity instanceof Player player)) return;
        
        Map<String, List<ItemStack>> trinkets = HexGloop.TRINKETY_INSTANCE.getTrinkets(player);
        boolean hasLeft = false;
        boolean hasRight = false;
        // this is set up for rendering just the glowy effect - would require some reworking to render other trinkets
        for(ItemStack stack : trinkets.getOrDefault("mainhandring", List.of())){
            if(stack.getItem() == HexGloopItems.CASTING_RING_ITEM.get()){
                hasRight = true;
                break;
            }
        }
        for(ItemStack stack : trinkets.getOrDefault("offhandring", List.of())){
            if(stack.getItem() == HexGloopItems.CASTING_RING_ITEM.get()){
                hasLeft = true;
                break;
            }
        }
        if(hasLeft){
            model.setupAnim(player, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
            model.prepareMobModel(player, limbAngle, limbDistance, tickDelta);
            
            renderArm(matrices, vertexConsumers, player, model, HumanoidArm.LEFT, false);
        }
        if(hasRight){
            model.setupAnim(player, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
            model.prepareMobModel(player, limbAngle, limbDistance, tickDelta);
            
            renderArm(matrices, vertexConsumers, player, model, HumanoidArm.RIGHT, false);
        }
    }

    // guided by / yoinked from artifacts mod

    // idk what 'hasFoil' is
    public final void renderFirstPersonArm(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, HumanoidArm side) {
        if (player.isSpectator()) return;

        Map<String, List<ItemStack>> trinkets = HexGloop.TRINKETY_INSTANCE.getTrinkets(player);
        boolean hasRing = false;
        String handString = side == HumanoidArm.LEFT ? "offhandring" : "mainhandring";
        // this is set up for rendering just the glowy effect - would require some reworking to render other trinkets
        for(ItemStack stack : trinkets.getOrDefault(handString, List.of())){
            if(stack.getItem() == HexGloopItems.CASTING_RING_ITEM.get()){
                hasRing = true;
                break;
            }
        }
        if(hasRing){
            ModelPart arm = side == HumanoidArm.LEFT ? model.leftArm : model.rightArm;

            model.crouching = false;
            model.attackTime = model.swimAmount = 0;
            model.setupAnim(player, 0, 0, 0, 0, 0);
            arm.xRot = 0;

            renderArm(matrices, vertexConsumers, player, model, side, true);
        }

        
    }

    // yoinked from TrinketRenderer 
    
    /**
	 * Translates the rendering context to the center of the bottom of the player's right arm
	 */
    static void translateToRightArm(PoseStack matrices, HumanoidModel<? extends LivingEntity> model,
			LivingEntity player){ translateToRightArm(matrices, model, player, false);}
	static void translateToRightArm(PoseStack matrices, HumanoidModel<? extends LivingEntity> model,
			LivingEntity player, boolean firstPerson) {

		if (player.isCrouching() && !model.riding && !player.isSwimming() && !firstPerson) {
			matrices.translate(0.0F, 0.2F, 0.0F);
		}
		matrices.mulPose(Axis.YP.rotation(model.body.yRot));
		matrices.translate(-0.3125F, 0.15625F, 0.0F);
		matrices.mulPose(Axis.ZP.rotation(model.rightArm.zRot));
		matrices.mulPose(Axis.YP.rotation(model.rightArm.yRot));
		matrices.mulPose(Axis.XP.rotation(model.rightArm.xRot));
		matrices.translate(-0.0625F, 0.625F, 0.0F);
	}

	/**
	 * Translates the rendering context to the center of the bottom of the player's left arm
	 */
    static void translateToLeftArm(PoseStack matrices, HumanoidModel<? extends LivingEntity> model,
			LivingEntity player){ translateToLeftArm(matrices, model, player, false);}
	static void translateToLeftArm(PoseStack matrices, HumanoidModel<? extends LivingEntity> model,
			LivingEntity player, boolean firstPerson) {

		if (player.isCrouching() && !model.riding && !player.isSwimming() && !firstPerson) {
			matrices.translate(0.0F, 0.2F, 0.0F);
		}
		matrices.mulPose(Axis.YP.rotation(model.body.yRot));
		matrices.translate(0.3125F, 0.15625F, 0.0F);
		matrices.mulPose(Axis.ZP.rotation(model.leftArm.zRot));
		matrices.mulPose(Axis.YP.rotation(model.leftArm.yRot));
		matrices.mulPose(Axis.XP.rotation(model.leftArm.xRot));
		matrices.translate(0.0625F, 0.625F, 0.0F);
	}

    /**
	 * Rotates the rendering for the models based on the entity's poses and movements. This will do
	 * nothing if the entity render object does not implement {@link LivingEntityRenderer} or if the
	 * model does not implement {@link HumanoidModel}).
	 *
	 * @param entity The wearer of the trinket
	 * @param model The model to align to the body movement
	 */
	@SuppressWarnings("unchecked")
	static void followBodyRotations(final LivingEntity entity, final HumanoidModel<LivingEntity> model) {

		EntityRenderer<? super LivingEntity> render = Minecraft.getInstance()
				.getEntityRenderDispatcher().getRenderer(entity);

		if (render instanceof LivingEntityRenderer) {
			//noinspection unchecked
			LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer =
					(LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) render;
			EntityModel<LivingEntity> entityModel = livingRenderer.getModel();

			if (entityModel instanceof HumanoidModel) {
				HumanoidModel<LivingEntity> bipedModel = (HumanoidModel<LivingEntity>) entityModel;
				bipedModel.copyPropertiesTo(model);
			}
		}
	}
}
