package com.samsthenerd.hexgloop.misc;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.network.HexGloopNetwork;

import at.petrak.hexcasting.common.lib.HexSounds;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CastingRingHelperClient {
    public static void handleCastingRingKeypress(KeyMapping keyBinding, Minecraft client){
        if(!keyBinding.consumeClick()) return;
        if(client.player == null) return;
        if(!HexGloop.TRINKETY_INSTANCE.isCastingRingEquipped(client.player)) return;
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.core.RegistryAccess.EMPTY);
        if (client.player.isShiftKeyDown()) {
            client.player.playSound(HexSounds.CAST_FAILURE.value(), 1f, 1f);
            // need to add a packet to the server to clear it
            buf.writeBoolean(true);
        } else {
            buf.writeBoolean(false);
        }
        // need to add a packet to the server to open the casting screen
        NetworkManager.sendToServer(HexGloopNetwork.OPEN_CASTING_GRID_PACKET_ID, buf);
    }
}
