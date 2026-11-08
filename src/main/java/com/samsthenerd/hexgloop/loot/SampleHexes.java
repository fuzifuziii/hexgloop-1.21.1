package com.samsthenerd.hexgloop.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import com.samsthenerd.hexgloop.items.HexGloopItems;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexItems;

public class SampleHexes {

    /*
     * regex for translating patterns from logs to here
     * find: HexPattern\((?<dir>[^ ]+) (?<pattern>[^ ]+)\) *
     * replace: new PatternIota(HexPattern.fromAngleString("$pattern", HexDir.$dir)),\n
     */
    public static SampleHex PLACE_LIGHT = register(
        new SampleHex(List.of(
            // get block looking at
            new PatternIota(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("aa", HexDir.EAST)),
            new PatternIota(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("wa", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("wqaawdd", HexDir.EAST)),
            //
            new PatternIota(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("aa", HexDir.EAST)),
            new PatternIota(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("wa", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("weddwaa", HexDir.EAST)),
            // add
            new PatternIota(HexPattern.fromAngleString("waaw", HexDir.NORTH_EAST)),
            // put light
            new PatternIota(HexPattern.fromAngleString("qqd", HexDir.NORTH_EAST))

        ), Component.translatable("hexgloop.lootitem.name.place_light"), Component.translatable("hexgloop.lootitem.desc.place_light"))
        .forItems(HexGloopItems.SCRIPT_ITEM.get())
        .withRarity(Rarity.UNCOMMON)
    );

    public static SampleHex HOVER = register(
        new SampleHex(List.of(
            new PatternIota(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("qqqqqew", HexDir.NORTH_WEST)),
            new PatternIota(HexPattern.fromAngleString("awqqqwaqw", HexDir.SOUTH_WEST)),
            new PatternIota(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("aqaaw", HexDir.SOUTH_EAST)),
            new PatternIota(HexPattern.fromAngleString("qqqqqawwawawd", HexDir.WEST))
        ), Component.translatable("hexgloop.lootitem.name.hover"), Component.translatable("hexgloop.lootitem.desc.hover"))
        .forItems(HexGloopItems.SCRIPT_ITEM.get())
        .withRarity(Rarity.RARE)
    );

    private static List<Iota> smeltItems = List.of(
        new PatternIota(HexPattern.fromAngleString("qqq", HexDir.WEST)),
        new PatternIota(HexPattern.fromAngleString("wqqqwqqadad", HexDir.EAST)),
        new PatternIota(HexPattern.fromAngleString("eee", HexDir.EAST)),
        new PatternIota(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST)),
        new PatternIota(HexPattern.fromAngleString("dd", HexDir.NORTH_EAST)),
        new PatternIota(HexPattern.fromAngleString("aqaawaa", HexDir.SOUTH_EAST)),
        new PatternIota(HexPattern.fromAngleString("qqqqqwdeddww", HexDir.SOUTH_EAST)),
        new PatternIota(HexPattern.fromAngleString("dadad", HexDir.NORTH_EAST)));

    public static SampleHex LESSER_SMELT = register(
        new SampleHex(smeltItems, Component.translatable("hexgloop.lootitem.name.lesser_smelt"), Component.translatable("hexgloop.lootitem.desc.lesser_smelt"))
        .forItems(HexGloopItems.CASTING_POTION_ITEM.get())
        .withRarity(Rarity.RARE)
        .withMediaAmt(MediaConstants.DUST_UNIT * 48)
        .requiredMods("hexal")
        .withPigments(new FrozenPigment(HexItems.DYE_PIGMENTS.get(DyeColor.ORANGE).get().getDefaultInstance(), new UUID(0,0)))
    );
    public static SampleHex GREATER_SMELT = register(
        new SampleHex(smeltItems, Component.translatable("hexgloop.lootitem.name.greater_smelt"), Component.translatable("hexgloop.lootitem.desc.greater_smelt"))
        .forItems(HexGloopItems.CASTING_POTION_ITEM.get())
        .withRarity(Rarity.EPIC)
        .withMediaAmt(MediaConstants.DUST_UNIT * 48 * 4)
        .requiredMods("hexal")
        .withPigments(new FrozenPigment(HexItems.DYE_PIGMENTS.get(DyeColor.RED).get().getDefaultInstance(), new UUID(0,0)))
    );


    public static final Map<Tuple<Item, Rarity>, Set<SampleHex>> SAMPLE_HEXES = new HashMap<>();

    public static SampleHex register(SampleHex hex){
        for(Item item : hex.forItems){
            Tuple<Item, Rarity> keyPair = new Tuple<>(item, hex.rarity);
            if(!SAMPLE_HEXES.containsKey(keyPair)){
                SAMPLE_HEXES.put(keyPair, new HashSet<SampleHex>());
            }
            SAMPLE_HEXES.get(keyPair).add(hex);
        }
        return hex;
    }
}
