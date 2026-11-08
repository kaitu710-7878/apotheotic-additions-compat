package dev.apotheotic_additions_compat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class ModConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue enableCompatLogic;
        public final ForgeConfigSpec.BooleanValue enableOnDemandFallback;
        public final ForgeConfigSpec.BooleanValue logExpansion;

        public final ForgeConfigSpec.BooleanValue enableAttributeExpansion;
        public final ForgeConfigSpec.BooleanValue enablePotionExpansion;
        public final ForgeConfigSpec.BooleanValue enableVanillaPotionPool;
        public final ForgeConfigSpec.BooleanValue enableModdedPotionPool;
        public final ForgeConfigSpec.BooleanValue allowCrossCategoryPotions;
        public final ForgeConfigSpec.BooleanValue enableSpecialAffixExpansion;
        public final ForgeConfigSpec.BooleanValue fixApotheoticAdditionsBugs;

        public final ForgeConfigSpec.BooleanValue capGravityReduction;
        public final ForgeConfigSpec.DoubleValue maxGravityReduction;

        public final ForgeConfigSpec.DoubleValue heirloomMinMultiplier;
        public final ForgeConfigSpec.DoubleValue heirloomMaxMultiplier;

        public final ForgeConfigSpec.DoubleValue artifactMinMultiplier;
        public final ForgeConfigSpec.DoubleValue artifactMaxMultiplier;

        public final ForgeConfigSpec.DoubleValue esotericMinMultiplier;
        public final ForgeConfigSpec.DoubleValue esotericMaxMultiplier;

        public final ForgeConfigSpec.DoubleValue genericMinStepMultiplier;
        public final ForgeConfigSpec.DoubleValue genericMaxStepMultiplier;

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> affixBlacklist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> attributeBlacklist;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Apotheotic Additions Compat Configuration")
                    .push("general");

            enableCompatLogic = builder
                    .comment("Master switch: Enable dynamic scaling and expansion of missing higher-tier affixes.",
                            "マスタースイッチ: 上位レアリティ（Artifact, Heirloom, Esotericなど）に対するアフィックスの自動補完・スケーリングを有効化するかどうか。")
                    .define("enableCompatLogic", true);

            enableOnDemandFallback = builder
                    .comment("Enable on-demand fallback when an unexpanded affix is queried during gameplay.",
                            "ゲームプレイ中に未拡張のアフィックスが参照された際、オンデマンドで自動計算・補完するかどうか。")
                    .define("enableOnDemandFallback", true);

            logExpansion = builder
                    .comment("Log details when affixes are automatically expanded for higher rarities.",
                            "上位レアリティ向けにアフィックスが自動補完された際にログを出力するかどうか。")
                    .define("logExpansion", true);

            builder.pop();

            builder.comment("Feature Toggles / 機能個別トグル設定")
                    .push("features");

            enableAttributeExpansion = builder
                    .comment("Enable dynamic scaling and expansion of missing higher-tier Attribute affixes (e.g. Iron's Spells mana/cooldown).",
                            "Attributeアフィックス（ISSの最大マナやクールダウン短縮など）の上位レアリティ動的移植・スケーリングを有効化するかどうか。")
                    .define("enableAttributeExpansion", true);

            enablePotionExpansion = builder
                    .comment("Enable dynamic scaling and expansion of missing higher-tier Potion/MobEffect affixes.",
                            "ポーション・モブ効果アフィックス（PotionAffix）の上位レアリティ動的移植・スケーリングを有効化するかどうか。")
                    .define("enablePotionExpansion", true);

            enableVanillaPotionPool = builder
                    .comment("Dynamically generate and register combat Potion affixes using vanilla MobEffects (Poison, Wither, Speed, Strength, Resistance, etc.).",
                            "バニラのMobEffect（毒、衰弱、移動速度、攻撃力上昇、耐性、再生など）を用いた安全で多彩なポーションアフィックスを動的生成・登録するかどうか。")
                    .define("enableVanillaPotionPool", true);

            enableModdedPotionPool = builder
                    .comment("Dynamically generate and register combat Potion affixes using modded MobEffects (e.g. Iron's Spells). Disabled by default to prevent unexpected overpowered combinations.",
                            "他MODのMobEffect（Iron's Spells等の追加効果など）を用いたポーションアフィックスを動的生成・登録するかどうか。予期せぬ強力効果との組み合わせやバランス崩壊を防ぐためデフォルトで無効です。")
                    .define("enableModdedPotionPool", false);

            allowCrossCategoryPotions = builder
                    .comment("Allow melee weapon potion affixes (e.g. Caustic/Poison, Bloodletting/Bleed) to be shared between swords, heavy weapons, and tridents.",
                            "近接武器用ポーション効果（毒や出血など）を剣・重武器・三叉槍の間で相互に付与可能にするかどうか。")
                    .define("allowCrossCategoryPotions", true);

            enableSpecialAffixExpansion = builder
                    .comment("Enable dynamic scaling and expansion of special ability affixes (Thunderstruck, Festive, etc.) to higher rarities (Artifact, Heirloom, Esoteric).",
                            "特殊アビリティアフィックス（雷撃 Thunderstruck、花火・ドロップ Festive 等）の上位レアリティへの動的拡張を有効化するかどうか。")
                    .define("enableSpecialAffixExpansion", true);

            fixApotheoticAdditionsBugs = builder
                    .comment("Fix known Apotheotic Additions bugs (such as mining-speed-on-block-break 'haste_break' incorrectly generating on swords).",
                            "Apotheotic Additionsの既知の不具合（ブロック破壊時ヘイスト付与が剣に付いてしまうバグ等）を修正するかどうか。")
                    .define("fixApotheoticAdditionsBugs", true);

            builder.pop();

            builder.comment("Gravity & Safety Limits", "重力制限および安全設定")
                    .push("limits");

            capGravityReduction = builder
                    .comment("Cap gravity reduction attribute modifiers so players do not freeze or become unable to jump.",
                            "重力軽減効果の上限をキャップし、ジャンプ不能や空中静止バグを防止するかどうか。")
                    .define("capGravityReduction", true);

            maxGravityReduction = builder
                    .comment("Maximum allowed gravity reduction ratio (e.g. 0.50 means at most 50% gravity reduction / -0.50 modifier).",
                            "許容される重力軽減率の最大上限（例: 0.50 の場合、最大でも50%軽減 = -0.50 に制限されます）。")
                    .defineInRange("maxGravityReduction", 0.50D, 0.0D, 1.0D);

            builder.pop();

            builder.comment("Scaling Multipliers for Specific Rarities",
                            "各レアリティ固有のスケーリング倍率（Mythicまたは基準最高レアリティに対する比率）")
                    .push("multipliers");

            builder.comment("Heirloom Rarity Multipliers (Focuses on strong effects with moderate attribute buffs)");
            heirloomMinMultiplier = builder
                    .comment("Minimum value multiplier for Heirloom rarity.",
                            "Heirloomレアリティの最小値倍率。")
                    .defineInRange("heirloomMinMultiplier", 1.15D, 0.0D, 1000.0D);
            heirloomMaxMultiplier = builder
                    .comment("Maximum value multiplier for Heirloom rarity.",
                            "Heirloomレアリティの最大値倍率。")
                    .defineInRange("heirloomMaxMultiplier", 1.25D, 0.0D, 1000.0D);

            builder.comment("Artifact Rarity Multipliers (Specialized in very high attribute values)");
            artifactMinMultiplier = builder
                    .comment("Minimum value multiplier for Artifact rarity.",
                            "Artifactレアリティの最小値倍率。")
                    .defineInRange("artifactMinMultiplier", 1.35D, 0.0D, 1000.0D);
            artifactMaxMultiplier = builder
                    .comment("Maximum value multiplier for Artifact rarity.",
                            "Artifactレアリティの最大値倍率。")
                    .defineInRange("artifactMaxMultiplier", 1.60D, 0.0D, 1000.0D);

            builder.comment("Esoteric Rarity Multipliers (Ultimate rarity, roughly 2x stats of Artifact/Heirloom)");
            esotericMinMultiplier = builder
                    .comment("Minimum value multiplier for Esoteric rarity.",
                            "Esotericレアリティの最小値倍率。")
                    .defineInRange("esotericMinMultiplier", 2.00D, 0.0D, 1000.0D);
            esotericMaxMultiplier = builder
                    .comment("Maximum value multiplier for Esoteric rarity.",
                            "Esotericレアリティの最大値倍率。")
                    .defineInRange("esotericMaxMultiplier", 2.50D, 0.0D, 1000.0D);

            builder.comment("Generic / Unknown Higher Rarities (Scaling per ordinal step difference)");
            genericMinStepMultiplier = builder
                    .comment("Additional minimum multiplier per ordinal rank difference above base rarity.",
                            "基準レアリティからの順序（ordinal）の差分1ランクあたりの最小値追加倍率。")
                    .defineInRange("genericMinStepMultiplier", 0.20D, 0.0D, 100.0D);
            genericMaxStepMultiplier = builder
                    .comment("Additional maximum multiplier per ordinal rank difference above base rarity.",
                            "基準レアリティからの順序（ordinal）の差分1ランクあたりの最大値追加倍率。")
                    .defineInRange("genericMaxStepMultiplier", 0.35D, 0.0D, 100.0D);

            builder.pop();

            builder.comment("Blacklists", "自動補完から除外するブラックリスト")
                    .push("blacklists");

            affixBlacklist = builder
                    .comment("List of Affix IDs to ignore from automatic expansion (e.g. \"apotheosis:ironforged\").",
                            "自動拡張の対象外とするアフィックスIDのリスト。")
                    .defineListAllowEmpty(Collections.singletonList("affixBlacklist"),
                            Collections::emptyList,
                            obj -> obj instanceof String);

            attributeBlacklist = builder
                    .comment("List of Attribute IDs to ignore from automatic expansion (e.g. \"minecraft:generic.attack_speed\").",
                            "自動拡張の対象外とする属性IDのリスト。")
                    .defineListAllowEmpty(Collections.singletonList("attributeBlacklist"),
                            Collections::emptyList,
                            obj -> obj instanceof String);

            builder.pop();
        }
    }
}
