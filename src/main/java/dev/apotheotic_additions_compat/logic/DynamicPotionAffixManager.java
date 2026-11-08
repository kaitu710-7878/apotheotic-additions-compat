package dev.apotheotic_additions_compat.logic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.apotheotic_additions_compat.ApotheoticAdditionsCompat;
import dev.apotheotic_additions_compat.config.ModConfig;
import dev.apotheotic_additions_compat.mixin.AffixRegistryAccessor;
import dev.apotheotic_additions_compat.mixin.DynamicRegistryAccessor;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Dynamically creates and registers a clean, vanilla-accurate pool of PotionAffix instances
 * for weapons, armor, shields, and ranged weapons.
 */
public class DynamicPotionAffixManager {

    private static boolean registered = false;

    @SuppressWarnings("unchecked")
    public static synchronized void registerDynamicPotionAffixes() {
        if (!ModConfig.COMMON.enableCompatLogic.get()) {
            return;
        }

        boolean allowVanilla = ModConfig.COMMON.enableVanillaPotionPool.get();
        boolean allowModded = ModConfig.COMMON.enableModdedPotionPool.get();

        if (!allowVanilla && !allowModded) {
            return;
        }

        ApotheoticAdditionsCompat.LOGGER.info("[ApotheoticAdditionsCompat] Registering dynamic potion affix pool (Vanilla: {}, Modded: {})...", allowVanilla, allowModded);

        // Category sets
        Set<LootCategory> meleeCategories = Set.of(LootCategory.SWORD, LootCategory.HEAVY_WEAPON, LootCategory.TRIDENT);
        Set<LootCategory> rangedCategories = Set.of(LootCategory.BOW, LootCategory.CROSSBOW);
        Set<LootCategory> armorCategories = Set.of(LootCategory.CHESTPLATE, LootCategory.HELMET, LootCategory.LEGGINGS, LootCategory.BOOTS);
        Set<LootCategory> shieldCategories = Set.of(LootCategory.SHIELD);

        // Temporarily unlock Placebo's DynamicRegistry from ImmutableBiMap to mutable HashBiMap
        DynamicRegistryAccessor<Affix> registryAccessor = (DynamicRegistryAccessor<Affix>) AffixRegistry.INSTANCE;
        BiMap<ResourceLocation, Affix> currentRegistry = registryAccessor.apotheoticAdditionsCompat$getRegistry();
        BiMap<ResourceLocation, Affix> mutableRegistry = HashBiMap.create(currentRegistry);
        registryAccessor.apotheoticAdditionsCompat$setRegistry(mutableRegistry);

        List<PotionAffix> newlyRegistered = new ArrayList<>();

        try {
            if (allowVanilla) {
                // ==========================================
                // 1. VANILLA MELEE WEAPONS (ATTACK_SELF & ATTACK_TARGET)
                // ==========================================
                // Self Buffs on Hit: Vanilla brewable potion effects + requested Absorption
                registerEffectIfPresent(newlyRegistered, "self_strength", MobEffects.DAMAGE_BOOST, PotionAffix.Target.ATTACK_SELF, meleeCategories, 60, 20, 500, false);
                registerEffectIfPresent(newlyRegistered, "self_speed", MobEffects.MOVEMENT_SPEED, PotionAffix.Target.ATTACK_SELF, meleeCategories, 100, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "self_jump_boost", MobEffects.JUMP, PotionAffix.Target.ATTACK_SELF, meleeCategories, 120, 30, 400, false);
                registerEffectIfPresent(newlyRegistered, "self_absorption", MobEffects.ABSORPTION, PotionAffix.Target.ATTACK_SELF, meleeCategories, 120, 40, 600, false);
                registerEffectIfPresent(newlyRegistered, "self_regeneration", MobEffects.REGENERATION, PotionAffix.Target.ATTACK_SELF, meleeCategories, 60, 20, 600, false);
                registerEffectIfPresent(newlyRegistered, "self_fire_resistance", MobEffects.FIRE_RESISTANCE, PotionAffix.Target.ATTACK_SELF, meleeCategories, 200, 40, 400, false);
                registerEffectIfPresent(newlyRegistered, "self_water_breathing", MobEffects.WATER_BREATHING, PotionAffix.Target.ATTACK_SELF, meleeCategories, 300, 60, 400, false);
                registerEffectIfPresent(newlyRegistered, "self_night_vision", MobEffects.NIGHT_VISION, PotionAffix.Target.ATTACK_SELF, meleeCategories, 300, 60, 400, false);
                registerEffectIfPresent(newlyRegistered, "self_invisibility", MobEffects.INVISIBILITY, PotionAffix.Target.ATTACK_SELF, meleeCategories, 100, 30, 500, false);

                // Debuffs on Target on Hit: Vanilla brewable harmful effects + Wither
                registerEffectIfPresent(newlyRegistered, "target_poison", MobEffects.POISON, PotionAffix.Target.ATTACK_TARGET, meleeCategories, 80, 20, 500, false);
                registerEffectIfPresent(newlyRegistered, "target_slowness", MobEffects.MOVEMENT_SLOWDOWN, PotionAffix.Target.ATTACK_TARGET, meleeCategories, 100, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "target_weakness", MobEffects.WEAKNESS, PotionAffix.Target.ATTACK_TARGET, meleeCategories, 100, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "target_wither", MobEffects.WITHER, PotionAffix.Target.ATTACK_TARGET, meleeCategories, 60, 20, 600, false);

                // ==========================================
                // 2. VANILLA RANGED WEAPONS (ARROW_SELF & ARROW_TARGET)
                // ==========================================
                // Self Buffs on Arrow Impact: Specifically Strength, Speed, Jump Boost, Absorption
                registerEffectIfPresent(newlyRegistered, "arrow_self_strength", MobEffects.DAMAGE_BOOST, PotionAffix.Target.ARROW_SELF, rangedCategories, 60, 20, 500, false);
                registerEffectIfPresent(newlyRegistered, "arrow_self_speed", MobEffects.MOVEMENT_SPEED, PotionAffix.Target.ARROW_SELF, rangedCategories, 100, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "arrow_self_jump_boost", MobEffects.JUMP, PotionAffix.Target.ARROW_SELF, rangedCategories, 120, 30, 400, false);
                registerEffectIfPresent(newlyRegistered, "arrow_self_absorption", MobEffects.ABSORPTION, PotionAffix.Target.ARROW_SELF, rangedCategories, 120, 40, 600, false);

                // Debuffs on Target on Arrow Impact
                registerEffectIfPresent(newlyRegistered, "arrow_target_poison", MobEffects.POISON, PotionAffix.Target.ARROW_TARGET, rangedCategories, 80, 20, 500, false);
                registerEffectIfPresent(newlyRegistered, "arrow_target_slowness", MobEffects.MOVEMENT_SLOWDOWN, PotionAffix.Target.ARROW_TARGET, rangedCategories, 100, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "arrow_target_weakness", MobEffects.WEAKNESS, PotionAffix.Target.ARROW_TARGET, rangedCategories, 100, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "arrow_target_wither", MobEffects.WITHER, PotionAffix.Target.ARROW_TARGET, rangedCategories, 60, 20, 600, false);

                // ==========================================
                // 3. VANILLA ARMOR (HURT_SELF & HURT_ATTACKER)
                // ==========================================
                // Self Buffs when Hurt
                registerEffectIfPresent(newlyRegistered, "hurt_strength", MobEffects.DAMAGE_BOOST, PotionAffix.Target.HURT_SELF, armorCategories, 60, 20, 500, false);
                registerEffectIfPresent(newlyRegistered, "hurt_speed", MobEffects.MOVEMENT_SPEED, PotionAffix.Target.HURT_SELF, armorCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "hurt_jump_boost", MobEffects.JUMP, PotionAffix.Target.HURT_SELF, armorCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "hurt_absorption", MobEffects.ABSORPTION, PotionAffix.Target.HURT_SELF, armorCategories, 100, 30, 600, false);
                registerEffectIfPresent(newlyRegistered, "hurt_regeneration", MobEffects.REGENERATION, PotionAffix.Target.HURT_SELF, armorCategories, 60, 20, 600, false);
                registerEffectIfPresent(newlyRegistered, "hurt_fire_resistance", MobEffects.FIRE_RESISTANCE, PotionAffix.Target.HURT_SELF, armorCategories, 160, 40, 400, false);
                registerEffectIfPresent(newlyRegistered, "hurt_invisibility", MobEffects.INVISIBILITY, PotionAffix.Target.HURT_SELF, armorCategories, 80, 20, 500, false);

                // Debuffs on Attacker when Hurt
                registerEffectIfPresent(newlyRegistered, "attacker_slowness", MobEffects.MOVEMENT_SLOWDOWN, PotionAffix.Target.HURT_ATTACKER, armorCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "attacker_poison", MobEffects.POISON, PotionAffix.Target.HURT_ATTACKER, armorCategories, 60, 20, 500, false);
                registerEffectIfPresent(newlyRegistered, "attacker_weakness", MobEffects.WEAKNESS, PotionAffix.Target.HURT_ATTACKER, armorCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "attacker_wither", MobEffects.WITHER, PotionAffix.Target.HURT_ATTACKER, armorCategories, 50, 15, 600, false);

                // ==========================================
                // 4. VANILLA SHIELDS (BLOCK_SELF & BLOCK_ATTACKER)
                // ==========================================
                // Self Buffs on Block
                registerEffectIfPresent(newlyRegistered, "block_self_strength", MobEffects.DAMAGE_BOOST, PotionAffix.Target.BLOCK_SELF, shieldCategories, 60, 20, 500, false);
                registerEffectIfPresent(newlyRegistered, "block_self_speed", MobEffects.MOVEMENT_SPEED, PotionAffix.Target.BLOCK_SELF, shieldCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "block_self_jump_boost", MobEffects.JUMP, PotionAffix.Target.BLOCK_SELF, shieldCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "block_self_absorption", MobEffects.ABSORPTION, PotionAffix.Target.BLOCK_SELF, shieldCategories, 100, 30, 500, false);
                registerEffectIfPresent(newlyRegistered, "block_self_regeneration", MobEffects.REGENERATION, PotionAffix.Target.BLOCK_SELF, shieldCategories, 60, 20, 500, false);

                // Debuffs on Attacker on Block
                registerEffectIfPresent(newlyRegistered, "block_attacker_slowness", MobEffects.MOVEMENT_SLOWDOWN, PotionAffix.Target.BLOCK_ATTACKER, shieldCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "block_attacker_poison", MobEffects.POISON, PotionAffix.Target.BLOCK_ATTACKER, shieldCategories, 60, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "block_attacker_weakness", MobEffects.WEAKNESS, PotionAffix.Target.BLOCK_ATTACKER, shieldCategories, 80, 20, 400, false);
                registerEffectIfPresent(newlyRegistered, "block_attacker_wither", MobEffects.WITHER, PotionAffix.Target.BLOCK_ATTACKER, shieldCategories, 50, 15, 500, false);
            }

            if (allowModded) {
                // ==========================================
                // 5. MODDED MELEE & RANGED WEAPONS (ATTACK_SELF, ARROW_SELF, ATTACK_TARGET, ARROW_TARGET)
                // ==========================================
                registerModdedEffectIfPresent(newlyRegistered, "self_instant_mana", "irons_spellbooks:instant_mana", PotionAffix.Target.ATTACK_SELF, meleeCategories, 20, 1, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "self_evasion", "irons_spellbooks:evasion", PotionAffix.Target.ATTACK_SELF, meleeCategories, 60, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "self_fortify", "irons_spellbooks:fortify", PotionAffix.Target.ATTACK_SELF, meleeCategories, 80, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "self_oakskin", "irons_spellbooks:oakskin", PotionAffix.Target.ATTACK_SELF, meleeCategories, 80, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "self_hastened", "irons_spellbooks:hastened", PotionAffix.Target.ATTACK_SELF, meleeCategories, 60, 20, 500, false);
                registerModdedEffectIfPresent(newlyRegistered, "self_echoing_strikes", "irons_spellbooks:echoing_strikes", PotionAffix.Target.ATTACK_SELF, meleeCategories, 60, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "self_charged", "irons_spellbooks:charged", PotionAffix.Target.ATTACK_SELF, meleeCategories, 80, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "self_true_invisibility", "irons_spellbooks:true_invisibility", PotionAffix.Target.ATTACK_SELF, meleeCategories, 100, 30, 600, false);

                registerModdedEffectIfPresent(newlyRegistered, "arrow_self_instant_mana", "irons_spellbooks:instant_mana", PotionAffix.Target.ARROW_SELF, rangedCategories, 20, 1, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "arrow_self_evasion", "irons_spellbooks:evasion", PotionAffix.Target.ARROW_SELF, rangedCategories, 60, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "arrow_self_hastened", "irons_spellbooks:hastened", PotionAffix.Target.ARROW_SELF, rangedCategories, 60, 20, 500, false);

                registerModdedEffectIfPresent(newlyRegistered, "target_rend", "irons_spellbooks:rend", PotionAffix.Target.ATTACK_TARGET, meleeCategories, 80, 20, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "target_chilled", "irons_spellbooks:chilled", PotionAffix.Target.ATTACK_TARGET, meleeCategories, 100, 20, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "target_shocked", "irons_spellbooks:shocked", PotionAffix.Target.ATTACK_TARGET, meleeCategories, 60, 20, 500, false);
                registerModdedEffectIfPresent(newlyRegistered, "target_blight", "irons_spellbooks:blight", PotionAffix.Target.ATTACK_TARGET, meleeCategories, 80, 20, 600, false);

                registerModdedEffectIfPresent(newlyRegistered, "arrow_target_rend", "irons_spellbooks:rend", PotionAffix.Target.ARROW_TARGET, rangedCategories, 80, 20, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "arrow_target_chilled", "irons_spellbooks:chilled", PotionAffix.Target.ARROW_TARGET, rangedCategories, 100, 20, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "arrow_target_shocked", "irons_spellbooks:shocked", PotionAffix.Target.ARROW_TARGET, rangedCategories, 60, 20, 500, false);
                registerModdedEffectIfPresent(newlyRegistered, "arrow_target_blight", "irons_spellbooks:blight", PotionAffix.Target.ARROW_TARGET, rangedCategories, 80, 20, 600, false);

                // ==========================================
                // 6. MODDED ARMOR & SHIELDS (HURT_SELF, BLOCK_SELF)
                // ==========================================
                registerModdedEffectIfPresent(newlyRegistered, "hurt_instant_mana", "irons_spellbooks:instant_mana", PotionAffix.Target.HURT_SELF, armorCategories, 20, 1, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "hurt_evasion", "irons_spellbooks:evasion", PotionAffix.Target.HURT_SELF, armorCategories, 60, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "hurt_fortify", "irons_spellbooks:fortify", PotionAffix.Target.HURT_SELF, armorCategories, 80, 20, 600, false);
                registerModdedEffectIfPresent(newlyRegistered, "hurt_oakskin", "irons_spellbooks:oakskin", PotionAffix.Target.HURT_SELF, armorCategories, 80, 20, 600, false);

                registerModdedEffectIfPresent(newlyRegistered, "block_self_instant_mana", "irons_spellbooks:instant_mana", PotionAffix.Target.BLOCK_SELF, shieldCategories, 20, 1, 400, false);
                registerModdedEffectIfPresent(newlyRegistered, "block_self_evasion", "irons_spellbooks:evasion", PotionAffix.Target.BLOCK_SELF, shieldCategories, 60, 20, 500, false);
                registerModdedEffectIfPresent(newlyRegistered, "block_self_fortify", "irons_spellbooks:fortify", PotionAffix.Target.BLOCK_SELF, shieldCategories, 80, 20, 500, false);
                registerModdedEffectIfPresent(newlyRegistered, "block_self_oakskin", "irons_spellbooks:oakskin", PotionAffix.Target.BLOCK_SELF, shieldCategories, 80, 20, 500, false);
            }

            // Also register newly registered affixes into byType Multimap so AffixHelper.byType and LootController find them!
            if (!newlyRegistered.isEmpty()) {
                AffixRegistryAccessor accessor = (AffixRegistryAccessor) AffixRegistry.INSTANCE;
                Multimap<AffixType, DynamicHolder<Affix>> currentByType = accessor.apotheoticAdditionsCompat$getByType();
                ImmutableMultimap.Builder<AffixType, DynamicHolder<Affix>> builder = ImmutableMultimap.builder();
                if (currentByType != null) {
                    builder.putAll(currentByType);
                }
                for (PotionAffix affix : newlyRegistered) {
                    builder.put(affix.getType(), AffixRegistry.INSTANCE.holder(affix));
                }
                accessor.apotheoticAdditionsCompat$setByType(builder.build());
            }

            registered = true;
            ApotheoticAdditionsCompat.LOGGER.info("[ApotheoticAdditionsCompat] Successfully populated dynamic potion affix pool ({} newly added).", newlyRegistered.size());

        } finally {
            // Safely re-freeze registry into ImmutableBiMap
            registryAccessor.apotheoticAdditionsCompat$setRegistry(ImmutableBiMap.copyOf(mutableRegistry));
        }
    }

