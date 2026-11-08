package dev.apotheotic_additions_compat.mixin;

import dev.apotheotic_additions_compat.config.ModConfig;
import dev.apotheotic_additions_compat.logic.AffixDynamicExpander;
import dev.apotheotic_additions_compat.logic.IExtendedStepFunctionAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.ThunderstruckAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ThunderstruckAffix.class, remap = false)
public abstract class ThunderstruckAffixMixin extends Affix implements IExtendedStepFunctionAffix {

    @Shadow
    protected Map<LootRarity, StepFunction> values;

    @Unique
    private final Map<LootRarity, StepFunction> apotheoticAdditionsCompat$extraValues = new ConcurrentHashMap<>();

    public ThunderstruckAffixMixin(AffixType type) {
        super(type);
    }

    @Override
    public Map<LootRarity, StepFunction> apotheoticAdditionsCompat$getExtraValues() {
        return this.apotheoticAdditionsCompat$extraValues;
    }

    @Override
    public void apotheoticAdditionsCompat$addExtraRarity(LootRarity rarity, StepFunction func) {
        this.apotheoticAdditionsCompat$extraValues.put(rarity, func);
    }

    @Override
    public boolean apotheoticAdditionsCompat$hasRarity(LootRarity rarity) {
        return (this.values != null && this.values.containsKey(rarity)) || this.apotheoticAdditionsCompat$extraValues.containsKey(rarity);
    }

    @Override
    public StepFunction apotheoticAdditionsCompat$getStepFunction(LootRarity rarity) {
        if (this.values != null && this.values.containsKey(rarity)) {
            return this.values.get(rarity);
        }
        return this.apotheoticAdditionsCompat$extraValues.get(rarity);
    }

    @Inject(method = "canApplyTo", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$canApplyTo(ItemStack stack, LootCategory category, LootRarity rarity, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.COMMON.enableCompatLogic.get() || !ModConfig.COMMON.enableSpecialAffixExpansion.get()) {
            return;
        }

        if (category == null || !category.isLightWeapon()) {
            return;
        }

        if (this.values != null && this.values.containsKey(rarity)) {
            return;
        }

        StepFunction func = apotheoticAdditionsCompat$getStepFunction(rarity);
        if (func == null) {
            func = AffixDynamicExpander.computeStepFunction(this.values, rarity);
            if (func != null) {
                apotheoticAdditionsCompat$addExtraRarity(rarity, func);
            }
        }

        if (func != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTrueLevel", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$getTrueLevel(LootRarity rarity, float level, CallbackInfoReturnable<Float> cir) {
        if (this.values != null && this.values.containsKey(rarity)) {
            return;
        }

        StepFunction func = apotheoticAdditionsCompat$getStepFunction(rarity);
        if (func == null) {
            func = AffixDynamicExpander.computeStepFunction(this.values, rarity);
            if (func != null) {
                apotheoticAdditionsCompat$addExtraRarity(rarity, func);
            }
        }

        if (func != null) {
            cir.setReturnValue(func.get(level));
        }
    }
}
