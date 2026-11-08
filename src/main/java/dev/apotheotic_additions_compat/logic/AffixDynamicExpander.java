package dev.apotheotic_additions_compat.logic;

import dev.apotheotic_additions_compat.ApotheoticAdditionsCompat;
import dev.apotheotic_additions_compat.config.ModConfig;
import dev.apotheotic_additions_compat.mixin.AttributeAffixAccessor;
import dev.apotheotic_additions_compat.mixin.PotionAffixAccessor;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.*;

public class AffixDynamicExpander {

    private static boolean isExpanding = false;

    /**
     * Checks if the given attribute represents entity gravity (e.g. forge:entity_gravity).
     */
    public static boolean isGravityAttribute(Attribute attribute) {
        if (attribute == null) return false;
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (id == null) return false;
        String path = id.getPath().toLowerCase(Locale.ROOT);
        String str = id.toString().toLowerCase(Locale.ROOT);
        return str.equals("forge:entity_gravity") || str.equals("minecraft:generic.gravity") || path.contains("gravity");
    }

    private static boolean isBlacklisted(ResourceLocation affixId, Attribute attribute) {
        if (affixId != null) {
            String affixStr = affixId.toString();
            String affixPath = affixId.getPath();
            for (String blacklisted : ModConfig.COMMON.affixBlacklist.get()) {
                if (affixStr.equalsIgnoreCase(blacklisted) || affixPath.equalsIgnoreCase(blacklisted)
                        || blacklisted.equalsIgnoreCase("apotheosis:" + affixPath)) {
                    return true;
                }
            }
        }

        if (attribute != null) {
            ResourceLocation attrId = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            if (attrId != null) {
                String attrStr = attrId.toString();
                String attrPath = attrId.getPath();
                for (String blacklisted : ModConfig.COMMON.attributeBlacklist.get()) {
                    if (attrStr.equalsIgnoreCase(blacklisted) || attrPath.equalsIgnoreCase(blacklisted)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Scans all registered Affixes and expands AttributeAffixes and PotionAffixes that lack higher-tier rarities.
     */
    public static synchronized void expandAllAffixes() {
        if (!ModConfig.COMMON.enableCompatLogic.get()) {
            ApotheoticAdditionsCompat.LOGGER.info("Affix compatibility logic is disabled via master switch.");
            return;
        }

        if (isExpanding) {
            return;
        }
        isExpanding = true;

        try {
            // First, ensure dynamic potion affix pool is populated if enabled
            if (ModConfig.COMMON.enableVanillaPotionPool.get() || ModConfig.COMMON.enableModdedPotionPool.get()) {
                DynamicPotionAffixManager.registerDynamicPotionAffixes();
            }

            List<DynamicHolder<LootRarity>> orderedHolders = RarityRegistry.INSTANCE.getOrderedRarities();
            if (orderedHolders == null || orderedHolders.isEmpty()) {
                ApotheoticAdditionsCompat.LOGGER.warn("RarityRegistry ordered rarities are empty during expandAllAffixes.");
                return;
            }

            List<LootRarity> allRarities = new ArrayList<>();
            for (DynamicHolder<LootRarity> holder : orderedHolders) {
                if (holder.isBound()) {
                    allRarities.add(holder.get());
                }
            }

            int expandedAttributes = 0;
            int totalAttributes = 0;
            int expandedPotions = 0;
            int totalPotions = 0;
            int expandedSpecial = 0;
            int totalSpecial = 0;

            for (Affix affix : AffixRegistry.INSTANCE.getValues()) {
                if (affix instanceof AttributeAffix attrAffix) {
                    totalAttributes++;
                    if (ModConfig.COMMON.enableAttributeExpansion.get() && expandSingleAffix(attrAffix, allRarities)) {
                        expandedAttributes++;
                    }
                } else if (affix instanceof PotionAffix potionAffix) {
                    totalPotions++;
                    if (ModConfig.COMMON.enablePotionExpansion.get() && expandSinglePotionAffix(potionAffix, allRarities)) {
                        expandedPotions++;
                    }
                } else if (affix instanceof IExtendedStepFunctionAffix stepAffix) {
                    totalSpecial++;
                    if (ModConfig.COMMON.enableSpecialAffixExpansion.get() && expandSingleStepFunctionAffix(stepAffix, affix, allRarities)) {
                        expandedSpecial++;
                    }
                }
            }

            ApotheoticAdditionsCompat.LOGGER.info("Dynamic Affix Expansion complete: expanded {} / {} AttributeAffixes, {} / {} PotionAffixes, {} / {} SpecialAffixes for higher rarities.",
                    expandedAttributes, totalAttributes, expandedPotions, totalPotions, expandedSpecial, totalSpecial);

        } catch (Exception e) {
            ApotheoticAdditionsCompat.LOGGER.error("Error occurred while expanding affixes:", e);
        } finally {
            isExpanding = false;
        }
    }

    /**
     * Backward-compatible alias for expandAllAffixes.
     */
    public static void expandAllAttributeAffixes() {
        expandAllAffixes();
    }

    /**
     * Expands a single AttributeAffix with missing higher-tier rarities using IExtendedAttributeAffix.
     */
    public static boolean expandSingleAffix(AttributeAffix affix, List<LootRarity> allRarities) {
        AttributeAffixAccessor accessor = (AttributeAffixAccessor) affix;
        ResourceLocation affixId = AffixRegistry.INSTANCE.getKey(affix);
        Attribute attribute = accessor.getAttribute();

        if (isBlacklisted(affixId, attribute)) {
            return false;
        }

        Map<LootRarity, StepFunction> values = accessor.getValues();
        if (values == null || values.isEmpty()) {
            return false;
        }

        IExtendedAttributeAffix extendedAffix = (IExtendedAttributeAffix) affix;

        // Find the base rarity (the highest-ordinal rarity currently present in original values)
        LootRarity baseRarity = null;
        for (LootRarity r : values.keySet()) {
            if (baseRarity == null || r.ordinal() > baseRarity.ordinal()) {
                baseRarity = r;
            }
        }

        if (baseRarity == null) {
            return false;
        }

        StepFunction baseStepFunc = values.get(baseRarity);
        if (baseStepFunc == null) {
            return false;
        }

        // Determine candidate higher rarities that are missing in both original values and extended values
        List<LootRarity> missingHigherRarities = new ArrayList<>();
        for (LootRarity r : allRarities) {
            if (r.ordinal() > baseRarity.ordinal() && !values.containsKey(r) && !extendedAffix.apotheoticAdditionsCompat$hasRarity(r)) {
                missingHigherRarities.add(r);
            }
        }

        if (missingHigherRarities.isEmpty()) {
            return false;
        }

        boolean addedAny = false;
        List<String> addedRaritiesLog = new ArrayList<>();

        for (LootRarity higherRarity : missingHigherRarities) {
            ResourceLocation rarityKey = RarityRegistry.INSTANCE.getKey(higherRarity);
            StepFunction scaledStepFunc = calculateScaledStepFunction(baseStepFunc, baseRarity, higherRarity, rarityKey, attribute);
            AttributeAffix.ModifierInst modifierInst = new AttributeAffix.ModifierInst(accessor.getAttribute(), accessor.getOperation(), scaledStepFunc);

            extendedAffix.apotheoticAdditionsCompat$addExtraRarity(higherRarity, scaledStepFunc, modifierInst);
            addedAny = true;

            if (ModConfig.COMMON.logExpansion.get()) {
                String rName = rarityKey != null ? rarityKey.toString() : ("ordinal:" + higherRarity.ordinal());
                addedRaritiesLog.add(String.format("%s (min:%.2f, max:%.2f, step:%.2f, steps:%d)",
                        rName, scaledStepFunc.min(), scaledStepFunc.max(), scaledStepFunc.step(), scaledStepFunc.steps()));
            }
        }

        if (addedAny && ModConfig.COMMON.logExpansion.get() && !addedRaritiesLog.isEmpty()) {
            ApotheoticAdditionsCompat.LOGGER.debug("Expanded AttributeAffix [{}] based on base rarity [{}]: added [{}]",
                    affixId, RarityRegistry.INSTANCE.getKey(baseRarity), String.join(", ", addedRaritiesLog));
        }

        return addedAny;
    }

    /**
     * Expands a single PotionAffix with missing higher-tier rarities using IExtendedPotionAffix.
     */
    public static boolean expandSinglePotionAffix(PotionAffix affix, List<LootRarity> allRarities) {
        PotionAffixAccessor accessor = (PotionAffixAccessor) affix;
        ResourceLocation affixId = AffixRegistry.INSTANCE.getKey(affix);

        if (isBlacklisted(affixId, null)) {
            return false;
        }

        Map<LootRarity, PotionAffix.EffectData> values = accessor.getValues();
        if (values == null || values.isEmpty()) {
            return false;
        }

        IExtendedPotionAffix extendedAffix = (IExtendedPotionAffix) affix;

        LootRarity baseRarity = null;
        for (LootRarity r : values.keySet()) {
            if (baseRarity == null || r.ordinal() > baseRarity.ordinal()) {
                baseRarity = r;
            }
        }

        if (baseRarity == null) {
            return false;
        }

        PotionAffix.EffectData baseData = values.get(baseRarity);
        if (baseData == null) {
            return false;
        }

        List<LootRarity> missingHigherRarities = new ArrayList<>();
        for (LootRarity r : allRarities) {
            if (r.ordinal() > baseRarity.ordinal() && !values.containsKey(r) && !extendedAffix.apotheoticAdditionsCompat$hasRarity(r)) {
                missingHigherRarities.add(r);
            }
        }

        if (missingHigherRarities.isEmpty()) {
            return false;
        }

        boolean addedAny = false;
        List<String> addedRaritiesLog = new ArrayList<>();

        for (LootRarity higherRarity : missingHigherRarities) {
            ResourceLocation rarityKey = RarityRegistry.INSTANCE.getKey(higherRarity);
            PotionAffix.EffectData scaledData = calculateScaledEffectData(baseData, baseRarity, higherRarity, rarityKey, accessor.getEffect());

            extendedAffix.apotheoticAdditionsCompat$addExtraRarity(higherRarity, scaledData);
            addedAny = true;

            if (ModConfig.COMMON.logExpansion.get()) {
                String rName = rarityKey != null ? rarityKey.toString() : ("ordinal:" + higherRarity.ordinal());
                addedRaritiesLog.add(String.format("%s (dur:%.0f-%.0f ticks, amp:%.0f, cd:%d ticks)",
                        rName, scaledData.duration().min(), scaledData.duration().max(), scaledData.amplifier().max(), scaledData.cooldown()));
            }
        }

        if (addedAny && ModConfig.COMMON.logExpansion.get() && !addedRaritiesLog.isEmpty()) {
            ApotheoticAdditionsCompat.LOGGER.debug("Expanded PotionAffix [{}] based on base rarity [{}]: added [{}]",
                    affixId, RarityRegistry.INSTANCE.getKey(baseRarity), String.join(", ", addedRaritiesLog));
        }

        return addedAny;
    }

    /**
     * On-demand calculation and injection for a specific missing rarity in AttributeAffix.
     */
    public static synchronized AttributeAffix.ModifierInst getOrComputeModifierInst(AttributeAffix affix, LootRarity rarity) {
        if (!ModConfig.COMMON.enableCompatLogic.get() || !ModConfig.COMMON.enableOnDemandFallback.get() || !ModConfig.COMMON.enableAttributeExpansion.get()) {
            return null;
        }

        IExtendedAttributeAffix extendedAffix = (IExtendedAttributeAffix) affix;
        AttributeAffix.ModifierInst existing = extendedAffix.apotheoticAdditionsCompat$getModifierInst(rarity);
        if (existing != null) {
            return existing;
        }

        List<DynamicHolder<LootRarity>> orderedHolders = RarityRegistry.INSTANCE.getOrderedRarities();
        List<LootRarity> allRarities = new ArrayList<>();
        if (orderedHolders != null) {
            for (DynamicHolder<LootRarity> h : orderedHolders) {
                if (h.isBound()) allRarities.add(h.get());
            }
        }

        expandSingleAffix(affix, allRarities);

        return extendedAffix.apotheoticAdditionsCompat$getModifierInst(rarity);
    }

    /**
     * On-demand calculation and injection for a specific missing rarity in PotionAffix.
     */
    public static synchronized PotionAffix.EffectData getOrComputeEffectData(PotionAffix affix, LootRarity rarity) {
        if (!ModConfig.COMMON.enableCompatLogic.get() || !ModConfig.COMMON.enableOnDemandFallback.get() || !ModConfig.COMMON.enablePotionExpansion.get()) {
            return null;
        }

        IExtendedPotionAffix extendedAffix = (IExtendedPotionAffix) affix;
        PotionAffix.EffectData existing = extendedAffix.apotheoticAdditionsCompat$getEffectData(rarity);
        if (existing != null) {
            return existing;
        }

        List<DynamicHolder<LootRarity>> orderedHolders = RarityRegistry.INSTANCE.getOrderedRarities();
        List<LootRarity> allRarities = new ArrayList<>();
        if (orderedHolders != null) {
            for (DynamicHolder<LootRarity> h : orderedHolders) {
                if (h.isBound()) allRarities.add(h.get());
            }
        }

        expandSinglePotionAffix(affix, allRarities);

        return extendedAffix.apotheoticAdditionsCompat$getEffectData(rarity);
    }

    /**
     * Calculates an appropriately scaled EffectData for a higher rarity.
     */
    public static PotionAffix.EffectData calculateScaledEffectData(PotionAffix.EffectData base, LootRarity baseRarity, LootRarity targetRarity, ResourceLocation targetRarityKey, MobEffect effect) {
        String path = targetRarityKey != null ? targetRarityKey.getPath().toLowerCase(Locale.ROOT) : "";

        // Scale duration using standard formula
        StepFunction scaledDuration = calculateScaledStepFunction(base.duration(), baseRarity, targetRarity, targetRarityKey, null);

        // Scale amplifier: for higher tiers (especially Esoteric), grant +1 amplifier to elevate impact
        float baseAmpMax = base.amplifier().max();
        float newAmp = baseAmpMax;
        if (path.contains("esoteric")) {
            newAmp = baseAmpMax + 1.0f;
        } else if (path.contains("artifact") && baseAmpMax >= 1.0f) {
            newAmp = baseAmpMax + 1.0f;
        }
        StepFunction scaledAmplifier = new StepFunction(newAmp, 1, 0.0f);

        // Scale cooldown: higher tiers reduce cooldown (more frequent trigger)
        int baseCooldown = base.cooldown();
        int newCooldown = baseCooldown;
        if (baseCooldown > 0) {
            double cdMultiplier = 1.0D;
            if (path.contains("heirloom")) {
                cdMultiplier = 0.85D;
            } else if (path.contains("artifact")) {
                cdMultiplier = 0.75D;
            } else if (path.contains("esoteric")) {
                cdMultiplier = 0.50D;
            } else {
                int ordinalDiff = Math.max(1, targetRarity.ordinal() - baseRarity.ordinal());
                cdMultiplier = Math.max(0.40D, 1.0D - (ordinalDiff * 0.10D));
            }
            newCooldown = Math.max(20, (int) Math.round(baseCooldown * cdMultiplier));
        }

        return new PotionAffix.EffectData(scaledDuration, scaledAmplifier, newCooldown);
    }

    /**
     * Calculates an appropriately scaled StepFunction for a higher rarity based on a base StepFunction,
     * applying safety limits (e.g. gravity reduction cap).
     */
    public static StepFunction calculateScaledStepFunction(StepFunction base, LootRarity baseRarity, LootRarity targetRarity, ResourceLocation targetRarityKey, Attribute attribute) {
        double minMult;
        double maxMult;

        String path = targetRarityKey != null ? targetRarityKey.getPath().toLowerCase(Locale.ROOT) : "";

        if (path.contains("heirloom")) {
            minMult = ModConfig.COMMON.heirloomMinMultiplier.get();
            maxMult = ModConfig.COMMON.heirloomMaxMultiplier.get();
        } else if (path.contains("artifact")) {
            minMult = ModConfig.COMMON.artifactMinMultiplier.get();
            maxMult = ModConfig.COMMON.artifactMaxMultiplier.get();
        } else if (path.contains("esoteric")) {
            minMult = ModConfig.COMMON.esotericMinMultiplier.get();
            maxMult = ModConfig.COMMON.esotericMaxMultiplier.get();
        } else {
            int ordinalDiff = Math.max(1, targetRarity.ordinal() - baseRarity.ordinal());
            minMult = 1.0D + (ordinalDiff * ModConfig.COMMON.genericMinStepMultiplier.get());
            maxMult = 1.0D + (ordinalDiff * ModConfig.COMMON.genericMaxStepMultiplier.get());
        }

        float baseMin = base.min();
        float baseMax = base.max();
        float baseStep = base.step();
        int baseSteps = base.steps();

        // Target scaled bounds
        float targetMin = (float) (baseMin * minMult);
        float targetMax = (float) (baseMax * maxMult);

        // Safety limit: clamp gravity reduction if applicable (e.g. 50% max reduction = -0.50 cap)
        if (isGravityAttribute(attribute) && ModConfig.COMMON.capGravityReduction.get()) {
            float maxReduction = (float) Math.abs(ModConfig.COMMON.maxGravityReduction.get());
            float lowerLimit = -maxReduction;

            if (targetMin < lowerLimit) {
                targetMin = lowerLimit;
            }
            if (targetMax < lowerLimit) {
                targetMax = lowerLimit;
            }

            if (Math.abs(targetMin - targetMax) < 0.0001f) {
                return new StepFunction(targetMin, 1, 0.0f);
            }
        }

        // Handle single-value functions (steps <= 1 or step == 0)
        if (baseSteps <= 1 || Math.abs(baseStep) < 0.00001f) {
            float val = targetMax;
            return new StepFunction(val, 1, 0.0f);
        }

        float targetRange = targetMax - targetMin;
        boolean isIntegerStep = Math.abs(baseStep - Math.round(baseStep)) < 0.0001f;

        if (isIntegerStep && Math.abs(baseStep) >= 1.0f) {
            float avgMult = (float) ((minMult + maxMult) / 2.0);
            float newStep = Math.max(1.0f, Math.round(Math.abs(baseStep) * avgMult));
            if (baseStep < 0) {
                newStep = -newStep;
            }
            int newSteps = Math.max(1, Math.round(Math.abs(targetRange) / Math.abs(newStep)));
            targetMin = (float) Math.round(targetMin);
            targetMax = targetMin + newSteps * newStep;
            return new StepFunction(targetMin, newSteps, newStep);
        } else {
            int newSteps = Math.max(1, baseSteps);
            float newStep = targetRange / (float) newSteps;
            newStep = Math.round(newStep * 10000.0f) / 10000.0f;
            targetMin = Math.round(targetMin * 10000.0f) / 10000.0f;
            return new StepFunction(targetMin, newSteps, newStep);
        }
    }

    /**
     * Expands a StepFunction-based special affix (such as Thunderstruck or Festive) for higher rarities.
     */
    public static boolean expandSingleStepFunctionAffix(IExtendedStepFunctionAffix stepAffix, Affix affix, List<LootRarity> allRarities) {
        ResourceLocation affixId = AffixRegistry.INSTANCE.getKey(affix);
        if (isBlacklisted(affixId, null)) {
            return false;
        }

        boolean anyAdded = false;
        LootRarity highestRarity = null;

        for (LootRarity r : allRarities) {
            if (stepAffix.apotheoticAdditionsCompat$hasRarity(r)) {
                highestRarity = r;
            }
        }

        if (highestRarity == null) {
            return false;
        }

        int highestIndex = allRarities.indexOf(highestRarity);
        for (int i = highestIndex + 1; i < allRarities.size(); i++) {
            LootRarity targetRarity = allRarities.get(i);
            if (!stepAffix.apotheoticAdditionsCompat$hasRarity(targetRarity)) {
                StepFunction baseFunc = stepAffix.apotheoticAdditionsCompat$getStepFunction(highestRarity);
                if (baseFunc != null) {
                    ResourceLocation targetRarityKey = RarityRegistry.INSTANCE.getKey(targetRarity);
                    StepFunction scaledFunc = calculateScaledStepFunction(baseFunc, highestRarity, targetRarity, targetRarityKey, null);
                    stepAffix.apotheoticAdditionsCompat$addExtraRarity(targetRarity, scaledFunc);
                    anyAdded = true;
                    if (ModConfig.COMMON.logExpansion.get()) {
                        ApotheoticAdditionsCompat.LOGGER.info("[ApotheoticAdditionsCompat] Expanded SpecialAffix {} for rarity {}: [min={}, max={}, steps={}]",
                                affixId, targetRarityKey, scaledFunc.min(), scaledFunc.max(), scaledFunc.steps());
                    }
                }
            }
        }

        return anyAdded;
    }

    /**
     * Helper to compute a scaled StepFunction on demand for a given rarity.
     */
    public static StepFunction computeStepFunction(Map<LootRarity, StepFunction> values, LootRarity targetRarity) {
        if (values == null || values.isEmpty() || targetRarity == null) {
            return null;
        }

        if (values.containsKey(targetRarity)) {
            return values.get(targetRarity);
        }

        List<DynamicHolder<LootRarity>> orderedHolders = RarityRegistry.INSTANCE.getOrderedRarities();
        if (orderedHolders == null || orderedHolders.isEmpty()) {
            return null;
        }

        List<LootRarity> allRarities = new ArrayList<>();
        for (DynamicHolder<LootRarity> holder : orderedHolders) {
            if (holder.isBound()) {
                allRarities.add(holder.get());
            }
        }

        LootRarity highestRarity = null;
        for (LootRarity r : allRarities) {
            if (values.containsKey(r)) {
                highestRarity = r;
            }
        }

        if (highestRarity == null) {
            return null;
        }

        ResourceLocation targetRarityKey = RarityRegistry.INSTANCE.getKey(targetRarity);
        StepFunction baseFunc = values.get(highestRarity);
        return calculateScaledStepFunction(baseFunc, highestRarity, targetRarity, targetRarityKey, null);
    }
}
