package dev.apotheotic_additions_compat.mixin;

import dev.apotheotic_additions_compat.ApotheoticAdditionsCompat;
import dev.apotheotic_additions_compat.config.ModConfig;
import dev.apotheotic_additions_compat.logic.AffixDynamicExpander;
import dev.apotheotic_additions_compat.logic.IExtendedPotionAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = PotionAffix.class, remap = false)
public abstract class PotionAffixMixin extends Affix implements IExtendedPotionAffix {

    @Shadow
    protected MobEffect effect;

    @Shadow
    protected PotionAffix.Target target;

    @Shadow
    protected Map<LootRarity, PotionAffix.EffectData> values;

    @Shadow
    protected int cooldown;

    @Shadow
    protected Set<LootCategory> types;

    @Shadow
    protected boolean stackOnReapply;

    @Unique
    private final Map<LootRarity, PotionAffix.EffectData> apotheoticAdditionsCompat$extraValues = new ConcurrentHashMap<>();

    public PotionAffixMixin(AffixType type) {
        super(type);
    }

    @Override
    public Map<LootRarity, PotionAffix.EffectData> apotheoticAdditionsCompat$getExtraValues() {
        return this.apotheoticAdditionsCompat$extraValues;
    }

    @Override
    public void apotheoticAdditionsCompat$addExtraRarity(LootRarity rarity, PotionAffix.EffectData effectData) {
        this.apotheoticAdditionsCompat$extraValues.put(rarity, effectData);
    }

    @Override
    public boolean apotheoticAdditionsCompat$hasRarity(LootRarity rarity) {
        return (this.values != null && this.values.containsKey(rarity)) || this.apotheoticAdditionsCompat$extraValues.containsKey(rarity);
    }

    @Override
    public PotionAffix.EffectData apotheoticAdditionsCompat$getEffectData(LootRarity rarity) {
        if (this.values != null && this.values.containsKey(rarity)) {
            return this.values.get(rarity);
        }
        return this.apotheoticAdditionsCompat$extraValues.get(rarity);
    }

