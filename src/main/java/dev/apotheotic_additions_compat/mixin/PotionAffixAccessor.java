package dev.apotheotic_additions_compat.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(value = PotionAffix.class, remap = false)
public interface PotionAffixAccessor {

    @Accessor("effect")
    MobEffect getEffect();

    @Accessor("target")
    PotionAffix.Target getTarget();

    @Accessor("values")
    Map<LootRarity, PotionAffix.EffectData> getValues();

    @Accessor("cooldown")
    int getCooldown();

    @Accessor("types")
    Set<LootCategory> getTypes();

    @Accessor("stackOnReapply")
    boolean getStackOnReapply();
}
