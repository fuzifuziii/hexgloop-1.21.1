package com.samsthenerd.hexgloop.misc.wnboi;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.samsthenerd.hexgloop.compat.hexal.HexalLabels;
import com.samsthenerd.hexgloop.compat.moreIotas.MoreIotasLabels;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker.Label;
import com.samsthenerd.hexgloop.misc.wnboi.LabelMaker.LabelType;
import com.samsthenerd.hexgloop.misc.wnboi.LabelTypes.PatternLabel.PatternOptions;
import com.samsthenerd.hexgloop.utils.ClientUtils;
import com.samsthenerd.hexgloop.utils.GloopyRenderUtils;
import com.samsthenerd.wnboi.utils.RenderUtils;

import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// todo: clean up the label system a lot
public class LabelTypes {
    public static LabelType<TextLabel> TEXT_LABEL_TYPE = LabelMaker.registerLabelType(
        new LabelType<TextLabel>(ResourceLocation.fromNamespaceAndPath("hexgloop", "text_label"), nbt -> {
            if(nbt == null || !nbt.contains("type", Tag.TAG_STRING) || !nbt.getString("type").equals("hexgloop:text_label")) return null;
            if(!nbt.contains("text", Tag.TAG_STRING)) return null;
            Component text = Component.Serializer.fromJson(nbt.getString("text"), com.samsthenerd.wnboi.items.DynamicMultiItem.registries());
            return new TextLabel(text);
        })
    );

    public static LabelType<ItemLabel> ITEM_LABEL_TYPE = LabelMaker.registerLabelType(
        new LabelType<ItemLabel>(ResourceLocation.fromNamespaceAndPath("hexgloop", "item_label"), nbt -> {
            if(nbt == null || !nbt.contains("type", Tag.TAG_STRING) || !nbt.getString("type").equals("hexgloop:item_label")) return null;
            if(!nbt.contains("item", Tag.TAG_COMPOUND)) return null;
            ItemStack stack = ItemStack.parseOptional(com.samsthenerd.wnboi.items.DynamicMultiItem.registries(), nbt.getCompound("item"));
            if(stack == null) return null;
            return new ItemLabel(stack);
        })
    );

    public static LabelType<EntityLabel> ENTITY_LABEL_TYPE = LabelMaker.registerLabelType(
        new LabelType<EntityLabel>(ResourceLocation.fromNamespaceAndPath("hexgloop", "entity_label"), nbt -> {
            if(nbt == null || !nbt.contains("type", Tag.TAG_STRING) || !nbt.getString("type").equals("hexgloop:entity_label")) return null;
            if(!nbt.contains("entity", Tag.TAG_COMPOUND)) return null;
            Entity entity;
            if(nbt.getCompound("entity").contains("player", Tag.TAG_STRING)){
                UUID playerUUID = UUID.fromString(nbt.getCompound("entity").getString("player"));
                if(Platform.getEnvironment() == dev.architectury.utils.Env.CLIENT){
                    entity = ClientUtils.makeOtherClientPlayer(playerUUID);
                } else {
                    return new EntityLabel(playerUUID);
                }
            } else {
                EntityType<?> entType = EntityType.by(nbt.getCompound("entity")).orElse(null);
                if(entType == null) return null;
                if(Platform.getEnvironment() == dev.architectury.utils.Env.CLIENT){
                    entity = EntityType.create(nbt.getCompound("entity"), ClientUtils.getClientWorld()).orElse(null);
                } else {
                    return new EntityLabel(nbt.getCompound("entity"));
                }
            }
            if(entity == null) return null;
            entity.setYBodyRot(215);
            entity.setYHeadRot(215);
            return new EntityLabel(entity);
        })
    );

