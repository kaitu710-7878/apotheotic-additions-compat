package dev.apotheotic_additions_compat.mixin;

import dev.apotheotic_additions_compat.logic.AffixDynamicExpander;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AffixRegistry.class, remap = false)
public class AffixRegistryMixin {

    @Inject(method = "onReload", at = @At("TAIL"))
    private void apotheoticAdditionsCompat$afterReload(CallbackInfo ci) {
        AffixDynamicExpander.expandAllAffixes();
    }
}
