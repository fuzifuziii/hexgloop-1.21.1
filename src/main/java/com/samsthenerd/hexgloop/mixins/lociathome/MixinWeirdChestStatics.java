package com.samsthenerd.hexgloop.mixins.lociathome;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestBlock.class)
public interface MixinWeirdChestStatics {
    @Accessor("CHEST_COMBINER")
    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>> getInventoryRetriever(){
        throw new AssertionError();
    }

    @Accessor("MENU_PROVIDER_COMBINER")
    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> getNameRetriever(){
        throw new AssertionError();
    }


}
