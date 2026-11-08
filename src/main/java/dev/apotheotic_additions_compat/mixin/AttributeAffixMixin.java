package dev.apotheotic_additions_compat.mixin;

import dev.apotheotic_additions_compat.config.ModConfig;
import dev.apotheotic_additions_compat.logic.AffixDynamicExpander;
import dev.apotheotic_additions_compat.logic.IExtendedAttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import java.util.function.BiConsumer;

@Mixin(value = AttributeAffix.class, remap = false)
public abstract class AttributeAffixMixin extends Affix implements IExtendedAttributeAffix {

    @Shadow
    protected Attribute attribute;

    @Shadow
    protected AttributeModifier.Operation operation;

    @Shadow
    protected Map<LootRarity, StepFunction> values;

    @Shadow
    protected Set<LootCategory> types;

    @Shadow
    protected transient Map<LootRarity, AttributeAffix.ModifierInst> modifiers;

    @Unique
    private final Map<LootRarity, StepFunction> apotheoticAdditionsCompat$extraValues = new ConcurrentHashMap<>();

    @Unique
    private final Map<LootRarity, AttributeAffix.ModifierInst> apotheoticAdditionsCompat$extraModifiers = new ConcurrentHashMap<>();

    public AttributeAffixMixin(AffixType type) {
        super(type);
    }

    @Override
    public Map<LootRarity, StepFunction> apotheoticAdditionsCompat$getExtraValues() {
        return this.apotheoticAdditionsCompat$extraValues;
    }

    @Override
    public Map<LootRarity, AttributeAffix.ModifierInst> apotheoticAdditionsCompat$getExtraModifiers() {
        return this.apotheoticAdditionsCompat$extraModifiers;
    }

