package dev.apotheotic_additions_compat.mixin;

import com.google.common.collect.BiMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(value = DynamicRegistry.class, remap = false)
public interface DynamicRegistryAccessor<R extends CodecProvider<? super R>> {

    @Accessor("registry")
    BiMap<ResourceLocation, R> apotheoticAdditionsCompat$getRegistry();

    @Accessor("registry")
    void apotheoticAdditionsCompat$setRegistry(BiMap<ResourceLocation, R> registry);

    @Accessor("holders")
    Map<ResourceLocation, DynamicHolder<? extends R>> apotheoticAdditionsCompat$getHolders();

    @Invoker("register")
    void apotheoticAdditionsCompat$invokeRegister(ResourceLocation id, R item);
}
