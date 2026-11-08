package com.samsthenerd.hexgloop.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;

public class ItemDyeableSpellbook extends ItemSpellbook {
    public ItemDyeableSpellbook(Properties properties) {
        super(properties);
    }

    // 1.21: dyeing goes through DataComponents.DYED_COLOR (item is in the minecraft:dyeable tag), no DyeableLeatherItem.
    // TODO(1.21 port): the creative-tab "Ancient Spellbook" (great_holder) stack used to be added in fillItemCategory.
    // Creative tab contents are now built in HexGloopItems' tab builder; add it there if wanted.

    public static final String GREAT_HOLDER_TAG = "great_holder";

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        // TODO(1.21 port): great-holder activation disabled. It used PatternRegistry.getPerWorldPatterns/lookupPattern
        // which no longer exist (Hex 0.12: per-world patterns are tagged hexcasting:per_world_pattern and resolved with
        // PatternRegistryManifest.getCanonicalStrokesPerWorld(key, level)). Original logic: for each great spell, write its
        // pattern iota to the next page, label it, name it, seal it, then rotate pages; once.
        return super.use(world, user, hand);
    }
}
