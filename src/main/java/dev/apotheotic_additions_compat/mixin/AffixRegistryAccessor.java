package dev.apotheotic_additions_compat.mixin;

import com.google.common.collect.Multimap;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AffixRegistry.class, remap = false)
public interface AffixRegistryAccessor {

    @Accessor("byType")
    Multimap<AffixType, DynamicHolder<Affix>> apotheoticAdditionsCompat$getByType();

    @Accessor("byType")
    void apotheoticAdditionsCompat$setByType(Multimap<AffixType, DynamicHolder<Affix>> byType);
}
