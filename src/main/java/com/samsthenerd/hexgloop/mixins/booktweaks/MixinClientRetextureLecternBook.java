package com.samsthenerd.hexgloop.mixins.booktweaks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.HexGloop;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.util.ItemStackUtil;

@Mixin(LecternRenderer.class)
public class MixinClientRetextureLecternBook {

    private static Map<ResourceLocation, Material> bookTextures = new HashMap<>();

    @ModifyExpressionValue(
        method="render(Lnet/minecraft/world/level/block/entity/LecternBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at=@At(value="FIELD", target="net/minecraft/client/renderer/blockentity/EnchantTableRenderer.BOOK_LOCATION : Lnet/minecraft/client/resources/model/Material;" )
    )    
    public Material retextureLecternBook(Material original, LecternBlockEntity lecternBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j){
        ItemStack lecternBook = lecternBlockEntity.getBook();
        HexGloop.logPrint("lectern has book: " + lecternBook);
        Book patchiBook = ItemStackUtil.getBookFromStack(lecternBook);
        if(patchiBook != null){
            HexGloop.logPrint("got a patchi book");
            if(bookTextures.containsKey(patchiBook.id)){
                return Objects.requireNonNullElse(bookTextures.get(patchiBook.id), original);
            }
            ResourceLocation textureId = ResourceLocation.fromNamespaceAndPath(patchiBook.id.getNamespace(), "entity/lectern_" + patchiBook.id.getPath());
            ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png");
            Optional<Resource> maybeTexture = Minecraft.getInstance().getResourceManager().getResource(resourceId);
            if(maybeTexture.isEmpty()){
                HexGloop.logPrint("couldn't find texture for " + patchiBook.id);
                bookTextures.put(patchiBook.id, null);
                return original;
            }
            HexGloop.logPrint("putting texture for " + patchiBook.id + " in cache");
            Material foundSpriteID = new Material(TextureAtlas.LOCATION_BLOCKS, textureId);
            bookTextures.put(patchiBook.id, foundSpriteID);
            return foundSpriteID;
        }
        return original;
    }
}
