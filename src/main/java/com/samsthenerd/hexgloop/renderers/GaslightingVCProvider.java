package com.samsthenerd.hexgloop.renderers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class GaslightingVCProvider implements MultiBufferSource{
    public MultiBufferSource innerProvider;
    Function <VertexConsumer, VertexConsumer> middleConsumerFactory;

    public GaslightingVCProvider(MultiBufferSource innerProvider, Function<VertexConsumer, VertexConsumer> middleConsumerFactory){
        this.innerProvider = innerProvider;
        this.middleConsumerFactory = middleConsumerFactory;
    }

    @Override
    public VertexConsumer getBuffer(RenderType layer) {
        return middleConsumerFactory.apply(innerProvider.getBuffer(layer));
    }
}
