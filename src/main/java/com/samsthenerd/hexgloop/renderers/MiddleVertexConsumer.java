package com.samsthenerd.hexgloop.renderers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;

// a wrapper class to modify data being passed to some vertex consumer before it's actually written
// override the methods you want to modify - note: might be a better way to do this with interfaces but this seems neat enough
public class MiddleVertexConsumer implements VertexConsumer {
    public VertexConsumer innerConsumer;
    // tells us info about where we probably are. should maybe expand to save more info if we use this class more.
    protected Vec3 lastVecWritten = new Vec3(0,0,0);

    // meant to be passed something like a block buffer
    public MiddleVertexConsumer(VertexConsumer innerConsumer) {
        this.innerConsumer = innerConsumer;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        lastVecWritten = new Vec3(x,y,z);
        this.innerConsumer.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.innerConsumer.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.innerConsumer.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.innerConsumer.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.innerConsumer.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        this.innerConsumer.setNormal(x, y, z);
        return this;
    }
}
