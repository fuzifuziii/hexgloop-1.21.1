package com.samsthenerd.hexgloop.mixins.morelayers;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.block.model.ItemModelGenerator;
@Mixin(ItemModelGenerator.class)
public class MixinPlsMoreItemLayers {
    

    // // why is this method signature so long :(
    // @Redirect(method = "generateBlockModel(Ljava/util/function/Function;Lnet/minecraft/client/renderer/block/model/BlockModel;)Lnet/minecraft/client/renderer/block/model/BlockModel;", 
    //     at = @At(value = "FIELD", 
    //     target = "Lnet/minecraft/client/renderer/block/model/ItemModelGenerator;LAYERS:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    // public List<String> getLongerLayers(Function<SpriteIdentifier, Sprite> textureGetter, JsonUnbakedModel blockModel) {
    //     return MoreModelLayers.MORE_LAYERS;
    // }
}
