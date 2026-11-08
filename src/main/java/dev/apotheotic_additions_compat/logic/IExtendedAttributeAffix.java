package dev.apotheotic_additions_compat.logic;

import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;

import java.util.Map;

public interface IExtendedAttributeAffix {

    Map<LootRarity, StepFunction> apotheoticAdditionsCompat$getExtraValues();

    Map<LootRarity, AttributeAffix.ModifierInst> apotheoticAdditionsCompat$getExtraModifiers();

    void apotheoticAdditionsCompat$addExtraRarity(LootRarity rarity, StepFunction stepFunction, AttributeAffix.ModifierInst modifierInst);

    boolean apotheoticAdditionsCompat$hasRarity(LootRarity rarity);

    StepFunction apotheoticAdditionsCompat$getStepFunction(LootRarity rarity);

    AttributeAffix.ModifierInst apotheoticAdditionsCompat$getModifierInst(LootRarity rarity);
}
