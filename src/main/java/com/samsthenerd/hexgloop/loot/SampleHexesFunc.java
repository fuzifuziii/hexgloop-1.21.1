package com.samsthenerd.hexgloop.loot;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexRegistries;
import net.minecraft.tags.TagKey;

public class SampleHexesFunc extends LootItemConditionalFunction {
    public static final MapCodec<SampleHexesFunc> CODEC = RecordCodecBuilder.mapCodec(inst ->
        commonFields(inst).apply(inst, SampleHexesFunc::new));

    // register this on Registries.LOOT_FUNCTION_TYPE if/when it's used by a loot table
    public static final LootItemFunctionType<SampleHexesFunc> TYPE = new LootItemFunctionType<>(CODEC);

    public SampleHexesFunc(List<LootItemCondition> lootItemConditions) {
        super(lootItemConditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        var rand = ctx.getRandom();
        // 1.21 hex: per-world patterns are the actions in the hexcasting:per_world_pattern tag
        var lookup = ctx.getLevel().registryAccess().lookupOrThrow(HexRegistries.ACTION);
        TagKey<ActionRegistryEntry> tag = TagKey.create(HexRegistries.ACTION, HexAPI.modLoc("per_world_pattern"));
        List<Holder<ActionRegistryEntry>> entries = new ArrayList<>();
        lookup.get(tag).ifPresent(set -> set.forEach(entries::add));
        if(entries.isEmpty()) return stack;
        var chosen = entries.get(rand.nextInt(entries.size()));
        var key = chosen.unwrapKey();
        if(key.isEmpty()) return stack;
        return ItemScroll.withPerWorldPattern(stack, key.get());
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return TYPE;
    }
}
