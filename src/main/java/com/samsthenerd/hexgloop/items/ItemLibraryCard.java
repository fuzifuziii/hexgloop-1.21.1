package com.samsthenerd.hexgloop.items;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.compat.moreIotas.MoreIotasMaybeIotas;
import com.samsthenerd.hexgloop.utils.GloopUtils;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord;
import com.samsthenerd.hexgloop.utils.StackNBT;
import dev.architectury.platform.Platform;

public class ItemLibraryCard extends Item implements IotaHolderItem{

    public static String TAG_DIMENSION = "bound_dimension";
    public static ResourceLocation DIMENSION_PREDICATE = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "dimension");

    public ItemLibraryCard(Properties settings) {
        super(settings);
    }

    public void setDimension(ItemStack stack, ResourceKey<Level> dim){
        if(dim == null){
            StackNBT.remove(stack, TAG_DIMENSION);
            return;
        }
        StackNBT.putString(stack, TAG_DIMENSION, dim.location().toString());
    }

    @Nullable
    public ResourceKey<Level> getDimension(ItemStack stack){
        if(!StackNBT.hasString(stack, TAG_DIMENSION))
            return null;
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(StackNBT.getString(stack, TAG_DIMENSION)));
    }

    public static final List<ResourceLocation> DIMENSIONS = List.of(
        Level.OVERWORLD.location(), 
        Level.NETHER.location(), 
        Level.END.location()
    );

    public float getPredicateValue(ResourceKey<Level> dim){
        if(dim == null)
            return 0;

        // so that overworld is 0.01, nether is 0.02, end is 0.03, and we can add 96 more dimensions ! (not that I will, but i'd rather have the expandability than not)
        int dimIndex = DIMENSIONS.indexOf(dim.location());
        if(dimIndex != -1)
            return (dimIndex+1) * 0.01f;
        
        // return 1 for generic bound
        return 1;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        BlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof BlockAkashicRecord){
            ItemStack stack = context.getItemInHand();
            setDimension(stack, world.dimension());
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public boolean writeable(ItemStack stack){
        return true;
    }

    @Override
    @Nullable
    public Iota readIota(ItemStack stack){
        ResourceKey<Level> dim = getDimension(stack);
        if(Platform.isModLoaded("moreiotas") && dim != null){
            Iota maybeString = MoreIotasMaybeIotas.makeStringIota(dim.location().toString());
            return maybeString;
        }
        return null;
    }

    public boolean canWrite(ItemStack stack, @Nullable Iota iota){
        return iota == null;
    }

    /**
     * Write {@code null} to indicate erasing
     */
    public void writeDatum(ItemStack stack, @Nullable Iota iota){
        if(iota == null){
            setDimension(stack, null);
            return;
        }
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag context){
        ResourceKey<Level> dim = getDimension(stack);
        if(dim != null){
            tooltip.add(Component.translatable("item.hexgloop.library_card.tooltip_dimension", GloopUtils.prettyPrintID(dim.location())));
        }
    }
}
