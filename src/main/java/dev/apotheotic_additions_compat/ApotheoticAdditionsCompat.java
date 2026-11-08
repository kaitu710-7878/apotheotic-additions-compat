package dev.apotheotic_additions_compat;

import com.mojang.logging.LogUtils;
import dev.apotheotic_additions_compat.config.ModConfig;
import dev.apotheotic_additions_compat.logic.AffixDynamicExpander;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.placebo.reload.RegistryCallback;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ApotheoticAdditionsCompat.MODID)
public class ApotheoticAdditionsCompat {

    public static final String MODID = "apotheotic_additions_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ApotheoticAdditionsCompat() {
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        try {
            AffixRegistry.INSTANCE.addCallback(RegistryCallback.reloadOnly(reg -> {
                LOGGER.info("AffixRegistry reload callback triggered. Expanding affixes for higher rarities...");
                AffixDynamicExpander.expandAllAffixes();
            }));
        } catch (Throwable t) {
            LOGGER.warn("Failed to register reload callback to AffixRegistry directly: {}", t.getMessage());
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Apotheotic Additions Compat initialized.");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server started, ensuring all affixes are expanded for higher rarities.");
        AffixDynamicExpander.expandAllAffixes();
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        AffixDynamicExpander.expandAllAffixes();
    }
}