    public static LabelType<PatternLabel> PATTERN_LABEL_TYPE = LabelMaker.registerLabelType(
        new LabelType<PatternLabel>(ResourceLocation.fromNamespaceAndPath("hexgloop", "pattern_label"), nbt -> {
            if(nbt == null || !nbt.contains("type", Tag.TAG_STRING) || !nbt.getString("type").equals("hexgloop:pattern_label")) return null;
            if(!nbt.contains("pattern", Tag.TAG_COMPOUND)) return null;
            HexPattern pattern = HexPattern.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, nbt.getCompound("pattern")).result().orElse(null);
            if(pattern == null) return null;
            if(nbt.contains("options", Tag.TAG_COMPOUND)){
                return new PatternLabel(pattern, PatternOptions.fromNbt(nbt.getCompound("options")));
            }
            return new PatternLabel(pattern);
        })
    );
    

    public static class TextLabel implements Label{
        public Component text;

        public TextLabel(Component text){
            this.text = text;
        }

        @Override
        public void render(PoseStack matrices, int x, int y, int width, int height){
            Font textRenderer = Minecraft.getInstance().font;
            List<FormattedCharSequence> lines = textRenderer.split(text, width);
            int totalHeight = textRenderer.lineHeight * lines.size();
            int yOffset = (height - totalHeight) / 2;
            int maxWidth = 0;
            for(FormattedCharSequence line: lines){
                maxWidth = Math.max(textRenderer.width(line), maxWidth);
            }
            int xOffset = (width - maxWidth) / 2;
            for(FormattedCharSequence line : lines){
                textRenderer.drawInBatch(line, x + xOffset - width/2, y + yOffset - height /2 + textRenderer.lineHeight/2, 0xFFFFFF, false, matrices.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
                yOffset += textRenderer.lineHeight;
            }
        }

        @Override
        public LabelType<?> getType(){
            return TEXT_LABEL_TYPE;
        }

        @Override
        public CompoundTag toNbt(){
            CompoundTag nbt = Label.super.toNbt();
            nbt.putString("text", Component.Serializer.toJson(text, com.samsthenerd.wnboi.items.DynamicMultiItem.registries()));
            return nbt;
        }
    }

    public static class ItemLabel implements Label{
        public ItemStack stack;

        public ItemLabel(ItemStack stack){
            this.stack = stack;
        }

        @Override
        public void render(PoseStack matrices, int x, int y, int width, int height){
            int size = Math.min(width, height);
            GloopyRenderUtils.renderGuiItemIcon(stack, x-size/2, y-size/2, size);
        }

        @Override
        public LabelType<?> getType(){
            return ITEM_LABEL_TYPE;
        }

        @Override
        public CompoundTag toNbt(){
            CompoundTag nbt = Label.super.toNbt();
            nbt.put("item", stack.saveOptional(com.samsthenerd.wnboi.items.DynamicMultiItem.registries()));
            return nbt;
        }
    }

    public static class EntityLabel implements Label{
        public Entity entity = null;
        public UUID playerUUID = null;
        public CompoundTag entityNbt = null;

        public EntityLabel(Entity entity){
            this.entity = entity;
        }

        public EntityLabel(UUID uuid){
            this.playerUUID = uuid;
        }

        public EntityLabel(CompoundTag entityNbt){
            this.entityNbt = entityNbt;
        }

        @Override
        public void render(PoseStack matrices, int x, int y, int width, int height){
            int size = (int)Math.min(width/entity.getBbWidth(), height/entity.getBbHeight());
            RenderUtils.drawEntity(x, y+height/2, size, entity);
        }

        @Override
        public LabelType<?> getType(){
            return ENTITY_LABEL_TYPE;
        }

        @Override
        public CompoundTag toNbt(){
            CompoundTag nbt = Label.super.toNbt();
            CompoundTag entNbt = new CompoundTag();
            if(entity== null && playerUUID == null && entityNbt != null){
                nbt.put("entity", entityNbt);
                return nbt;
            }
            if(entity == null && playerUUID != null){
                entNbt.putString("player", playerUUID.toString());
                nbt.put("entity", entNbt);
                return nbt;
            }
            if(entity instanceof Player player){
                entNbt.putString("player", player.getStringUUID());
            } else {
                entity.saveAsPassenger(entNbt);
            }
            nbt.put("entity", entNbt);
            return nbt;
        }
    }

    public static class PatternLabel implements Label{
        public HexPattern pattern;
        public PatternOptions options;

        public PatternLabel(HexPattern pattern, PatternOptions options){
            this.pattern = pattern;
            this.options = options;
        }

        public PatternLabel(HexPattern pattern){
            this(pattern, new PatternOptions());
        }

        @Override
        public void render(PoseStack matrices, int x, int y, int width, int height){
            GloopyRenderUtils.drawPattern(matrices, pattern, width, height, x, y, 
                options.outerColor == null ? 0xFF_FFFFFF : options.outerColor.intValue(),
                options.innerColorMain == null ? 0xFF_FFFFFF : options.innerColorMain.intValue(),
                options.innerColorAccent == null ? 0xFF_FFFFFF : options.innerColorAccent.intValue(),
                options.dotColor == null ? 0xFF_FFFFFF : options.dotColor.intValue(),
                options.speed == null ? 0 : options.speed.floatValue(),
                options.variance == null ? 0 : options.variance.floatValue(),
                options.hasStartingPoint == null ? false : options.hasStartingPoint.booleanValue()
            );
        }

        @Override
        public LabelType<?> getType(){
            return PATTERN_LABEL_TYPE;
        }

        @Override
        public CompoundTag toNbt(){
            CompoundTag nbt = Label.super.toNbt();
            nbt.put("pattern", HexPattern.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, pattern).result().orElse(new CompoundTag()));
            nbt.put("options", options.toNbt());
            return nbt;
        }

        // lots of options ! most prob won't be accessible in game but i might use them for fidgets
        public static class PatternOptions{
            public Integer outerColor; public Integer innerColorMain; public Integer innerColorAccent;
            public Float speed; public Float variance; public Boolean hasStartingPoint; public Integer dotColor;

            public PatternOptions(Integer outerColor, Integer innerColorMain, Integer innerColorAccent, Integer dotColor, Float speed, Float variance, Boolean hasStartingPoint){
                this.outerColor = outerColor;
                this.innerColorMain = innerColorMain;
                this.innerColorAccent = innerColorAccent;
                this.speed = speed;
                this.variance = variance;
                this.hasStartingPoint = hasStartingPoint;
                this.dotColor = dotColor;
            }

            public PatternOptions(){
                this(null, null, null, null, null, null, null);
            }

            public static PatternOptions fromNbt(CompoundTag nbt){
                PatternOptions options = new PatternOptions();
                if(nbt.contains("outerColor", Tag.TAG_INT)) options.outerColor = nbt.getInt("outerColor");
                if(nbt.contains("innerColorMain", Tag.TAG_INT)) options.innerColorMain = nbt.getInt("innerColorMain");
                if(nbt.contains("innerColorAccent", Tag.TAG_INT)) options.innerColorAccent = nbt.getInt("innerColorAccent");
                if(nbt.contains("dotColor", Tag.TAG_INT)) options.dotColor = nbt.getInt("dotColor");
                if(nbt.contains("speed", Tag.TAG_FLOAT)) options.speed = nbt.getFloat("speed");
                if(nbt.contains("variance", Tag.TAG_FLOAT)) options.variance = nbt.getFloat("variance");
                if(nbt.contains("hasStartingPoint", Tag.TAG_BYTE)) options.hasStartingPoint = nbt.getBoolean("hasStartingPoint");
                return options;
            }

            public CompoundTag toNbt(){
                CompoundTag nbt = new CompoundTag();
                if(outerColor != null) nbt.putInt("outerColor", outerColor);
                if(innerColorMain != null) nbt.putInt("innerColorMain", innerColorMain);
                if(innerColorAccent != null) nbt.putInt("innerColorAccent", innerColorAccent);
                if(dotColor != null) nbt.putInt("dotColor", dotColor);
                if(speed != null) nbt.putFloat("speed", speed);
                if(variance != null) nbt.putFloat("variance", variance);
                if(hasStartingPoint != null) nbt.putBoolean("hasStartingPoint", hasStartingPoint);
                return nbt;
            }
        }
    }

    // now the iota transforming functions

    public static void registerIotaLabelFunctions(){
        LabelMaker.registerIotaLabelFunction(DoubleIota.TYPE, LabelTypes::doubleToTextLabel);
        LabelMaker.registerIotaLabelFunction(PatternIota.TYPE, LabelTypes::patternToLabel);
        LabelMaker.registerIotaLabelFunction(EntityIota.TYPE, LabelTypes::entityToLabel);
        if(Platform.isModLoaded("hexal")){
            HexalLabels.register();
        }
        if(Platform.isModLoaded("moreiotas")){
            MoreIotasLabels.register();
        }
    }

    @Nullable
    public static TextLabel doubleToTextLabel(Iota iota){
        if(!(iota instanceof DoubleIota)) return null;
        DoubleIota di = (DoubleIota) iota;
        Component text = Component.literal(Double.toString(di.getDouble()));
        return new TextLabel(text);
    }

    @Nullable
    public static PatternLabel patternToLabel(Iota iota){
        if(!(iota instanceof PatternIota)) return null;
        PatternIota patIota = (PatternIota) iota;
        HexPattern pattern = patIota.getPattern();
        if(pattern == null) return null;
        return new PatternLabel(pattern);
    }

    @Nullable
    public static Label entityToLabel(Iota iota){
        if(!(iota instanceof EntityIota)) return null;
        EntityIota entityIota = (EntityIota) iota;
        Entity entity = null;
        if(ClientUtils.getClientWorld() instanceof net.minecraft.server.level.ServerLevel sLevel){
            entity = entityIota.getEntity(sLevel);
        } else if(ClientUtils.getClientWorld() instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel){
            for(Entity e : clientLevel.entitiesForRendering()){
                if(e.getUUID().equals(entityIota.getEntityId())){ entity = e; break; }
            }
        }
        if(entity == null) return null;
        if(entity instanceof ItemEntity itemEnt){
            ItemStack stack = itemEnt.getItem();
            return new ItemLabel(stack);
        } else {
            return new EntityLabel(entity);
        }
    }
}