    /**
     * Intercepts canApplyTo to:
     * 1. Fix Apotheotic Additions bug where arrow effects or mining-effects generate on melee swords.
     * 2. Dynamically allow higher rarities when enablePotionExpansion is active.
     */
    @Inject(method = "canApplyTo", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$canApplyTo(ItemStack stack, LootCategory category, LootRarity rarity, CallbackInfoReturnable<Boolean> cir) {
        if (category == null || category.isNone()) {
            return;
        }

        // Bugfix & Safety for Apotheotic Additions: enforce strict target category compatibility
        if (ModConfig.COMMON.fixApotheoticAdditionsBugs.get() && this.target != null) {
            switch (this.target) {
                case ARROW_SELF, ARROW_TARGET -> {
                    // Arrow impact effects should only ever apply to ranged weapons (bow, crossbow)
                    if (!category.isRanged()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                case ATTACK_SELF, ATTACK_TARGET -> {
                    // Melee attack effects should only apply to weapons (not ranged, not armor, not shields, not breaker tools)
                    boolean isMelee = category.isLightWeapon() || category == LootCategory.SWORD || category == LootCategory.HEAVY_WEAPON || category == LootCategory.TRIDENT;
                    if (!isMelee || category.isRanged() || category.isArmor() || category == LootCategory.SHIELD || category.isBreaker()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                case HURT_SELF, HURT_ATTACKER -> {
                    // Defensive hurt effects should only apply to armor
                    if (!category.isArmor()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                case BLOCK_SELF, BLOCK_ATTACKER -> {
                    // Shield block effects should only apply to shields
                    if (category != LootCategory.SHIELD) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                case BREAK_SELF -> {
                    // Mining block-break effects should only apply to breaker tools (pickaxes, shovels, axes)
                    if (!category.isBreaker()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }

        if (!ModConfig.COMMON.enableCompatLogic.get() || !ModConfig.COMMON.enablePotionExpansion.get()) {
            return;
        }

        boolean matchesCategory = this.types == null || this.types.isEmpty() || this.types.contains(category);
        boolean crossCategoryMatched = false;

        if (!matchesCategory && ModConfig.COMMON.allowCrossCategoryPotions.get()) {
            // Melee weapons cross-category sharing: allow sword, trident, heavy_weapon to share ONLY melee combat potion affixes
            if (this.target == PotionAffix.Target.ATTACK_SELF || this.target == PotionAffix.Target.ATTACK_TARGET) {
                boolean isRequestedMelee = category.isLightWeapon() || category == LootCategory.SWORD || category == LootCategory.HEAVY_WEAPON || category == LootCategory.TRIDENT;
                boolean isConfiguredMelee = this.types != null && this.types.stream().anyMatch(t -> t.isLightWeapon() || t == LootCategory.SWORD || t == LootCategory.HEAVY_WEAPON || t == LootCategory.TRIDENT);
                if (isRequestedMelee && isConfiguredMelee) {
                    matchesCategory = true;
                    crossCategoryMatched = true;
                }
            }
        }

        if (!matchesCategory) {
            cir.setReturnValue(false);
            return;
        }

        if (this.values != null && this.values.containsKey(rarity)) {
            if (crossCategoryMatched) {
                // When matched via cross-category sharing, original canApplyTo would return false
                // because this.types does not contain category. We must explicitly return true!
                cir.setReturnValue(true);
            }
            return;
        }

        PotionAffix.EffectData data = this.apotheoticAdditionsCompat$getEffectData(rarity);
        if (data == null) {
            data = AffixDynamicExpander.getOrComputeEffectData((PotionAffix) (Object) this, rarity);
        }

        if (data != null) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    /**
     * Intercepts getCooldown to return scaled cooldown for higher rarities.
     */
    @Inject(method = "getCooldown", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$getCooldown(LootRarity rarity, CallbackInfoReturnable<Integer> cir) {
        if (this.values != null && this.values.containsKey(rarity)) {
            return;
        }

        PotionAffix.EffectData data = this.apotheoticAdditionsCompat$getEffectData(rarity);
        if (data == null) {
            data = AffixDynamicExpander.getOrComputeEffectData((PotionAffix) (Object) this, rarity);
        }

        if (data != null) {
            cir.setReturnValue(data.cooldown() != -1 ? data.cooldown() : this.cooldown);
        }
    }

    /**
     * Intercepts applyEffect to handle missing rarities safely using extraValues.
     */
    @Inject(method = "applyEffect", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$applyEffect(LivingEntity targetEntity, LootRarity rarity, float level, CallbackInfo ci) {
        if (targetEntity.level().isClientSide()) {
            return;
        }

        if (this.values != null && this.values.containsKey(rarity)) {
            return;
        }

        PotionAffix.EffectData data = this.apotheoticAdditionsCompat$getEffectData(rarity);
        if (data == null) {
            data = AffixDynamicExpander.getOrComputeEffectData((PotionAffix) (Object) this, rarity);
        }

        if (data != null) {
            int cd = data.cooldown() != -1 ? data.cooldown() : this.cooldown;
            if (cd != 0 && isOnCooldown(this.getId(), cd, targetEntity)) {
                ci.cancel();
                return;
            }

            MobEffectInstance existing = targetEntity.getEffect(this.effect);
            if (this.stackOnReapply && existing != null) {
                int duration = existing.getDuration() + data.duration().getInt(level);
                int amp = Math.min(existing.getAmplifier() + 1 + data.amplifier().getInt(level), 127);
                targetEntity.addEffect(new MobEffectInstance(this.effect, duration, amp, false, false, true));
            } else {
                targetEntity.addEffect(data.build(this.effect, level));
            }

            startCooldown(this.getId(), targetEntity);
            ci.cancel();
        } else {
            // Safe exit to prevent NPE
            ci.cancel();
        }
    }

    /**
     * Intercepts getAugmentingText to format dynamic and higher rarity effect descriptions.
     * Preserves original mod/Apotheosis texts for pre-existing rarities.
     */
    @Inject(method = "getAugmentingText", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$getAugmentingText(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<Component> cir) {
        if (!ModConfig.COMMON.enableCompatLogic.get()) {
            return;
        }

        ResourceLocation id = this.getId();
        boolean isDynamic = id != null && id.getNamespace().equals(ApotheoticAdditionsCompat.MODID);

        // For non-dynamic (original Apotheosis or external mods) affixes that already define this rarity,
        // do not touch it so original formatting is strictly preserved.
        if (!isDynamic && this.values != null && this.values.containsKey(rarity)) {
            return;
        }

        PotionAffix.EffectData data = null;
        if (this.values != null && this.values.containsKey(rarity)) {
            data = this.values.get(rarity);
        } else {
            data = this.apotheoticAdditionsCompat$getEffectData(rarity);
            if (data == null) {
                data = AffixDynamicExpander.getOrComputeEffectData((PotionAffix) (Object) this, rarity);
            }
        }

        if (data != null) {
            MobEffectInstance inst = data.build(this.effect, level);
            int ampLevel = inst.getAmplifier() + 1;
            float durSec = inst.getDuration() / 20.0f;
            String durStr = (durSec == (int) durSec) ? String.valueOf((int) durSec) : String.format(java.util.Locale.ROOT, "%.1f", durSec);
            Component effectName = Component.translatable(inst.getDescriptionId());
            cir.setReturnValue(Component.translatable("affix.apotheotic_additions_compat.augmenting", effectName, String.valueOf(ampLevel), durStr));
        }
    }

    /**
     * Intercepts getDescription:
     * 1. Preserves original Apotheosis / third-party mod formatting for standard defined rarities (NEVER overwrites them).
     * 2. Formats dynamic potion affixes with unambiguous target descriptions (gain vs inflict).
     * 3. Formats expanded higher rarities safely using Apotheosis standard target components.
     */
    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$getDescription(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<MutableComponent> cir) {
        if (!ModConfig.COMMON.enableCompatLogic.get()) {
            return;
        }

        ResourceLocation id = this.getId();
        boolean isDynamic = id != null && id.getNamespace().equals(ApotheoticAdditionsCompat.MODID);

        // Pre-existing rarities on non-dynamic affixes are completely untouched: let the original method run!
        if (!isDynamic && this.values != null && this.values.containsKey(rarity)) {
            return;
        }

        PotionAffix.EffectData data = null;
        if (this.values != null && this.values.containsKey(rarity)) {
            data = this.values.get(rarity);
        } else {
            data = this.apotheoticAdditionsCompat$getEffectData(rarity);
            if (data == null) {
                data = AffixDynamicExpander.getOrComputeEffectData((PotionAffix) (Object) this, rarity);
            }
        }

        if (data != null) {
            MobEffectInstance inst = data.build(this.effect, level);
            int cd = data.cooldown() != -1 ? data.cooldown() : this.cooldown;

            if (isDynamic) {
                // Use our dedicated clear description for dynamic affixes
                MutableComponent formatted = apotheoticAdditionsCompat$formatDescription(this.target, inst, cd, this.stackOnReapply);
                cir.setReturnValue(formatted);
            } else {
                // For expanded higher rarities on third-party affixes, format using Apotheosis's native Target.toComponent
                // to maintain 100% visual and lexical consistency with the mod's existing rarity tiers.
                MutableComponent comp = this.target.toComponent(new Object[]{ PotionAffix.toComponent(inst) });
                if (cd != 0) {
                    comp.append(" ").append(Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(cd)));
                }
                if (this.stackOnReapply) {
                    comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
                }
                cir.setReturnValue(comp);
            }
        }
    }

    @Unique
    private static MutableComponent apotheoticAdditionsCompat$formatDescription(PotionAffix.Target target, MobEffectInstance inst, int cd, boolean stackOnReapply) {
        String targetName = target != null ? target.name().toLowerCase(java.util.Locale.ROOT) : "attack_self";
        String langKey = "affix.apotheotic_additions_compat.target." + targetName;

        Component effectName = Component.translatable(inst.getDescriptionId());
        String ampStr = String.valueOf(inst.getAmplifier() + 1);

        float durSec = inst.getDuration() / 20.0f;
        String durStr = (durSec == (int) durSec) ? String.valueOf((int) durSec) : String.format(java.util.Locale.ROOT, "%.1f", durSec);

        MutableComponent comp = Component.translatable(langKey, effectName, ampStr, durStr);

        if (cd > 0) {
            float cdSec = cd / 20.0f;
            String cdStr = (cdSec == (int) cdSec) ? String.valueOf((int) cdSec) : String.format(java.util.Locale.ROOT, "%.1f", cdSec);
            comp.append(Component.translatable("affix.apotheotic_additions_compat.cooldown", cdStr));
        }

        if (stackOnReapply) {
            comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
        }

        return comp;
    }
}
