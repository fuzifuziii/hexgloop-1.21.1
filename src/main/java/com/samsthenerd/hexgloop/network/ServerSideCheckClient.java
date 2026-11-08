package com.samsthenerd.hexgloop.network;

import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.HexGloopClient;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import vazkii.patchouli.api.PatchouliAPI;

public class ServerSideCheckClient {
    public static void handleServerSideConfirmation(RegistryFriendlyByteBuf buf, PacketContext context){
        HexGloopClient.isServerSide = true;
        PatchouliAPI.get().setConfigFlag("hexgloop:gloopyserver", true);
        HexGloop.logPrint("gloop is on server");
    }

    // 1.21: Patchouli#setConfigFlag only stores the flag in PatchouliConfig, it does not rebuild the
    // already loaded book. Every hexgloop entry is gated by "flag": "hexgloop:gloopyserver", so without
    // an explicit reload the whole HexGloop section stays hidden until a resource reload (F3+T).
    private static void reloadBookEntries(){
        try {
            vazkii.patchouli.client.book.ClientBookRegistry.INSTANCE.reload();
        } catch (Exception e) {
            HexGloop.LOGGER.error("Failed to reload Patchouli book after setting gloopyserver flag", e);
        }
    }

    public static void registerDisconnectUpdate(){
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> {
            if (net.minecraft.client.Minecraft.getInstance().hasSingleplayerServer()) {
                HexGloopClient.isServerSide = true;
                PatchouliAPI.get().setConfigFlag("hexgloop:gloopyserver", true);
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            HexGloopClient.isServerSide = false;
            PatchouliAPI.get().setConfigFlag("hexgloop:gloopyserver", false);
            HexGloop.logPrint("gloop is not on server");
        });
    }
}
