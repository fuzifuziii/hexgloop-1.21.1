package com.samsthenerd.hexgloop.mixins.morelayers;

import net.minecraft.client.renderer.block.model.BlockModel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockModel.class)
public class MixinPlsMoreItemLayersGetTexture {
    //     @Redirect(method = "getMaterials(Ljava/util/function/Function;Ljava/util/Set;)Ljava/util/Collection;", 
    //     at = @At(value = "FIELD", 
    //     target = "Lnet/minecraft/client/renderer/block/model/ItemModelGenerator;LAYERS:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    // public List<String> getLongerLayersInTexture() {
    //     return MoreModelLayers.MORE_LAYERS;
    // }
}