    @Override
    public void apotheoticAdditionsCompat$addExtraRarity(LootRarity rarity, StepFunction stepFunction, AttributeAffix.ModifierInst modifierInst) {
        this.apotheoticAdditionsCompat$extraValues.put(rarity, stepFunction);
        this.apotheoticAdditionsCompat$extraModifiers.put(rarity, modifierInst);
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

    @Override
    public AttributeAffix.ModifierInst apotheoticAdditionsCompat$getModifierInst(LootRarity rarity) {
        if (this.modifiers != null && this.modifiers.containsKey(rarity)) {
            return this.modifiers.get(rarity);
        }
        return this.apotheoticAdditionsCompat$extraModifiers.get(rarity);
    }

    /**
     * Intercepts canApplyTo to ensure missing higher-tier rarities are recognized as valid.
     */
    @Inject(method = "canApplyTo", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$canApplyTo(ItemStack stack, LootCategory category, LootRarity rarity, CallbackInfoReturnable<Boolean> cir) {
        if (category == null || category.isNone()) {
            return;
        }

        boolean matchesCategory = this.types == null || this.types.isEmpty() || this.types.contains(category);
        if (!matchesCategory) {
            return;
        }

        if (this.modifiers != null && this.modifiers.containsKey(rarity)) {
            return;
        }

        AttributeAffix.ModifierInst inst = this.apotheoticAdditionsCompat$getModifierInst(rarity);
        if (inst == null) {
            inst = AffixDynamicExpander.getOrComputeModifierInst((AttributeAffix) (Object) this, rarity);
        }

        if (inst != null) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Ensures modifier is applied safely with gravity clamping (e.g. max 50% reduction).
     */
    @Inject(method = "addModifiers", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$addModifiers(ItemStack stack, LootRarity rarity, float level, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> map, CallbackInfo ci) {
        BiConsumer<Attribute, AttributeModifier> safeConsumer = apotheoticAdditionsCompat$wrapConsumer(map);

        if (this.modifiers != null && this.modifiers.containsKey(rarity)) {
            // If present in original modifiers, still enforce gravity reduction cap
            if (AffixDynamicExpander.isGravityAttribute(this.attribute) && ModConfig.COMMON.capGravityReduction.get()) {
                AttributeAffix.ModifierInst inst = this.modifiers.get(rarity);
                if (inst != null && inst.attr() != null) {
                    LootCategory category = LootCategory.forItem(stack);
                    if (category != null) {
                        for (EquipmentSlot categorySlot : category.getSlots()) {
                            if (categorySlot == slot) {
                                safeConsumer.accept(inst.attr(), inst.build(stack, this.getId(), level));
                            }
                        }
                    }
                }
                ci.cancel();
            }
            return;
        }

        AttributeAffix.ModifierInst inst = this.apotheoticAdditionsCompat$getModifierInst(rarity);
        if (inst == null) {
            inst = AffixDynamicExpander.getOrComputeModifierInst((AttributeAffix) (Object) this, rarity);
        }

        if (inst != null) {
            if (inst.attr() != null) {
                LootCategory category = LootCategory.forItem(stack);
                if (category != null) {
                    for (EquipmentSlot categorySlot : category.getSlots()) {
                        if (categorySlot == slot) {
                            safeConsumer.accept(inst.attr(), inst.build(stack, this.getId(), level));
                        }
                    }
                }
            }
            ci.cancel();
        } else {
            // Cannot provide modifier, safely cancel to avoid crashing
            ci.cancel();
        }
    }

    @Unique
    private BiConsumer<Attribute, AttributeModifier> apotheoticAdditionsCompat$wrapConsumer(BiConsumer<Attribute, AttributeModifier> original) {
        if (!ModConfig.COMMON.capGravityReduction.get()) {
            return original;
        }
        double maxReduction = Math.abs(ModConfig.COMMON.maxGravityReduction.get());
        double limit = -maxReduction;

        return (attr, modif) -> {
            if (AffixDynamicExpander.isGravityAttribute(attr) && modif != null && modif.getAmount() < limit) {
                AttributeModifier clamped = new AttributeModifier(modif.getId(), modif.getName(), limit, modif.getOperation());
                original.accept(attr, clamped);
            } else {
                original.accept(attr, modif);
            }
        };
    }

    /**
     * Builds augmenting text dynamically for higher rarities.
     */
    @Inject(method = "getAugmentingText", at = @At("HEAD"), cancellable = true)
    private void apotheoticAdditionsCompat$getAugmentingText(ItemStack stack, LootRarity rarity, float level, CallbackInfoReturnable<Component> cir) {
        if (this.modifiers != null && this.modifiers.containsKey(rarity) && !AffixDynamicExpander.isGravityAttribute(this.attribute)) {
            return;
        }

        AttributeAffix.ModifierInst inst = this.apotheoticAdditionsCompat$getModifierInst(rarity);
        if (inst == null) {
            inst = AffixDynamicExpander.getOrComputeModifierInst((AttributeAffix) (Object) this, rarity);
        }

        if (inst != null && inst.attr() != null) {
            cir.setReturnValue(apotheoticAdditionsCompat$formatAugmentingText(inst, level));
        } else if (this.modifiers != null && this.modifiers.containsKey(rarity)) {
            // fallback to original if couldn't construct
            return;
        } else {
            cir.setReturnValue(Component.empty());
        }
    }

    @Unique
    private Component apotheoticAdditionsCompat$formatAugmentingText(AttributeAffix.ModifierInst inst, float level) {
        double value = inst.valueFactory().get(level);
        if (AffixDynamicExpander.isGravityAttribute(this.attribute) && ModConfig.COMMON.capGravityReduction.get()) {
            double limit = -Math.abs(ModConfig.COMMON.maxGravityReduction.get());
            if (value < limit) {
                value = limit;
            }
        }

        MutableComponent valueComp = IFormattableAttribute.toValueComponent(
                this.attribute,
                this.operation,
                value < 0 ? -value : value,
                AttributesLib.getTooltipFlag()
        );

        MutableComponent comp;
        if (value > 0) {
            comp = Component.translatable("attributeslib.modifier.plus", valueComp, Component.translatable(this.attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE);
        } else {
            comp = Component.translatable("attributeslib.modifier.take", valueComp, Component.translatable(this.attribute.getDescriptionId())).withStyle(ChatFormatting.RED);
        }

        float min = inst.valueFactory().min();
        float max = inst.valueFactory().max();
        if (AffixDynamicExpander.isGravityAttribute(this.attribute) && ModConfig.COMMON.capGravityReduction.get()) {
            float limit = -(float) Math.abs(ModConfig.COMMON.maxGravityReduction.get());
            if (min < limit) min = limit;
            if (max < limit) max = limit;
        }

        if (min != max) {
            MutableComponent minComp = IFormattableAttribute.toValueComponent(this.attribute, this.operation, min < 0 ? -min : min, AttributesLib.getTooltipFlag());
            MutableComponent maxComp = IFormattableAttribute.toValueComponent(this.attribute, this.operation, max < 0 ? -max : max, AttributesLib.getTooltipFlag());
            MutableComponent rangeComp = Component.translatable("affix.apotheosis.range", minComp, maxComp);
            comp.append(" ").append(rangeComp);
        }

        return comp;
    }
}
