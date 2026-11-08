package com.samsthenerd.hexgloop.items;

import com.samsthenerd.hexgloop.HexGloop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;

// come back to this
public abstract class ItemHexTool extends ItemPackagedHex implements IExtendedEnchantable {
    public static final ResourceLocation TOOL_STATUS_PREDICATE = ResourceLocation.fromNamespaceAndPath(HexGloop.MOD_ID, "hex_tool_status");
    private final float attackDamage;
    protected final float attackSpeed;

    // note that that's base stats, so like,, when broken
    public ItemHexTool(Properties pProperties, float attackDamage, float attackSpeed) {
        super(pProperties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    public boolean hasMediaToUse(ItemStack stack){
        return getMedia(stack) > 0;
    }

    @Override
    public boolean canDrawMediaFromInventory(ItemStack stack){
        return false; // i feel like no,, but the design is artifact-ish,, so idk
    }

    // 1.21: Item.canBeDepleted() no longer exists; kept as a plain helper for subclasses/callers
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean breakAfterDepletion(){
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 25;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    // 1.21: getDefaultAttributeModifiers(EquipmentSlot) returning a Multimap is gone;
    // modifiers are a data component built with ItemAttributeModifiers.Builder
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)this.attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)this.attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    }

    // for hexxycraft backport
    @Override
    public int cooldown(){
        return 0;
    }
}
