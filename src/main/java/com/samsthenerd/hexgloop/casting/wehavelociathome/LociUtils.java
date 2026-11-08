package com.samsthenerd.hexgloop.casting.wehavelociathome;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import com.samsthenerd.hexgloop.casting.wehavelociathome.modules.IMediaProviderLocus;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;

public class LociUtils {
    public static int getCircleMedia(BlockEntityAbstractImpetus impetus){
        return withdrawCircleMedia(impetus, -1, true, true);
    }

    // returns how much media is left unfulfilled
    // so if it withdraws the full amount it returns 0
    // if amount == -1 then return how much media there is -- silly but it keeps everything in one place, note that return value of 0 means infinite media
    public static int withdrawCircleMedia(BlockEntityAbstractImpetus impetus, int amount, boolean takeFromImpetus, boolean simulate){
        if(impetus.getMedia() < 0 && !simulate && takeFromImpetus) return 0; // for infinite media
        int amountRemaining = amount;
        if(amount == -1) amountRemaining = 0;
        List<Tuple<BlockPos, IMediaProviderLocus>> mediaProviders = ILociHandler.get(impetus).getTrackedModuleBlocks(IMediaProviderLocus.class);
        List<ADMediaHolder> mediaHolders = new ArrayList<>();
        // check through for infinite media
        for(Tuple<BlockPos, IMediaProviderLocus> mediaProvider : mediaProviders){
            ADMediaHolder mediaHolder = mediaProvider.getB().getMediaHolder(mediaProvider.getA(), impetus.getLevel(), impetus);
            if(mediaHolder == null) continue;
            if(mediaHolder.getMedia() < 0) return 0; // infinite media
            mediaHolders.add(mediaHolder);
        }
        // didn't find infinite media so go through and withdraw them
        for(ADMediaHolder mediaHolder : mediaHolders){
            if(amount == -1){
                amountRemaining += (int)Math.min(Integer.MAX_VALUE, mediaHolder.getMedia());
                continue;
            }
            int amtToTake = (int)Math.min((long)amountRemaining, mediaHolder.getMedia());
            amountRemaining -= amtToTake;
            if(!simulate)
                mediaHolder.setMedia(mediaHolder.getMedia() - amtToTake);
        }
        if(takeFromImpetus){
            if(amount == -1){
                amountRemaining += (int)Math.min(Integer.MAX_VALUE, impetus.getMedia());
            } else {
                int amtToTake = (int)Math.min((long)amountRemaining, impetus.getMedia());
                amountRemaining -= amtToTake;
                if(!simulate)
                    impetus.setMedia(impetus.getMedia() - amtToTake);
            }
        }
        return amountRemaining;
    }

    public static boolean addOrEmbedIota(CastingVM harness, Iota iota){
        // so we want to either embed the iota in a new paren or add it to existing or add it to the stack?
        if(iota instanceof PatternIota patternIota){
            // if it's a pattern iota deal with it elsewhere maybe ?
            return true;
        }
        // TODO(1.21 port): Hex 0.12 has no CastingVM#handleParentheses/applyFunctionalData; parentheses live in CastingImage now.
        // Only embed into an already-open paren; otherwise let the caller push the iota onto the stack itself.
        CastingImage image = harness.getImage();
        if(image.getParenCount() > 0){
            harness.setImage(image.withNewParenthesized(iota, false));
            return true;
        }
        return false; // ?
    }
}
