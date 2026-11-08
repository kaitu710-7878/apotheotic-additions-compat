package dev.apotheotic_additions_compat.logic;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;

import java.util.Map;

public interface IExtendedStepFunctionAffix {

    Map<LootRarity, StepFunction> apotheoticAdditionsCompat$getExtraValues();

    void apotheoticAdditionsCompat$addExtraRarity(LootRarity rarity, StepFunction func);

    boolean apotheoticAdditionsCompat$hasRarity(LootRarity rarity);

    StepFunction apotheoticAdditionsCompat$getStepFunction(LootRarity rarity);
}
