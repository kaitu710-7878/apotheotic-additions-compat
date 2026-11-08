# Changelog / 変更履歴

[English](#english) | [日本語](#japanese)

---

<a name="english"></a>
## English

### [1.3.4] - 2026-11-08
#### Fixed & Resolved
- **Fixed Dynamic Potion Affix Registration Failure**:
  - Resolved `UnsupportedOperationException: ImmutableMap.put` by unlocking Placebo's `DynamicRegistry` before injection and cleanly re-freezing it into `ImmutableBiMap` afterward. All dynamic potion affixes now reliably register into the registry and byType multimap.
- **Fixed Potion Affixes Not Appearing on Swords (Mythic & Esoteric)**:
  - Fixed cross-category sharing logic where `canApplyTo` was returning false because the original method checked strict types when values already contained the rarity.
  - Reinforced category checking so melee weapons (swords, tridents, heavy weapons) properly match all offensive/self potion affixes at any rarity tier.

### [1.3.3] - 2026-11-08
#### Fixed & Refactored
- **Fixed Arrow Effects Incorrectly Generating on Swords**:
  - Fixed a bug where Apotheotic Additions ranged potion affixes (`planar.json`, `sunbird.json`, etc.) mistakenly included `trident` in their types, causing melee cross-sharing to apply arrow hit effects (`ARROW_SELF` / `ARROW_TARGET`) to swords.
  - Implemented strict target-to-category compatibility validation in `PotionAffixMixin.canApplyTo`: arrow effects are strictly restricted to ranged weapons (`category.isRanged()`), attack effects strictly to melee weapons, hurt effects to armor, block effects to shields, and break effects to tools.
- **Preserved Original Mod Tooltip Formatting**:
  - Resolved the issue where existing Apotheosis and third-party mod potion affixes were overwritten with a generic format.
  - Pre-existing rarities on non-dynamic affixes now strictly execute their original `getDescription()` to preserve their exact author formatting.
  - Expanded higher rarities safely format using native `Target.toComponent` to maintain lexical harmony with original mod lines.
- **Unambiguous Target Phrasing (Self vs Target/Attacker)**:
  - Clarified dynamic potion affix tooltips to explicitly indicate recipients: "On hit, grant self...", "On hit, inflict on target...", "When attacked, grant self...", "When attacked, inflict on attacker...".
  - Japanese: "攻撃ヒット時、自身に...", "攻撃ヒット時、対象に...", "被ダメージ時、自身に...", "被ダメージ時、攻撃者に...".
- **Vanilla-Standard Item Affix Names**:
  - Re-aligned dynamic affix item prefixes and suffixes with official Minecraft naming conventions (e.g., "Strength" / "of Strength", "攻撃力上昇の" / "（攻撃力上昇）", "移動速度上昇の" / "（移動速度上昇）", "衝撃吸収の" / "（衝撃吸収）").
- **Code Optimization & Refactoring**:
  - Refactored category checks and tooltip builder branches for enhanced performance and maintainability.

### [1.3.2] - 2026-11-08
#### Changed & Formatted
- **Standardized Potion Affix Tooltip Format**: Replaced ad-hoc phrasing with Apotheosis-standardized explicit format across all languages:
  - English: `On hit, gain [Effect] Level [N] for [X]s (Cooldown: [Y]s)`
  - Japanese: `ヒット時 [効果名] をレベル [N] で [X]秒 （クールダウン: [Y]秒）`
- **Replaced Raw Potion Names on Items**: Item affix prefixes/suffixes now use elegant, lore-friendly affix descriptors (e.g. "Brawny" / "剛腕の", "Swift" / "神速の", "Revitalizing" / "湧活の", "Shielding" / "防護の") instead of literal potion name prefixes ("Strength's" / "再生能力の"), matching vanilla Apotheosis naming quality.

### [1.3.1] - 2026-11-08
#### Changed & Refined
- **Strictly Vanilla Brewable Buffs Only**: Replaced miscellaneous buffs with strictly vanilla brewable potion effects (Strength, Speed, Jump Boost, Regeneration, Fire Resistance, Water Breathing, Night Vision, Invisibility) plus Absorption.
- **Removed Slow Falling**: Completely excluded Slow Falling (`SLOW_FALLING`) per user request to respect player movement preferences.
- **Ranged Self-Buffs Focused**: Focused ranged self-buffs (`ARROW_SELF`) strictly on Strength, Speed, Jump Boost, and Absorption.
- **Vanilla-Standard Naming**: Standardized all potion affix names to match Minecraft vanilla names directly (e.g. "Strength" / "of Strength", "移動速度上昇の" / "（移動速度上昇）", "衝撃吸収の" / "（衝撃吸収）") for clear, intuitive item names.

### [1.3.0] - 2026-11-08
#### Added & Expanded
- **Comprehensive Self-Buffs & Retaliation Effects Across All Gear Types**:
  - Expanded dynamic potion affixes to support both **self buffs** and **target/attacker debuffs** across melee weapons, ranged weapons, armor, and shields:
    - **Melee Weapons**: Added on-hit self buffs for Invisibility, Night Vision, Water Breathing, Slow Falling, and Saturation, alongside existing buffs (Strength, Speed, Regeneration, Resistance, Absorption, Jump Boost, Haste, Fire Resistance).
    - **Ranged Weapons**: Added `ARROW_SELF` (Speed, Resistance, Invisibility, Regeneration, Absorption) and `ARROW_TARGET` (Poison, Wither, Slowness, Blindness, Glowing, Levitation, Darkness).
    - **Armor**: Added `HURT_SELF` (Resistance, Regeneration, Absorption, Speed, Fire Resistance, Invisibility, Strength, Saturation) and `HURT_ATTACKER` (Slowness, Poison, Wither, Blindness).
    - **Shields**: Added `BLOCK_SELF` (Resistance, Regeneration, Absorption, Strength, Speed) and `BLOCK_ATTACKER` (Slowness, Poison, Wither, Blindness).
  - **Modded Self-Buff Support**: Fully integrated Iron's Spells buff effects (`instant_mana`, `evasion`, `fortify`, `oakskin`, `hastened`, `echoing_strikes`, `charged`, `true_invisibility`, `angel_wings`, `abyssal_shroud`) for both weapons and defensive gear (disabled by default under `enableModdedPotionPool`).

### [1.2.1] - 2026-11-08
#### Changed & Configured
- **Split Dynamic Potion Pools**: Separated the dynamic potion pool generation into **Vanilla** (`enableVanillaPotionPool`, Default: `true`) and **Modded** (`enableModdedPotionPool`, Default: `false`).
- Modded potion affix dynamic generation is disabled by default to prevent unexpected game-breaking interactions with powerful modded status effects, while keeping safe, well-balanced vanilla potion affixes available out of the box.

### [1.2.0] - 2026-11-08
#### Fixed & Added
- **Potion Tooltip Internal Key Bugfix**: Fixed an issue where higher-rarity potion affixes displayed raw unlocalized internal description keys (`affix.%s.desc`). Replaced with vanilla Apotheosis localization templates (`target.toComponent(...)` + `affix.apotheosis.cooldown`), displaying clean bilingual names, amplifiers, durations, and cooldowns.
- **Dynamic Potion Affix Pool Generator**: Added automatic generation and registration of over 20+ rich combat PotionAffixes using vanilla and modded MobEffects:
  - Offensive on-target debuffs: Poison, Wither, Slowness, Blindness, Glowing, Levitation, Darkness, Hunger, and Iron's Spells effects (Rend, Chilled, Shocked, Blight).
  - Offensive on-hit self buffs: Strength, Speed, Regeneration, Resistance, Absorption, Jump Boost, Haste, Fire Resistance, and Instant Mana.
  - Defensive self buffs & retaliation debuffs for armor and shields.
- **Cross-Category Melee Potion Sharing**: Melee weapons (swords, tridents, heavy weapons) can now share combat potion effects (e.g. Caustic/Poison, Bloodletting/Bleed).
- **Special Ability Affix Expansion**: Dynamically expands StepFunction-based special affixes (`ThunderstruckAffix` lightning chain, `FestiveAffix` firework celebration/bonus drops) to higher tiers (Artifact, Heirloom, Esoteric).
- **New Configuration Toggles**: Added `enableDynamicPotionExpansion`, `allowCrossCategoryPotions`, and `enableSpecialAffixExpansion` under `[features]`.

### [1.1.0] - 2026-11-08
#### Added & Changed
- **Potion / MobEffect Affix Dynamic Expansion**: Extended dynamic scaling to `PotionAffix`. Missing higher rarities (Artifact, Heirloom, Esoteric) now receive scaled duration, elevated amplifier (potency increase for Esoteric), and reduced cooldown.
- **Apotheotic Additions Bugfix**: Automatically prevents `haste_break` (a block-break mining speed effect) from erroneously rolling on swords and tridents.
- **Granular Feature Toggles**: Added dedicated configuration toggles under `[features]` (`enableAttributeExpansion`, `enablePotionExpansion`, `fixApotheoticAdditionsBugs`) allowing standalone usage or custom configurations.
- Resolves ability affix pool depletion where sword Esoteric affixes were permanently fixed to the same 5 affixes.

### [1.0.3] - 2026-11-08
#### Added & Changed
- Replaced complete gravity exclusion with an intelligent **50% maximum gravity reduction cap** (`maxGravityReduction = 0.50`, configurable).
- Low gravity affixes (`forge:entity_gravity`, `apotheosis:armor/attribute/gravitational`) can now roll on higher rarities safely without causing physics bugs such as inability to jump or floating mid-air.
- Dual-layer protection: clamps both `StepFunction` generation and `AttributeModifier` application.

### [1.0.2] - 2026-11-08
#### Added
- Added low-gravity effects to default blacklists.

### [1.0.1] - 2026-11-07
#### Fixed
- Fixed `java.lang.IllegalAccessError` caused by attempting to set `final` fields via Mixin accessors.
- Re-architected expansion system using clean interface injection (`IExtendedAttributeAffix`) backed by concurrent maps.
- Renamed root package namespace to `dev.apotheotic_additions_compat`.

### [1.0.0] - 2026-11-07
#### Added
- Initial release of **Apotheotic Additions Compat**.

---

<a name="japanese"></a>
## 日本語

### [1.3.4] - 2026-11-08
#### 修正・不具合解消
- **動的ポーションアフィックス登録失敗の根本解決**:
  - Placeboの `DynamicRegistry` が初期化後に `ImmutableBiMap` に固定されていたため `UnsupportedOperationException` が発生し、生成された全ポーションアフィックスが登録失敗していた重大な問題を解消。登録時に安全に可変マップへ差し替えて登録後再フリーズする設計へ改修し、全てのポーションアフィックスが確実にゲーム内へ登録されるよう修正。
- **剣にポーション効果が付与されない（Mythic / Esoteric）不具合の解消**:
  - `allowCrossCategoryPotions` による近接武器相互共有判定において、該当レアリティが存在する場合に元の `canApplyTo` に委ねてしまい、元のカテゴリチェックで弾かれていた問題を修正。
  - 近接武器（剣、重武器、三叉槍）の適合性判定を明示化し、MythicやEsotericを含む全レアリティで剣に正常にポーションアフィックスが付与されるよう改善。

### [1.3.3] - 2026-11-08
#### 修正・最適化・リファクタリング
- **剣に矢のポーション効果が付与される不具合の根本修正**:
  - Apotheotic Additionsの遠距離アフィックス（`planar.json` や `sunbird.json` 等）の `types` に誤って `trident` が含まれていた問題、および近接武器間の相互付与判定を修正。
  - `PotionAffixMixin.canApplyTo` に厳格なターゲット・カテゴリ適合性チェックを導入：
    - `ARROW_SELF` / `ARROW_TARGET`（矢命中時効果）は **遠距離武器（弓・クロスボウ）のみ** に厳密制限し、剣などの近接武器への付与を完全遮断。
    - `ATTACK_SELF` / `ATTACK_TARGET`（近接攻撃時効果）は武器のみに限定。
    - `HURT_SELF` / `HURT_ATTACKER` は防具のみ、`BLOCK_SELF` / `BLOCK_ATTACKER` は盾のみ、`BREAK_SELF` は採掘ツールのみに制限。
- **既存MOD・バニラApotheosisの元表示の上書きを防止**:
  - Apotheosis本体や他アドオンに元から存在するポーションアフィックスの説明文まで一律で書き換えてしまっていた問題を修正。
  - 元からデータが存在するレアリティでは元のMODの `getDescription()` をそのまま実行させ、元の表示を1文字も崩さず100%保持。未定義上位レアリティ拡張時もApotheosisネイティブの `Target.toComponent` を呼び出して自然な統一感を維持。
- **自身付与か相手付与かの明確化**:
  - 動的ポーションアフィックスの説明文において、「自身に」「対象に」「攻撃者に」を明記：
    - 攻撃時自身バフ: `攻撃ヒット時、自身に [効果名] (Lv.[N]) を [X]秒間付与`
    - 攻撃時相手デバフ: `攻撃ヒット時、対象に [効果名] (Lv.[N]) を [X]秒間付与`
    - 被弾時自身バフ: `被ダメージ時、自身に [効果名] (Lv.[N]) を [X]秒間付与`
    - 被弾時相手デバフ: `被ダメージ時、攻撃者に [効果名] (Lv.[N]) を [X]秒間付与`
    - 矢命中・盾防御・ブロック破壊時も同様に付与対象を明確化し、誤認を完全に防止。
- **アフィックス名をバニラ名称に再統一**:
  - ユーザー指示に準拠し、動的ポーションアフィックスのアイテム接頭辞・接尾辞をバニラMinecraftの正式名称（「攻撃力上昇の」「（攻撃力上昇）」「移動速度上昇の」「（移動速度上昇）」「跳躍力上昇の」「（跳躍力上昇）」「衝撃吸収の」「（衝撃吸収）」等）に再統一。
- **全体的なコード最適化・リファクタリング**:
  - `PotionAffixMixin` のカテゴリ整合性判定ロジックとツールチップ分岐を整理・高速化。

### [1.3.2] - 2026-11-08
#### 変更・書式統一
- **ポーションアフィックスのツールチップ書式を統一**: 他のApotheosisポーションバフの標準書式に寄せ、完全統一されたわかりやすい説明文フォーマットを導入：
  - 日本語: `ヒット時 [効果名] をレベル [N] で [X]秒 （クールダウン: [Y]秒）`（攻撃、被弾、矢着弾、ブロック時それぞれ完全対応）
  - 英語: `On hit, gain [Effect] Level [N] for [X]s (Cooldown: [Y]s)`
- **アイテム名（アフィックス名）の洗練**: アイテムに「再生能力の ダイヤの剣」のようなポーション効果名そのままの直球名称が付かないよう、他のApotheosisポーションアフィックスに倣った格調高い接頭辞・接尾辞（剛腕の、神速の、跳躍の、防護の、湧活の、耐熱の、潜水の、夜目の、幽影の等）へ一新。

### [1.3.1] - 2026-11-08
#### 変更・調整
- **バニラ製作可能ポーション効果のみに厳選**: バフ効果を醸造台で製作可能なバニラポーション（攻撃力上昇、移動速度上昇、跳躍力上昇、再生能力、火炎耐性、水中呼吸、暗視、透明化）および指定の衝撃吸収のみに絞り込み。
- **低速落下の完全除外**: プレイスタイルの好みが分かれる「低速落下（`SLOW_FALLING`）」をプールから完全に除外。
- **射撃系自身バフの整理**: 弓・クロスボウの射撃時自身バフ（`ARROW_SELF`）を「攻撃力上昇」「移動速度上昇」「跳躍力上昇」「衝撃吸収」に特化。
- **アフィックス名をバニラ名称に統一**: アフィックスのプレフィックスおよびサフィックスを、装飾的な語句からバニラMinecraftの正式名称（「攻撃力上昇の」「（攻撃力上昇）」「移動速度上昇の」「（移動速度上昇）」「衝撃吸収の」「（衝撃吸収）」等）へ統一。直感的で親しみやすい表記に改善。

### [1.3.0] - 2026-11-08
#### 追加・対応範囲拡大
- **武器・防具・盾・遠距離の全装備における「自分へのバフ」および「相手へのデバフ」の全面拡充**:
  - 動的ポーションアフィックスの対象を大幅に拡張し、各装備種別で攻守両面（自身バフ / 相手デバフ）のアフィックスがロールできるよう改善：
    - **近接武器**: 攻撃時自身バフ（剛力、神速、治癒、耐性、衝撃吸収、跳躍、加速、耐火、透明化、暗視、水中呼吸、低速落下、満腹回復）＆相手デバフ（猛毒、衰弱、鈍足、盲目、発光、浮遊、暗闇、空腹）。
    - **遠距離武器（弓・クロスボウ）**: 射撃時自身バフ（`ARROW_SELF`: 神速、耐性、隠密、治癒、衝撃吸収）＆着弾時相手デバフ（`ARROW_TARGET`: 毒、衰弱、鈍足、盲目、発光、浮遊、暗闇）。
    - **防具**: 被弾時自身バフ（`HURT_SELF`: 耐性、治癒、衝撃吸収、神速、耐火、透明化、逆境剛力、満腹回復）＆反撃デバフ（`HURT_ATTACKER`: 鈍足、毒、衰弱、盲目）。
    - **盾**: ガード時自身バフ（`BLOCK_SELF`: 金剛耐性、癒護再生、防塁吸収、反撃剛力、遊撃機動）＆ガード時相手デバフ（`BLOCK_ATTACKER`: 衝撃鈍足、毒針、呪詛衰弱、閃光盲目）。
  - **他MODの自身バフ対応**: Iron's Spells等の追加バフ効果（マナ奔流、回避、強化、樹皮硬化、急速詠唱、追撃、帯電、真の不可視、天使の翼、深淵被膜など）を武器・防具・盾に完全実装（バランス保護のためデフォルトはOFFのまま維持）。

### [1.2.1] - 2026-11-08
#### 変更・設定調整
- **動的ポーションプールの分離とデフォルト設定の適正化**: 動的ポーション生成機能を「バニラポーション（`enableVanillaPotionPool`、デフォルト: `true`）」と「MODポーション（`enableModdedPotionPool`、デフォルト: `false`）」に細分化。
- 想定外の強力な他MOD効果と組み合わさってバランス崩壊や不具合を起こすリスクを防止するため、MODポーションの自動生成をデフォルトで無効化。安全で効果が把握しやすいバニラポーション（毒、衰弱、鈍足、盲目、剛力、神速、耐性、再生、跳躍など）はデフォルト有効のまま豊富な選択肢を提供。

### [1.2.0] - 2026-11-08
#### 修正・追加
- **ポーションツールチップ内部名称バグの修正**: 上位レアリティのポーションアフィックスで `affix.%s.desc` という未定義の内部キーが表示されていた不具合を修正。バニラApotheosis標準の翻訳書式（`target.toComponent(...)` および `affix.apotheosis.cooldown`）に統一し、効果名・レベル・持続時間・クールダウンが日英両言語で正確にローカライズ表示されるよう改善。
- **動的ポーションアフィックス生成（Dynamic Potion Pool）の追加**: バニラおよび導入MOD（Iron's Spells 等）の多彩なMobEffectを活用した20種類以上の新規ポーションアフィックスを自動生成・登録：
  - 攻撃時対象デバフ: 猛毒、衰弱、鈍足、盲目、発光、浮遊、暗闇、空腹、およびISS効果（引き裂き、凍傷、感電、疫病）。
  - 攻撃時自身バフ: 剛力（攻撃上昇）、神速（移動上昇）、治癒（再生）、金剛（耐性）、防護（衝撃吸収）、跳躍、疾風（加速）、耐火、霊力（マナ奔流）。
  - 防御時自身バフおよび反撃デバフ（防具・盾用）。
- **近接武器間のポーション効果相互付与（Cross-Category Potions）**: 重武器専用だった毒や出血などの戦闘ポーションアフィックスを剣や三叉槍でも相互にロール可能に。
- **特殊アビリティアフィックスの上位拡張**: 剣の `ThunderstruckAffix`（連鎖雷撃）や `FestiveAffix`（撃破時花火・ドロップ増加）を上位レアリティ（Artifact, Heirloom, Esoteric）でもロール可能に自動スケーリング。
- **設定項目の追加**: `[features]` セクションに `enableDynamicPotionExpansion`, `allowCrossCategoryPotions`, `enableSpecialAffixExpansion` を追加。

### [1.1.0] - 2026-11-08
#### 追加・変更
- **ポーション・モブ効果アフィックス（PotionAffix）の動的スケーリング拡張**: 属性アフィックスに加えて、ポーション効果アフィックスもMythicから上位レアリティ（Artifact, Heirloom, Esoteric）へ動的移植・拡張する機能を追加。持続時間延長、Esotericでの効果レベル（Amplifier）上昇、クールダウン短縮を自動計算。
- **Apotheotic Additionsの不具合修正**: ブロック採掘時効果である `haste_break` が剣やトライデントに誤って付与されてしまうAAの定義バグを自動修正・除外。
- **機能個別トグル設定の追加**: `[features]` セクションに `enableAttributeExpansion`, `enablePotionExpansion`, `fixApotheoticAdditionsBugs` を追加し、修正単体での利用や特定機能のみの利用を自由に切り替え可能に。
- Esotericの要求ability枠に対して候補が足りず、剣のアフィックスが毎回同じ5種類で固定化されてしまう問題を根本的に解消。

### [1.0.3] - 2026-11-08
#### 追加・変更
- 低重力効果の完全除外から、**最大50%（-0.50）を上限とする自動クランプ機構**（`maxGravityReduction = 0.50`、コンフィグで調整可能）へ刷新。
- 上位レアリティ（Artifact, Heirloom, Esoteric）でも低重力アフィックスが正常に付与されつつ、90%等の過剰軽減による「ジャンプ不可能」や「空中静止」の不具合を完全に防止。
- `StepFunction` スケーリング計算時および `AttributeModifier` 適用時の2重保護により、他MODや既存装備であっても安全に50%上限を維持。

### [1.0.2] - 2026-11-08
#### 追加
- 低重力効果のデフォルトブラックリスト追加。

### [1.0.1] - 2026-11-07
#### 修正
- Mixinセッターによる `IllegalAccessError` の修正とパッケージ名の変更。

### [1.0.0] - 2026-11-07
#### 追加
- **Apotheotic Additions Compat** の初回リリース。
