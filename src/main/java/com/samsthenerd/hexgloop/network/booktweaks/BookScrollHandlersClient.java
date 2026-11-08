package com.samsthenerd.hexgloop.network.booktweaks;



import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.misc.GetPatchouliPatterns;
import com.samsthenerd.hexgloop.network.HexGloopNetwork;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookEntry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.item.ItemModBook;

public class BookScrollHandlersClient {
    public static void handleCloseClientBook(RegistryFriendlyByteBuf buf, PacketContext context){
        // should be fine ? maybe build in some more protections if it's an issue
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen instanceof GuiBook){
            mc.screen.onClose();
            mc.setScreen(null);
        }
    }

    public static void handleReplaceScrollPrompt(RegistryFriendlyByteBuf buf, PacketContext context){
        Player player = context.getPlayer();
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        Book book = ItemModBook.getBook(stack);
        if(book == null) return;
        GuiBook currentGui = book.getContents().getCurrentGui();
        if(currentGui instanceof GuiBookEntry guiEntry){
            BookEntry entry = guiEntry.getEntry();
            int spread = guiEntry.getSpread();
            Tuple<HexPattern, HexPattern> patterns = GetPatchouliPatterns.getPatternsFromEntry(entry, spread);
            if(patterns.getA() == null && patterns.getB() == null) return;
            // atleast one must be non-null
            HexPattern patternToUse = patterns.getA();
            if((hand == InteractionHand.MAIN_HAND && patterns.getB() != null) || patternToUse == null){
                patternToUse = patterns.getB();
            }
            HexGloop.logPrint("got pattern: " + patternToUse.toString());
            Minecraft mc = Minecraft.getInstance();
            Entity maybeWallScroll = null;
            if(mc.hitResult.getType() == HitResult.Type.ENTITY){
                EntityHitResult entResult = (EntityHitResult) mc.hitResult;
                float reachDist = (float) player.entityInteractionRange();
                if(mc.hitResult.distanceTo(player) > (reachDist * reachDist)){
                    return; // don't do anything if the player is too far away
                }
                maybeWallScroll = entResult.getEntity();
            }
            if(maybeWallScroll instanceof EntityWallScroll wallScroll){
                RegistryFriendlyByteBuf bufC2S = new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.core.RegistryAccess.EMPTY);
                HexGloop.logPrint("correctly got a wall scroll !");
                bufC2S.writeUUID(wallScroll.getUUID());
                bufC2S.writeNbt(HexPattern.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, patternToUse).result().orElseThrow()); // TODO(1.21 port): wire format must match server-side decoder (HexPattern.CODEC)
                NetworkManager.sendToServer(HexGloopNetwork.CHANGE_WALL_SCROLL_ID, bufC2S);
                return;
            }
        }
    }
}
