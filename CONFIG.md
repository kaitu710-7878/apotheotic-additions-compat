# Configuration Guide / コンフィグ設定ガイド

File Location: `config/apotheotic_additions_compat-common.toml`

[English](#english) | [日本語](#japanese)

---

<a name="english"></a>
## English

### `[general]`
| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableCompatLogic` | Boolean | `true` | Master switch: Enables or disables the dynamic scaling and expansion of missing higher-tier affixes. |
| `enableOnDemandFallback` | Boolean | `true` | Enables on-demand calculation when an unexpanded affix is queried during gameplay. Prevents NPEs. |
| `logExpansion` | Boolean | `true` | If enabled, logs detailed expansion information to the console/log files when affixes are modified. |

### `[features]`
| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableAttributeExpansion` | Boolean | `true` | Enables dynamic scaling and expansion of missing higher-tier Attribute affixes (e.g. Iron's Spells mana/cooldown, attack damage, etc.). |
| `enablePotionExpansion` | Boolean | `true` | Enables dynamic scaling and expansion of missing higher-tier Potion/MobEffect affixes (scales duration, elevates amplifier for Esoteric, and reduces cooldown). |
| `enableVanillaPotionPool` | Boolean | `true` | Dynamically generates and registers safe combat Potion affixes using vanilla MobEffects (Poison, Wither, Speed, Strength, Resistance, etc.). |
| `enableModdedPotionPool` | Boolean | `false` | Dynamically generates and registers combat Potion affixes using modded MobEffects (e.g. Iron's Spells). Disabled by default to prevent unexpected overpowered combinations. |
| `allowCrossCategoryPotions` | Boolean | `true` | Allows combat potion effects (such as Caustic/Poison and Bloodletting/Bleed) to roll on swords, tridents, and heavy weapons interchangeably. |
| `enableSpecialAffixExpansion` | Boolean | `true` | Enables dynamic scaling and expansion of StepFunction-based special ability affixes (Thunderstruck, Festive, etc.) to higher rarities. |
| `fixApotheoticAdditionsBugs` | Boolean | `true` | Fixes known Apotheotic Additions bugs (e.g. prevents the block-break mining speed effect `haste_break` from erroneously generating on swords and tridents). |

### `[limits]`
| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `capGravityReduction` | Boolean | `true` | Whether to cap gravity reduction modifiers to avoid physics issues (e.g. unable to jump or floating mid-air). |
| `maxGravityReduction` | Double (0.0 - 1.0) | `0.50` | Maximum allowed gravity reduction ratio. Default `0.50` limits gravity reduction to at most 50% (-0.50 modifier). |

### `[multipliers]`
| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `heirloomMinMultiplier` | Double (0.0 - 1000.0) | `1.15` | Minimum value multiplier for Heirloom rarity relative to Mythic/base. |
| `heirloomMaxMultiplier` | Double (0.0 - 1000.0) | `1.25` | Maximum value multiplier for Heirloom rarity relative to Mythic/base. |
| `artifactMinMultiplier` | Double (0.0 - 1000.0) | `1.35` | Minimum value multiplier for Artifact rarity relative to Mythic/base. |
| `artifactMaxMultiplier` | Double (0.0 - 1000.0) | `1.60` | Maximum value multiplier for Artifact rarity relative to Mythic/base. |
| `esotericMinMultiplier` | Double (0.0 - 1000.0) | `2.00` | Minimum value multiplier for Esoteric rarity relative to Mythic/base. |
| `esotericMaxMultiplier` | Double (0.0 - 1000.0) | `2.50` | Maximum value multiplier for Esoteric rarity relative to Mythic/base. |
| `genericMinStepMultiplier` | Double (0.0 - 100.0) | `0.20` | Additional minimum multiplier per ordinal tier difference for unknown/modded higher rarities. |
| `genericMaxStepMultiplier` | Double (0.0 - 100.0) | `0.35` | Additional maximum multiplier per ordinal tier difference for unknown/modded higher rarities. |

### `[blacklists]`
| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `affixBlacklist` | List of Strings | `[]` | List of Affix IDs to ignore from automatic expansion (e.g. `["apotheosis:ironforged"]`). |
| `attributeBlacklist` | List of Strings | `[]` | List of Attribute IDs to ignore from automatic expansion (e.g. `["minecraft:generic.attack_speed"]`). |

---

<a name="japanese"></a>
## 日本語

### `[general]`
| キー | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `enableCompatLogic` | 真偽値 (Boolean) | `true` | マスタースイッチ: アフィックスの自動補完・スケーリング機能全体の有効/無効。 |
| `enableOnDemandFallback` | 真偽値 (Boolean) | `true` | ゲームプレイ中に未登録のレアリティが参照された際、オンデマンドで自動計算・注入する機能の有効/無効（NPE防止）。 |
| `logExpansion` | 真偽値 (Boolean) | `true` | アフィックスが上位レアリティ向けに自動拡張された際、ログに詳細を出力するかどうか。 |

### `[features]`
| キー | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `enableAttributeExpansion` | 真偽値 (Boolean) | `true` | Attributeアフィックス（ISSの最大マナやクールダウン短縮、攻撃力等）の上位レアリティ動的移植・スケーリングを有効化。 |
| `enablePotionExpansion` | 真偽値 (Boolean) | `true` | ポーション・モブ効果アフィックス（PotionAffix）の上位レアリティ動的移植・スケーリングを有効化（持続時間増加、EsotericでLv上昇、クールダウン短縮）。 |
| `enableVanillaPotionPool` | 真偽値 (Boolean) | `true` | バニラのMobEffect（毒、衰弱、移動速度、攻撃力上昇、耐性、再生等）を用いた安全で多彩なポーションアフィックスの動的生成・登録を有効化。 |
| `enableModdedPotionPool` | 真偽値 (Boolean) | `false` | 他MODのMobEffect（Iron's Spells等の追加効果など）を用いたポーションアフィックスの動的生成・登録を有効化（想定外の強力効果との組み合わせやバランス崩壊防止のためデフォルト無効）。 |
| `allowCrossCategoryPotions` | 真偽値 (Boolean) | `true` | 近接武器用ポーション効果（毒や出血など）を剣・重武器・三叉槍の間で相互に付与可能にする。 |
| `enableSpecialAffixExpansion` | 真偽値 (Boolean) | `true` | 特殊アビリティアフィックス（雷撃 Thunderstruck、花火・ドロップ Festive 等）の上位レアリティへの動的拡張を有効化。 |
| `fixApotheoticAdditionsBugs` | 真偽値 (Boolean) | `true` | Apotheotic Additionsの既知のバグ修正を有効化（ブロック採掘時の採掘速度上昇 `haste_break` が剣・トライデントに誤って付与される不具合を防止）。 |

### `[limits]`
| キー | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `capGravityReduction` | 真偽値 (Boolean) | `true` | 重力軽減効果の上限クランプを有効化するかどうか（ジャンプ不能や空中浮遊・静止バグを防止）。 |
| `maxGravityReduction` | 小数 (0.0 - 1.0) | `0.50` | 最大重力軽減率。デフォルトの `0.50` で最大50%軽減（-0.50）までに制限されます。 |

### `[multipliers]`
| キー | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `heirloomMinMultiplier` | 小数 (Double) | `1.15` | Heirloom レアリティの最小値倍率（Mythic等の基準に対する比率）。 |
| `heirloomMaxMultiplier` | 小数 (Double) | `1.25` | Heirloom レアリティの最大値倍率（Mythic等の基準に対する比率）。 |
| `artifactMinMultiplier` | 小数 (Double) | `1.35` | Artifact レアリティの最小値倍率（Mythic等の基準に対する比率）。 |
| `artifactMaxMultiplier` | 小数 (Double) | `1.60` | Artifact レアリティの最大値倍率（Mythic等の基準に対する比率）。 |
| `esotericMinMultiplier` | 小数 (Double) | `2.00` | Esoteric レアリティの最小値倍率（Mythic等の基準に対する比率）。 |
| `esotericMaxMultiplier` | 小数 (Double) | `2.50` | Esoteric レアリティの最大値倍率（Mythic等の基準に対する比率）。 |
| `genericMinStepMultiplier` | 小数 (Double) | `0.20` | 未知・カスタム上位レアリティにおける順位差1ランクあたりの最小値追加倍率。 |
| `genericMaxStepMultiplier` | 小数 (Double) | `0.35` | 未知・カスタム上位レアリティにおける順位差1ランクあたりの最大値追加倍率。 |

### `[blacklists]`
| キー | 型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `affixBlacklist` | 文字列リスト | `[]` | 自動拡張の対象から除外するアフィックスIDのリスト。 |
| `attributeBlacklist` | 文字列リスト | `[]` | 自動拡張の対象から除外する属性IDのリスト。 |
