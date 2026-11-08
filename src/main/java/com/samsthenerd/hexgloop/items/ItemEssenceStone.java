package com.samsthenerd.hexgloop.items;

import java.util.UUID;

import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import com.samsthenerd.hexgloop.HexGloop;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ItemEssenceStone extends Item implements PigmentItem, IotaHolderItem{

    public static ResourceLocation ESSENCE_PREDICATE = ResourceLocation.fromNamespaceAndPath("hexgloop", "has_essence");
    public static String HAS_ESSENCE = "has_essence";
    public static String PRIMARY_COLOR = "primary_color";
    public static String SECONDARY_COLOR = "secondary_color";
    public static final String TAG_DATA = "data";

    public ItemEssenceStone(Properties settings){
        super(settings);
    }

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner){
        Tuple<Integer, Integer> colors = getColors(stack);
        int[] colorsArray = {colors.getA(), colors.getB()};
        return new ColorProvider() {
            @Override
            protected int getRawColor(float time, Vec3 position) {
                return ADPigment.morphBetweenColors(colorsArray, new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
            }
        };
    }

    @Override
    public boolean writeable(ItemStack stack){
        return true;
    }

    public boolean canWrite(ItemStack stack, @Nullable Iota iota){
        return true;
    }

    public void writeDatum(ItemStack stack, @Nullable Iota iota){
        if(iota == null){
            clearEssence(stack);
            return;
        }
        StackNBT.putIota(stack, TAG_DATA, iota);
        if(iota instanceof EntityIota eIota){
            Entity ent = resolveEntity(eIota);
            if(ent == null){
                int iotaColor = iota.getType().color();
                setColors(stack, iotaColor, iotaColor);
                return;
            }
            SpawnEggItem spawnEgg = SpawnEggItem.byId(ent.getType());
            if(spawnEgg == null){
                spawnEgg = HexGloop.GLOOPXPLAT.getForgeSpawnEggForEntity(ent.getType());
            }
            if(spawnEgg != null){
                int primaryColor = spawnEgg.getColor(0);
                int secondaryColor = spawnEgg.getColor(1);
                setColors(stack, primaryColor, secondaryColor);
                return;
            }
        }
        int iotaColor = iota.getType().color();
        setColors(stack, iotaColor, iotaColor);
    }

    // Hex 0.12: EntityIota.getEntity needs a ServerLevel, search all server levels
    @Nullable
    private static Entity resolveEntity(EntityIota eIota){
        var server = ServerLifecycleHooks.getCurrentServer();
        if(server == null) return null;
        for(ServerLevel lvl : server.getAllLevels()){
            Entity ent = eIota.getEntity(lvl);
            if(ent != null) return ent;
        }
        return null;
    }

    @Override
    @Nullable
    public Iota readIota(ItemStack stack){
        return null; // i don't really want it to be a focus
    }

    public void clearEssence(ItemStack stack){
        if(StackNBT.contains(stack, HAS_ESSENCE, Tag.TAG_INT)){
            StackNBT.putInt(stack, HAS_ESSENCE, 0);
        }
    }

    public boolean hasEssence(ItemStack stack){
        return StackNBT.contains(stack, HAS_ESSENCE, Tag.TAG_INT) && StackNBT.getInt(stack, HAS_ESSENCE) == 1;
    }

    public Tuple<Integer, Integer> getColors(ItemStack stack){
        if(hasEssence(stack) && StackNBT.contains(stack, PRIMARY_COLOR, Tag.TAG_INT)
        && StackNBT.contains(stack, SECONDARY_COLOR, Tag.TAG_INT)){
            return new Tuple<Integer, Integer>(StackNBT.getInt(stack, PRIMARY_COLOR), StackNBT.getInt(stack, SECONDARY_COLOR));
        }
        return new Tuple<Integer, Integer>(0x564661, 0x564661);
    }

    public void setColors(ItemStack stack, int primary, int secondary){
        StackNBT.edit(stack, nbt -> {
            nbt.putInt(PRIMARY_COLOR, primary);
            nbt.putInt(SECONDARY_COLOR, secondary);
            nbt.putInt(HAS_ESSENCE, 1);
        });
    }
}
