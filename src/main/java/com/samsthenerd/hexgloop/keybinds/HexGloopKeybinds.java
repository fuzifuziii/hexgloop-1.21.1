package com.samsthenerd.hexgloop.keybinds;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import com.samsthenerd.hexgloop.items.ItemFidget;
import com.samsthenerd.hexgloop.items.ItemMultiFocus;
import com.samsthenerd.hexgloop.misc.CastingRingHelperClient;
import com.samsthenerd.hexgloop.mixins.wnboi.MixinIsScrollableInvoker;
import com.samsthenerd.wnboi.interfaces.KeyboundItem;
import com.samsthenerd.wnboi.utils.KeybindUtils;
import com.samsthenerd.wnboi.utils.KeybindUtils.KeybindHandler;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HexGloopKeybinds {
    public static final KeyMapping IOTA_WHEEL_KEYBIND = new KeyMapping("key.hexgloop.open_iota_wheel",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.hexgloop");
    // public static final KeyBinding FIDGET_WHEEL_KEYBIND = new KeyBinding("key.hexgloop.open_fidget_wheel",
	// 		InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.hexgloop");
    public static final KeyMapping CASTING_RING_KEY_BINDING = new KeyMapping("key.hexgloop.casting_ring",
    InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.hexgloop");

    public static final KeyMapping HEX_SCROLL_UP = new KeyMapping("key.hexgloop.scroll_up", 
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.hexgloop");
    public static final KeyMapping HEX_SCROLL_DOWN = new KeyMapping("key.hexgloop.scroll_down",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, "key.categories.hexgloop");

    public static final KeyMapping OPEN_HEX_BOOK = new KeyMapping("key.hexgloop.open_hex_book",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.hexgloop");

    public static void registerKeybinds(){
        registerKeybind(IOTA_WHEEL_KEYBIND, (keyBinding, client) -> handleIotaWheelItems(keyBinding, client));
        registerKeybind(CASTING_RING_KEY_BINDING, CastingRingHelperClient::handleCastingRingKeypress);
        registerKeybind(HEX_SCROLL_UP, (keyBinding, client) -> handleScrollKey(keyBinding, client, true));
        registerKeybind(HEX_SCROLL_DOWN, (keyBinding, client) -> handleScrollKey(keyBinding, client, false));
        registerKeybind(OPEN_HEX_BOOK, (keyBinding, client)->{}); // don't want to actually *do* anything, but do want to have it registered
    }

    // Registration itself happens in HexGloopForgeClient via NeoForge's RegisterKeyMappingsEvent:
    // this is called from FMLClientSetupEvent, which is after that event, and Architectury's
    // KeyMappingRegistry only appends late mappings to options.keyMappings with a warning.
    // Here we only hook up the per-tick handlers.
    public static void registerKeybind(KeyMapping keybinding, KeybindHandler handler){
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            handler.run(keybinding, minecraft);
        });
    }

    // not the best named anymore but it's just used here. technically catches fidgets too
    private static boolean isIotaWheelItem(Item item){
        return item instanceof ItemSpellbook || item instanceof ItemMultiFocus || item instanceof ItemFidget;
    }

    public static void handleIotaWheelItems(KeyMapping keyBinding, Minecraft client){
        handleIotaWheelItems(keyBinding, client, keyBinding.isDown());
    }

    public static void handleScrollKey(KeyMapping keyBinding, Minecraft client, boolean goUp){
        handleScrollKey(keyBinding, client, goUp, keyBinding.consumeClick());
    }

    public static void handleScrollKey(KeyMapping keyBinding, Minecraft client, boolean goUp, boolean isPressed){
        if(!isPressed) return;
        boolean mainHand = false;
        if(MixinIsScrollableInvoker.InvokeIsScrollableItem(client.player.getMainHandItem().getItem())){
            mainHand = true;
        } else if(!MixinIsScrollableInvoker.InvokeIsScrollableItem(client.player.getOffhandItem().getItem())){
            return;
        }
        int dir = goUp ? -1 : 1;
        IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                    new MsgShiftScrollC2S(mainHand ? dir : 0, !mainHand ? dir : 0, Screen.hasControlDown()));
    }

    // modified from default wnboi to support opening from casting context
    public static void handleIotaWheelItems(KeyMapping keyBinding, Minecraft client, boolean isPressed){
        if(!isPressed) return;
        // HexGloop.logPrint("thread check");
        Player player = client.player;
        if(player == null){
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        KeyboundItem mainItem = null;
        KeyboundItem offItem = null;

        if(mainHand.getItem() instanceof KeyboundItem && isIotaWheelItem(mainHand.getItem())){
            mainItem = (KeyboundItem)mainHand.getItem();
        }
        if(offHand.getItem() instanceof KeyboundItem && isIotaWheelItem(offHand.getItem())){
            offItem = (KeyboundItem)offHand.getItem();
        }

        if(client.screen != null && !(client.screen instanceof GuiSpellcasting)){
            // there's some other screen open, probably shouldn't open ours
            return;
        } else {
            // no screen open - check if we should open one
            if(mainItem != null && mainItem.getKeyBinding() == keyBinding){
                mainItem.openScreen();
                return;
            }
            if(offItem != null && offItem.getKeyBinding() == keyBinding){
                offItem.openScreen();
                return;
            }
        }
    }
}
