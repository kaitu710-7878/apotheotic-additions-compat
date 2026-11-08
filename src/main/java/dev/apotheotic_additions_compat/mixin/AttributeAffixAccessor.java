package dev.apotheotic_additions_compat.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(value = AttributeAffix.class, remap = false)
public interface AttributeAffixAccessor {

    @Accessor("attribute")
    Attribute getAttribute();

    @Accessor("operation")
    AttributeModifier.Operation getOperation();

    @Accessor("values")
    Map<LootRarity, StepFunction> getValues();

    @Accessor("modifiers")
    Map<LootRarity, AttributeAffix.ModifierInst> getModifiers();

    @Accessor("types")
    Set<LootCategory> getTypes();
}
