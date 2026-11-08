package com.samsthenerd.hexgloop.blocks.iotic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.blocks.akashic.AkashicFloodfiller;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.lib.HexBlocks;

// Hex 0.12: HexBlocks.* are Supplier<Block> themselves (pass () -> HexBlocks.X.get()),
// ADIotaHolder: readIotaTag()/HexIotaTypes.serialize gone -> readIota() returns Iota, plus writeable()
public class HexGloopIoticProviders {
    public static void register(){
        IoticHandler.registerIoticProvider(() -> HexBlocks.SLATE.get(), (world, pos) -> {
            Supplier<Iota> patternProvider = () -> {
                Iota patternIota = new NullIota();
                if (world.getBlockEntity(pos) instanceof BlockEntitySlate tile && tile.pattern != null) {
                    patternIota = new PatternIota(tile.pattern);
                }
                return patternIota;
            };
            if(world.getBlockState(pos).getValue(BlockCircleComponent.ENERGIZED)){
                Iota patternIota = patternProvider.get();
                return new ReadOnlyIotaHolder(patternIota);
            }
            return new ReadAndWriteIotaHolder(patternProvider, iota -> iota instanceof PatternIota || iota instanceof NullIota, iota -> {
                if(world.getBlockEntity(pos) instanceof BlockEntitySlate tile){
                    if(iota instanceof NullIota){
                        tile.pattern = null;
                    } else {
                        tile.pattern = ((PatternIota)iota).getPattern();
                    }
                    tile.sync();
                }
            });
        });

        IoticHandler.registerIoticProvider(() -> HexBlocks.AKASHIC_BOOKSHELF.get(), (world, pos) -> {
            Iota patternIota = new NullIota();
            if (world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile && tile.getPattern() != null) {
                patternIota = new PatternIota(tile.getPattern());
            }
            return new ReadOnlyIotaHolder(patternIota);
        });

        IoticHandler.registerIoticProvider(() -> HexBlocks.AKASHIC_RECORD.get(), (world, pos) -> {
            List<Iota> patterns = new ArrayList<>();
            AkashicFloodfiller.floodFillFor(pos, world, (testPos, bs, testWorld) -> {
                if(world.getBlockEntity(testPos) instanceof BlockEntityAkashicBookshelf bookshelf && bookshelf.getPattern() != null){
                    patterns.add(new PatternIota(bookshelf.getPattern()));
                }
                return false;
            });
            return new ReadOnlyIotaHolder(new ListIota(patterns));
        });
    }

    public static class ReadAndWriteIotaHolder implements ADIotaHolder{
        private Supplier<Iota> iotaProvider;
        private Predicate<Iota> iotaPredicate;
        private Consumer<Iota> iotaConsumer;
        public ReadAndWriteIotaHolder(Supplier<Iota> iotaProvider, Predicate<Iota> iotaPredicate, Consumer<Iota> iotaConsumer){
            this.iotaProvider = iotaProvider;
            this.iotaPredicate = iotaPredicate;
            this.iotaConsumer = iotaConsumer;
        }

        @Nullable
        public Iota readIota(){
            return iotaProvider.get();
        }

        public boolean writeable(){
            return true;
        }

        public boolean writeIota(@Nullable Iota iota, boolean simulate){
            if(iota != null && iotaPredicate.test(iota)){
                if(!simulate){
                    iotaConsumer.accept(iota);
                }
                return true;
            }
            return false;
        }
    }

    public static class ReadOnlyIotaHolder implements ADIotaHolder{
        private Iota iota;
        public ReadOnlyIotaHolder(Iota iota){
            this.iota = iota;
        }

        @Nullable
        public Iota readIota(){
            return iota;
        }

        public boolean writeable(){
            return false;
        }

        public boolean writeIota(@Nullable Iota iota, boolean simulate){
            return false;
        }
    }
}
