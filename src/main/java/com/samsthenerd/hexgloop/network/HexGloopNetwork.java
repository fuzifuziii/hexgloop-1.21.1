package com.samsthenerd.hexgloop.network;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.samsthenerd.hexgloop.HexGloop;
import com.samsthenerd.hexgloop.items.HexGloopItems;
import com.samsthenerd.hexgloop.items.ItemCastingFrog;
import com.samsthenerd.hexgloop.mixins.orchard.MixinServerPlayerOrchard;
import com.samsthenerd.hexgloop.network.booktweaks.BookScrollHandlers;
import com.samsthenerd.hexgloop.network.booktweaks.BookScrollHandlersClient;

import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.msgs.MsgOpenSpellGuiS2C;
import at.petrak.hexcasting.common.lib.hex.HexImageComponents;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;

public class HexGloopNetwork {
    public static final ResourceLocation CLEAR_GRID_PACKET_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "clear_grid");
    public static final ResourceLocation OPEN_CASTING_GRID_PACKET_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "open_casting_grid");
    public static final ResourceLocation CHANGE_WALL_SCROLL_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "change_wall_scroll");
    public static final ResourceLocation CLOSE_HEX_BOOK_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "close_hex_book");
    public static final ResourceLocation PROMPT_REPLACE_SCROLL_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "prompt_replace_scroll");
    public static final ResourceLocation S2C_GLOOPY_SERVER_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "s2c_gloopy_server");

    public static final ResourceLocation C2S_FROG_CASTING = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "c2s_frog_casting");


    public static final ResourceLocation C2S_UPDATE_ORCHARD_ID = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "ctos_update_orchard"); // the important one

    public static void register(){
        // clears the grid - maybe not actually super useful
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, CLEAR_GRID_PACKET_ID, (buf, context) -> {
            Player player = context.getPlayer();
            if(!(player instanceof ServerPlayer)) return;
            IXplatAbstractions.INSTANCE.clearCastingData((ServerPlayer) player);
        });
        
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, OPEN_CASTING_GRID_PACKET_ID, (buf, context) -> {
            Player player = context.getPlayer();
            if(!(player instanceof ServerPlayer)) return;
            ServerPlayer serverPlayer = (ServerPlayer) player;
            boolean clear = buf.readBoolean();
            if(clear) IXplatAbstractions.INSTANCE.clearCastingData((ServerPlayer) player);
            
            // same as ItemStaff#use in Hex 0.12
            CastingVM vm = IXplatAbstractions.INSTANCE.getStaffcastVM(serverPlayer, InteractionHand.MAIN_HAND);
            List<ResolvedPattern> patterns = IXplatAbstractions.INSTANCE.getPatternsSavedInUi(serverPlayer);
            Iota ravenmind = vm.getImage().getComponent(HexImageComponents.RAVENMIND.get());

            IXplatAbstractions.INSTANCE.sendPacketToPlayer(serverPlayer,
                new MsgOpenSpellGuiS2C(InteractionHand.MAIN_HAND, patterns, vm.getImage().getStack(), ravenmind,
                    vm.getImage().getParenCount()));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2S_UPDATE_ORCHARD_ID, (buf, context) -> {
            Player player = context.getPlayer();
            if(!(player instanceof ServerPlayer sPlayer)) return;
            String angleSig = buf.readUtf();
            boolean activated = buf.readBoolean();
            ((MixinServerPlayerOrchard)(Object)sPlayer).setAssociation(HexPattern.fromAngleString(angleSig, HexDir.EAST), activated);
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, CHANGE_WALL_SCROLL_ID, BookScrollHandlers::handleReplaceScroll);

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2S_FROG_CASTING, (buf, context) -> {
            Player player = context.getPlayer();
            ItemStack frogStackIsh = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            ItemStack frogStack = null;
            for(ItemStack maybeFrog : HexGloopItems.CASTING_FROG_ITEM.get().getEquippedFrogs(player)){
                if(ItemStack.matches(maybeFrog, frogStackIsh)){
                    frogStack = maybeFrog;
                    break;
                }
            }
            if(frogStack != null && frogStack.getItem() instanceof ItemCastingFrog frogItem){
                frogItem.cast(frogStack, player);
            }
        });

        registerGloopyServerSender();
    }

    public static void registerClientSideOnly(){
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLOSE_HEX_BOOK_ID, BookScrollHandlersClient::handleCloseClientBook);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PROMPT_REPLACE_SCROLL_ID, BookScrollHandlersClient::handleReplaceScrollPrompt);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2C_GLOOPY_SERVER_ID, ServerSideCheckClient::handleServerSideConfirmation);
    }

    private static void registerGloopyServerSender(){
        PlayerEvent.PLAYER_JOIN.register(player -> {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
            NetworkManager.sendToPlayer(player, HexGloopNetwork.S2C_GLOOPY_SERVER_ID, buf);
        });
    }
}
