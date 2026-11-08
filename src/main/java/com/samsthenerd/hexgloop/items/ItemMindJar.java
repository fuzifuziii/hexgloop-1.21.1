package com.samsthenerd.hexgloop.items;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import com.samsthenerd.hexgloop.utils.StackNBT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

public class ItemMindJar extends Item implements IFlayableItem{

    public ItemMindJar(Properties settings) {
        super(settings);
    }

    @Override
    public Villager getFlayableVillager(ItemStack stack, @Nullable ItemEntity itemEnt, CastingEnvironment ctx){
        CompoundTag entNbt = StackNBT.getCompound(stack, IMindTargetItem.STORED_MIND_TAG);
        Optional<Entity> maybeEnt = EntityType.create(entNbt, ctx.getWorld());
        if(maybeEnt.orElse(null) instanceof Villager villager){
            Villager clone = new Villager(EntityType.VILLAGER, ctx.getWorld());
            clone.setVillagerData(villager.getVillagerData());
            if(itemEnt != null){
                clone.setPosRaw(itemEnt.getX(), itemEnt.getY(), itemEnt.getZ());
            } else {
                clone.setPosRaw(ctx.mishapSprayPos().x(), ctx.mishapSprayPos().y(), ctx.mishapSprayPos().z());
            }
            return clone;
        }
        return null;
    }

    // not in the creative tab (HexGloopItems decides tab contents)

    @Override
    public void handleBrainsweep(ItemStack stack, @Nullable ItemEntity itemEnt, CastingEnvironment ctx, Consumer<ItemStack> resultConsumer){
        stack.shrink(1);
        resultConsumer.accept(new ItemStack(HexGloopItems.EMPTY_JAR_ITEM.get()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag context) {
        Level world = tooltipContext.level();
        if(world != null && StackNBT.hasCompound(stack, IMindTargetItem.STORED_MIND_TAG)){
            CompoundTag entNbt = StackNBT.getCompound(stack, IMindTargetItem.STORED_MIND_TAG);
            Optional<Entity> maybeEnt = EntityType.create(entNbt, world);
            if(maybeEnt.orElse(null) instanceof Villager villager){
                VillagerData vData = villager.getVillagerData();
                if(vData.getProfession() == VillagerProfession.NONE){
                    tooltip.add(Component.translatable("item.hexgloop.mind_jar.tooltip.nojob"));
                } else {
                    Component jobLevel = Component.translatable("merchant.level." + vData.getLevel());
                    Component job = Component.translatable("entity.minecraft.villager." + vData.getProfession());
                    Component full = Component.translatable("item.hexgloop.mind_jar.tooltip", jobLevel, job);
                    tooltip.add(full);
                }
                return;
            }
        }
        tooltip.add(Component.translatable("item.hexgloop.mind_jar.tooltip.empty"));
    }
}
