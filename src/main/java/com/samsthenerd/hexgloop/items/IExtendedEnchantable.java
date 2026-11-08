package com.samsthenerd.hexgloop.items;

import net.minecraft.world.item.Item;

// TODO(1.21 port): EnchantmentCategory is gone (enchantments are data driven, supported items come from item tags
// like minecraft:enchantable/weapon and minecraft:enchantable/mining). This now uses a local enum; the mixin that
// used to hook EnchantmentCategory has to be replaced with item tags in data.
public interface IExtendedEnchantable {
    public static enum Category { WEAPON, DIGGER, OTHER }

    boolean canAcceptEnchantment(Category target, Item item);

    // some default implementations for convenience, don't use these if you need multiple, just implement the generic interface
    public static interface IWeaponEnchantable extends IExtendedEnchantable{
        default boolean canAcceptEnchantment(Category target, Item item){
            return target == Category.WEAPON;
        }
    }

    public static interface IToolEnchantable extends IExtendedEnchantable{
        default boolean canAcceptEnchantment(Category target, Item item){
            return target == Category.DIGGER;
        }
    }
}
