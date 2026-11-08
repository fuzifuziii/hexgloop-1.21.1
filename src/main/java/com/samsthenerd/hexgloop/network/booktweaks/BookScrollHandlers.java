package com.samsthenerd.hexgloop.network.booktweaks;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.network.HexGloopNetwork;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import io.netty.buffer.Unpooled;

public class BookScrollHandlers {
    public static void handleReplaceScroll(RegistryFriendlyByteBuf buf, PacketContext context){
        HexGloop.logPrint("received wall scroll change");
            Player player = context.getPlayer();
            if(!(player instanceof ServerPlayer)) return;
            HexGloop.logPrint("got a server player");
            ServerPlayer serverPlayer = (ServerPlayer) player;
            UUID scrollUUID = buf.readUUID();
            CompoundTag patternNbt = buf.readNbt();
            HexPattern pattern = null;
            try{
                pattern = HexPattern.CODEC.parse(NbtOps.INSTANCE, patternNbt).result().orElse(null);
            } catch (Exception e){
                HexGloop.logPrint("failed to parse pattern");
                e.printStackTrace();
                return;
            }
            if(pattern == null) return;
            Entity maybeWallScroll = serverPlayer.serverLevel().getEntity(scrollUUID);
            HexGloop.logPrint("got down to a maybe scroll");
            if(maybeWallScroll == null){
                HexGloop.logPrint("maybe scroll is null");
                return;
            }
            if(maybeWallScroll == null || serverPlayer.position().distanceTo(maybeWallScroll.position()) > 32) return;
            HexGloop.logPrint("we have a valid maybe wall scroll");
            if(maybeWallScroll instanceof EntityWallScroll wallScroll){
                // do this up here to try to get every tiny bit of time we can ?
                NetworkManager.sendToPlayer(serverPlayer, HexGloopNetwork.CLOSE_HEX_BOOK_ID, new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.core.RegistryAccess.EMPTY));
                ItemStack newScroll = wallScroll.scroll.copy();
                HexGloop.logPrint("changeing out scroll now");
                EntityWallScroll newWallScroll = new EntityWallScroll(wallScroll.level(), wallScroll.blockPosition(),
                    wallScroll.getDirection(), newScroll, wallScroll.getShowsStrokeOrder(), wallScroll.blockSize);
                newWallScroll.lerpTo(wallScroll.position().x(), wallScroll.position().y(), wallScroll.position().z(), 
                wallScroll.getYRot(), wallScroll.getXRot(), 0);
                newWallScroll.setUUID(wallScroll.getUUID()); // keep the same uuid so we don't break hexes that rely on it
                // Hex 0.12: ItemScroll.TAG_PATTERN and item NBT are gone; writeDatum stores the pattern in HexDataComponents.PATTERN
                if(newScroll.getItem() instanceof ItemScroll scrollItem){
                    scrollItem.writeDatum(newScroll, new PatternIota(pattern));
                }
                newWallScroll.recalculateDisplay();
                wallScroll.discard();
                serverPlayer.serverLevel().addFreshEntity(newWallScroll);
                // wallScroll.scroll = newScroll;
                // IXplatAbstractions.INSTANCE.sendPacketNear(wallScroll.getPos(), 32.0, serverPlayer.getWorld(),
                //     new MsgRecalcWallScrollDisplayAck(wallScroll.getId(), wallScroll.getShowsStrokeOrder()));
            }
    }
}