    private static void registerModdedEffectIfPresent(List<PotionAffix> newlyRegistered, String affixName, String effectId, PotionAffix.Target target, Set<LootCategory> types, int baseDuration, int durationStep, int baseCooldown, boolean stackOnReapply) {
        ResourceLocation id = new ResourceLocation(effectId);
        if (ForgeRegistries.MOB_EFFECTS.containsKey(id)) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
            if (effect != null) {
                registerEffect(newlyRegistered, affixName, effect, target, types, baseDuration, durationStep, baseCooldown, stackOnReapply);
            }
        }
    }

    private static void registerEffectIfPresent(List<PotionAffix> newlyRegistered, String affixName, MobEffect effect, PotionAffix.Target target, Set<LootCategory> types, int baseDuration, int durationStep, int baseCooldown, boolean stackOnReapply) {
        if (effect != null) {
            registerEffect(newlyRegistered, affixName, effect, target, types, baseDuration, durationStep, baseCooldown, stackOnReapply);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerEffect(List<PotionAffix> newlyRegistered, String affixName, MobEffect effect, PotionAffix.Target target, Set<LootCategory> types, int baseDuration, int durationStep, int baseCooldown, boolean stackOnReapply) {
        ResourceLocation affixId = new ResourceLocation(ApotheoticAdditionsCompat.MODID, "dynamic/" + affixName);

        // Check if already registered
        if (AffixRegistry.INSTANCE.getKeys().contains(affixId)) {
            return;
        }

        Map<LootRarity, PotionAffix.EffectData> values = new HashMap<>();

        // Helper to safely fetch LootRarity from RarityRegistry
        for (LootRarity rarity : RarityRegistry.INSTANCE.getValues()) {
            ResourceLocation rarityId = RarityRegistry.INSTANCE.getKey(rarity);
            if (rarityId == null) continue;

            String path = rarityId.getPath().toLowerCase(Locale.ROOT);
            int amp = 0;
            int dur = baseDuration;
            int cd = baseCooldown;

            switch (path) {
                case "common":
                    dur = baseDuration;
                    amp = 0;
                    cd = baseCooldown;
                    break;
                case "uncommon":
                    dur = baseDuration + durationStep;
                    amp = 0;
                    cd = Math.max(100, baseCooldown - 50);
                    break;
                case "rare":
                    dur = baseDuration + durationStep * 2;
                    amp = 0;
                    cd = Math.max(100, baseCooldown - 100);
                    break;
                case "epic":
                    dur = baseDuration + durationStep * 3;
                    amp = 1;
                    cd = Math.max(100, baseCooldown - 150);
                    break;
                case "mythic":
                    dur = baseDuration + durationStep * 5;
                    amp = 1;
                    cd = Math.max(100, baseCooldown - 200);
                    break;
                case "ancient":
                    dur = baseDuration + durationStep * 7;
                    amp = 1;
                    cd = Math.max(100, baseCooldown - 220);
                    break;
                case "artifact":
                    dur = (int) (baseDuration * 1.5) + durationStep * 8;
                    amp = 2;
                    cd = Math.max(100, baseCooldown - 250);
                    break;
                case "heirloom":
                    dur = (int) (baseDuration * 1.8) + durationStep * 10;
                    amp = 2;
                    cd = Math.max(80, baseCooldown - 280);
                    break;
                case "esoteric":
                    dur = (int) (baseDuration * 2.5) + durationStep * 15;
                    amp = 3;
                    cd = Math.max(60, baseCooldown - 320);
                    break;
                default:
                    // Scaled for any custom higher rarity
                    dur = baseDuration + durationStep * 6;
                    amp = 1;
                    cd = Math.max(100, baseCooldown - 150);
                    break;
            }

            StepFunction durFunc = new StepFunction(dur, 4, durationStep / 2f);
            StepFunction ampFunc = StepFunction.constant(amp);
            values.put(rarity, new PotionAffix.EffectData(durFunc, ampFunc, cd));
        }

        PotionAffix affix = new PotionAffix(effect, target, values, baseCooldown, types, stackOnReapply);

        // Inject into DynamicRegistry
        try {
            ((DynamicRegistryAccessor<Affix>) AffixRegistry.INSTANCE).apotheoticAdditionsCompat$invokeRegister(affixId, affix);
            newlyRegistered.add(affix);
        } catch (Exception e) {
            ApotheoticAdditionsCompat.LOGGER.warn("[ApotheoticAdditionsCompat] Failed to register dynamic potion affix: {}", affixId, e);
        }
    }
}
