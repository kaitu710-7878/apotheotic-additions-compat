package dev.apotheotic_additions_compat.logic;

import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;

import java.util.Map;

public interface IExtendedPotionAffix {

    Map<LootRarity, PotionAffix.EffectData> apotheoticAdditionsCompat$getExtraValues();

    void apotheoticAdditionsCompat$addExtraRarity(LootRarity rarity, PotionAffix.EffectData effectData);

    boolean apotheoticAdditionsCompat$hasRarity(LootRarity rarity);

    PotionAffix.EffectData apotheoticAdditionsCompat$getEffectData(LootRarity rarity);
}
