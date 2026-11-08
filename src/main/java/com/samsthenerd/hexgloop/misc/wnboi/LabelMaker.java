package com.samsthenerd.hexgloop.misc.wnboi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.utils.StackNBT;

// used for the labels on the wnboi screens
// each label type needs a way to go from iota/other input -> nbt -> chilling as object -> rendered
public class LabelMaker {
    public static final String LABELS_TAG = "HEXGLOOP:LABELS";
    public static final String LABEL_SIZE_TAG = "HEXGLOOP:LABEL_SIZE";
    private static final Map<ResourceLocation, LabelType<?> > LABEL_TYPE_MAP = new HashMap<ResourceLocation, LabelType<?> >();
    private static final Map<IotaType<?>, Function<Iota, Label>> IOTA_LABEL_MAP = new HashMap<IotaType<?>, Function<Iota, Label>>();

    private CompoundTag nbtLabels;
    private Map<Integer, Label> labelMap;

    public static void registerIotaLabelFunction(IotaType<?> type, Function<Iota, Label> function){
        IOTA_LABEL_MAP.put(type, function);
    }

    public static <L extends Label> LabelType<L> registerLabelType(LabelType<L> type){
        LABEL_TYPE_MAP.put(type.id, type);
        return type;
    }
    
    @Nullable
    public static Label fromNbt(CompoundTag nbt){
        if(!nbt.contains("type", Tag.TAG_STRING)) return null;
        ResourceLocation id = ResourceLocation.parse(nbt.getString("type"));
        LabelType<?> type = LABEL_TYPE_MAP.get(id);
        if(type == null) {
            return null;
        }
        return type.fromNbt(nbt);
    }

    @Nullable
    public static Label fromIota(Iota iota){
        Function<Iota, Label> function = IOTA_LABEL_MAP.get(iota.getType());
        if(function == null) return null;
        return function.apply(iota);
    }

    // returns whether or not it successfully put the label or not
    public static boolean putLabel(ItemStack stack, CompoundTag labelNbt, int index){
        // found issue: we never give the stack the LABEL_SIZE_TAG
        // if(!stack.hasNbt() || !stack.getNbt().contains(LABEL_SIZE_TAG, NbtElement.INT_TYPE)) return false;
        CompoundTag nbt = StackNBT.get(stack);
        if(nbt == null) return false;
        if(!nbt.contains(LABELS_TAG, Tag.TAG_COMPOUND)){
            nbt.put(LABELS_TAG, new CompoundTag());
        }
        CompoundTag labels = nbt.getCompound(LABELS_TAG);
        labels.put(Integer.toString(index), labelNbt);
        nbt.put(LABELS_TAG, labels);
        StackNBT.set(stack, nbt);
        return true;
    }

    public static boolean putLabel(ItemStack stack, Iota iota, int index){
        Label label = fromIota(iota);
        if(label == null) return putLabel(stack, new CompoundTag(), index);
        return putLabel(stack, label.toNbt(), index);
    }

    public static boolean putLabel(ItemStack stack, Label label, int index){
        if(label == null) return putLabel(stack, new CompoundTag(), index);
        return putLabel(stack, label.toNbt(), index);
    }

    // given an nbt from a stack
    public LabelMaker(CompoundTag nbt){
        if(nbt != null && nbt.contains(LABELS_TAG, Tag.TAG_COMPOUND)){
            nbtLabels = nbt.getCompound(LABELS_TAG);
        } else {
            nbtLabels = null;
        }
        labelMap = new HashMap<Integer, Label>();
    }

    public LabelMaker(ItemStack stack){
        this(StackNBT.get(stack));
    }

    @Nullable
    public Label getLabel(int index){
        if(nbtLabels == null || labelMap == null) return null;
        Label label = labelMap.get(index);
        if(label == null){ // no label in the map
            if(!nbtLabels.contains(Integer.toString(index), Tag.TAG_COMPOUND)) return null;
            CompoundTag labelNbt = nbtLabels.getCompound(Integer.toString(index));
            label = fromNbt(labelNbt);
            if(label == null) return null;
            labelMap.put(index, label);
        }
        return label;
    }
    
    // an instance of this is made for each label 
    public static interface Label{
        // render the label centered at x,y with the given width and height
        public void render(PoseStack matrixStack, int x, int y, int width, int height);

        public LabelType<?> getType();

        public default CompoundTag toNbt(){
            CompoundTag nbt = new CompoundTag();
            nbt.putString("type", getType().id.toString());
            return nbt;
        }
    }

    // handling nbt deserialization
    public static class LabelType<T extends Label>{
        public final ResourceLocation id;
        private final Function<CompoundTag, T> fromNbtFunction;

        public LabelType(ResourceLocation id, Function<CompoundTag, T> fromNbtFunction){
            this.id = id;
            this.fromNbtFunction = fromNbtFunction;
        }

        public T fromNbt(CompoundTag nbt){
            return fromNbtFunction.apply(nbt);
        }
    }
}
