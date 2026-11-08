# Apotheotic Additions Compat

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-blue.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.0+-orange.svg)](https://files.minecraftforge.net/)
[![License: GPL-3.0-or-later](https://img.shields.io/badge/License-GPL--3.0--or--later-green.svg)](LICENSE)
[![Latest Release](https://img.shields.io/badge/Release-v1.3.4-brightgreen.svg)](releases/apotheotic_additions_compat-1.3.4-Release.jar)

[English](#english) | [日本語](#japanese)

---

<a name="english"></a>
## English

### Overview
**Apotheotic Additions Compat** is a comprehensive compatibility, balance-enforcing, and bug-fixing mod for **Minecraft 1.20.1 Forge**. It bridges the structural disparity between **Apotheotic Additions** (which introduces higher-tier loot rarities: *Artifact*, *Heirloom*, and *Esoteric*) and external magic/adventure mods (*Iron's Spells 'n Spellbooks*, *Cataclysm*, *Ars Nouveau*, etc.) that natively register Apotheosis affixes only up to the *Mythic* tier.

In addition to dynamic mathematical scaling, this mod fixes critical game-breaking bugs present in Apotheotic Additions—such as ability pool freezing on high-tier weapons, misplaced affix targets (arrow effects and mining speed generating on melee swords), and physics crashes caused by unbounded gravity modifiers.

---

### Key Problems Solved
1. **Missing Modded Affixes on Higher Rarities**: In vanilla Apotheosis, affixes are strictly mapped to explicitly defined rarity data. Because third-party mods only define stats up to Mythic, rolling gear at Artifact, Heirloom, or Esoteric completely strips away vital attributes (spell cooldown reduction, cast time reduction, mana regeneration, max mana, elemental spell power).
2. **Esoteric Weapon Ability Freeze**: Esoteric weapons require 5 ability slots. Because the pool of valid higher-tier ability affixes is severely starved in standard modded setups, Apotheosis fails to find enough candidates and deterministically assigns the exact same 5 affixes on every crafted sword.
3. **Misplaced Affix Categories in Apotheotic Additions**: Apotheotic Additions suffers from internal JSON misconfigurations, erroneously allowing block-breaking haste (`BREAK_SELF`) and ranged arrow effects (`ARROW_SELF`, `ARROW_TARGET` from tridents) to roll on melee swords.
4. **Extreme Negative Gravity Freezing**: Uncapped negative gravity affixes (`forge:entity_gravity`, `apotheosis:gravitational`) scale beyond -100% at higher tiers, causing players to freeze in mid-air, float indefinitely, or become unable to jump or walk down slopes.
5. **Placebo Immutable Registry Exceptions**: Dynamic runtime injection into Placebo registries previously failed due to post-load `ImmutableBiMap` restrictions. This mod implements dynamic unlocking and re-freezing to safely register runtime affixes.

---

### Features & Architecture

#### 1. Dynamic Attribute Affix Expansion (`AttributeAffix`)
- Automatically scans all registered `AttributeAffix` instances at datapack sync and registry reload.
- If an attribute affix possesses definitions for base rarities (e.g. Mythic or Ancient) but lacks data for higher tiers (*Artifact*, *Heirloom*, *Esoteric*), this mod calculates scaled `StepFunction` values based on configurable multipliers:
  - **Heirloom**: Focuses on solid multipliers (Default: 1.15x - 1.25x).
  - **Artifact**: Specialized in high-stat scaling (Default: 1.35x - 1.60x).
  - **Esoteric**: Ultimate endgame tier (Default: 2.00x - 2.50x).
  - **Generic / Custom Rarities**: Rank-step scaling (Default: +0.20x min, +0.35x max per ordinal rank difference).
- Supports an on-demand fallback mechanism ensuring zero null-pointer crashes if an unexpanded affix is queried during gameplay.

#### 2. Dynamic Potion / Mob Effect Affix System (`PotionAffix`)
- **Native Potion Expansion**: Scales pre-existing modded and vanilla `PotionAffix` definitions into higher tiers with extended durations, increased effect levels (amplifiers), and reduced cooldowns.
- **Rich Dynamic Potion Affix Pool**: Automatically registers dozens of balanced, lore-friendly combat potion affixes across all gear categories:
  - **Melee Weapons (`ATTACK_SELF` & `ATTACK_TARGET`)**:
    - *Self Buffs on Hit*: Strength, Speed, Jump Boost, Absorption, Regeneration, Fire Resistance, Water Breathing, Night Vision, Invisibility.
    - *Debuffs on Target*: Poison, Slowness, Weakness, Wither.
  - **Ranged Weapons (`ARROW_SELF` & `ARROW_TARGET`)**:
    - *Self Buffs on Arrow Hit*: Strength, Speed, Jump Boost, Absorption.
    - *Debuffs on Target*: Poison, Slowness, Weakness, Wither.
  - **Armor (`HURT_SELF` & `HURT_ATTACKER`)**:
    - *Self Buffs when Hurt*: Strength, Speed, Jump Boost, Absorption, Regeneration, Fire Resistance, Invisibility.
    - *Debuffs on Attacker*: Slowness, Poison, Weakness, Wither.
  - **Shields (`BLOCK_SELF` & `BLOCK_ATTACKER`)**:
    - *Self Buffs on Block*: Strength, Speed, Jump Boost, Absorption, Regeneration.
    - *Debuffs on Attacker*: Slowness, Poison, Weakness, Wither.
- **Modded Potion Pool Toggle**: Supports optional registration of Iron's Spells buffs (instant mana, evasion, fortify, oakskin, hastened, echoing strikes, charged, true invisibility) and offensive debuffs (rend, chilled, shocked, blight). Disabled by default (`enableModdedPotionPool = false`) to maintain vanilla balance.
- **Cross-Category Melee Sharing (`allowCrossCategoryPotions`)**: Allows combat potion effects (e.g. Caustic/Poison, Bloodletting/Bleed) to naturally cross-apply between Swords, Heavy Weapons, and Tridents.
- **Unambiguous Formatting & Original Text Protection**:
  - Pre-existing affixes from vanilla Apotheosis and other mods retain 100% of their original tooltip text without modification.
  - Dynamic affixes explicitly specify the target recipient: `On hit, grant self [Effect] Level [N] for [X]s (Cooldown: [Y]s)` vs `On hit, inflict on target [Effect] Level [N] for [X]s`.

#### 3. Special Ability Affix Expansion
- Dynamically scales StepFunction-based non-attribute affixes:
  - **`ThunderstruckAffix`**: Scales lightning strike chain damage and radius for Swords into higher rarities.
  - **`FestiveAffix`**: Scales firework explosions and bonus mob drop rates into higher tiers.

#### 4. Apotheotic Additions Bug Fixes
- **Ranged Arrow Effects on Swords**: Enforces strict category checking (`category.isRanged()`). Arrow impact effects will never appear on melee swords or tridents.
- **Mining Haste on Weapons**: Enforces breaker category checking (`category.isBreaker()`). Block-break speed effects (`haste_break`) will never roll on swords or combat weapons.
- **Weapon Ability Starvation**: Eliminates identical affix rolls on Esoteric weapons by expanding the pool of available abilities and mob effects.

#### 5. Gravity & Physics Safety Cap
- Intercepts attribute modifiers applied to entity gravity (`forge:entity_gravity`, `minecraft:generic.gravity`, `apotheosis:gravitational`).
- Clamps negative reduction to a safe maximum (Default: `0.50` / 50% max reduction).
- Prevents players from floating away, freezing in mid-air, or losing the ability to jump when wearing high-rarity gravity-modifying armor.

---

### Configuration Guide (`config/apotheotic_additions_compat-common.toml`)

| Category | Option Key | Default | Description |
| :--- | :--- | :--- | :--- |
| **general** | `enableCompatLogic` | `true` | Master toggle for all dynamic scaling and compatibility logic. |
| **general** | `enableOnDemandFallback` | `true` | Computes missing affixes on-the-fly if queried during gameplay. |
| **general** | `logExpansion` | `true` | Logs detailed scaling statistics to the console/log file on reload. |
| **features** | `enableAttributeExpansion` | `true` | Dynamically scales `AttributeAffix` instances to higher tiers. |
| **features** | `enablePotionExpansion` | `true` | Dynamically scales `PotionAffix` instances to higher tiers. |
| **features** | `enableVanillaPotionPool` | `true` | Registers the curated pool of vanilla brewable potion affixes. |
| **features** | `enableModdedPotionPool` | `false` | Registers modded potion affixes (e.g. Iron's Spells). Off by default. |
| **features** | `allowCrossCategoryPotions`| `true` | Allows swords, heavy weapons, and tridents to share melee potion affixes. |
| **features** | `enableSpecialAffixExpansion`| `true` | Dynamically scales Thunderstruck and Festive affixes to higher tiers. |
| **features** | `fixApotheoticAdditionsBugs` | `true` | Fixes arrow/haste_break category misplacements from Apotheotic Additions. |
| **limits** | `capGravityReduction` | `true` | Enables safety clamping on gravity reduction modifiers. |
| **limits** | `maxGravityReduction` | `0.50` | Maximum allowed gravity reduction (0.50 = 50% max reduction). |
| **multipliers**| `heirloomMinMultiplier` / `Max` | `1.15` / `1.25` | Min/Max multiplier bounds for Heirloom rarity. |
| **multipliers**| `artifactMinMultiplier` / `Max` | `1.35` / `1.60` | Min/Max multiplier bounds for Artifact rarity. |
| **multipliers**| `esotericMinMultiplier` / `Max` | `2.00` / `2.50` | Min/Max multiplier bounds for Esoteric rarity. |
| **blacklists** | `affixBlacklist` | `[]` | List of Affix IDs completely excluded from dynamic expansion. |
| **blacklists** | `attributeBlacklist` | `[]` | List of Attribute IDs completely excluded from dynamic expansion. |

---

### Installation & Requirements
1. **Minecraft**: `1.20.1`
2. **Forge**: `47.4.0` or higher
3. **Required Dependencies**:
   - [Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis) (`7.4.8`+)
   - [Placebo](https://www.curseforge.com/minecraft/mc-mods/placebo) (`8.6.3`+)
   - [Apotheotic Additions](https://www.curseforge.com/minecraft/mc-mods/apotheotic-additions) (`2.2.2`+)
4. Drop `apotheotic_additions_compat-1.3.4-Release.jar` into your `.minecraft/mods` directory.

---

<a name="japanese"></a>
## 日本語

### 概要
**Apotheotic Additions Compat** は、Minecraft 1.20.1 (Forge) において、上位レアリティ（*Artifact*, *Heirloom*, *Esoteric*）を追加する **Apotheotic Additions** と、Mythic（ミシック）までしかアフィックスを定義していない各種魔法・冒険系MOD（*Iron's Spells 'n Spellbooks*, *Cataclysm*, *Ars Nouveau* 等）との間の深刻な互換性断絶を解消し、さらにApotheotic Additions自体の各種不具合を修正する総合互換拡張MODです。

単に数値を自動スケーリングするだけでなく、高レアリティ武器のアフィックス固定化バグ、装備カテゴリと発動条件の不整合（剣に矢着弾時効果や採掘速度が付く問題）、重力過剰軽減による物理崩壊バグなど、プレイ体験を著しく損ねる既存の不具合を根底から解消します。

---

### 解決される主要な課題
1. **上位レアリティにおける他MODアフィックスの完全欠落**: バニラApotheosisの仕様上、アフィックスは定義されているレアリティにのみ出現します。他MODはMythicまでしか数値を登録していないため、ArtifactやEsotericの装備をリフォージしても、重要な属性（クールダウン短縮、詠唱速度、最大マナ、マナ再生、各属性魔法威力など）が一切付与されなくなっていました。
2. **Esoteric剣のアフィックス固定化バグ**: Esoteric武器は能力枠（ability）が5枠要求されますが、対応するアビリティアフィックス候補が極度に不足しているため、何本作っても「飛行・ドロップ格納・呪詛・採掘速度・耐久免除」という同一の5種類のアフィックスが確定で付与されていました。
3. **装備カテゴリと効果ターゲットの不整合バグ**: Apotheotic Additions側のJSON定義ミスにより、ブロック破壊時ヘイスト（`BREAK_SELF`）や、三叉槍経由の矢命中時効果（`ARROW_SELF`, `ARROW_TARGET`）が通常の近接剣にロールされてしまう不具合が存在していました。
4. **低重力効果の累積による物理・挙動崩壊**: 上位レアリティで重力軽減（`forge:entity_gravity`, `apotheosis:gravitational`）が-90%や-120%以上に達し、ジャンプ不能になったり空中に静止して降りられなくなるバグが発生していました。
5. **Placebo DynamicRegistry の不変マップ凍結による登録例外**: Placeboのレジストリがリロード完了後に `ImmutableBiMap` へ固定化されるため、動的登録時に `UnsupportedOperationException` が発生していた問題を、一時的な可変BiMapへのロック解除と再フリーズ処理によって完全に解消しました。

---

### 機能とアーキテクチャ

#### 1. 属性アフィックスの自動動的拡張 (`AttributeAffix`)
- レジストリのリロード時およびデータパック同期時に、全 `AttributeAffix` を自動走査。
- 基準レアリティ（Mythic等）が存在し、上位レアリティ（*Artifact*, *Heirloom*, *Esoteric*）が未定義の場合、コンフィグで指定された倍率に基づき `StepFunction`（最小値・最大値・ステップ幅）を自動計算・注入します：
  - **Heirloom**: 堅実な倍率スケーリング（デフォルト: 1.15倍 〜 1.25倍）。
  - **Artifact**: 高数値特化スケーリング（デフォルト: 1.35倍 〜 1.60倍）。
  - **Esoteric**: 究極のエンドゲーム性能（デフォルト: 2.00倍 〜 2.50倍）。
  - **その他未知の上位レアリティ**: ランク順序差に応じたステップ計算（デフォルト: 1ランクごとに +0.20倍 〜 +0.35倍）。
- ゲームプレイ中に未計算のアフィックスが要求された場合でも、オンデマンドで瞬時に数値を計算・補完し、NullPointerExceptionクラッシュを防止します。

#### 2. ポーション・モブ効果アフィックス動的生成＆拡張システム (`PotionAffix`)
- **既存ポーションアフィックスの上位拡張**: 既存の `PotionAffix` を上位レアリティへ自動拡張（効果時間の延長、Amplifierの上昇、クールダウンの短縮）。
- **洗練された動的ポーションアフィックスプールの自動生成**: 全装備種別に対応した高品質な戦闘用ポーションアフィックスを自動登録：
  - **近接武器（`ATTACK_SELF` / `ATTACK_TARGET`）**:
    - *攻撃ヒット時自身バフ*: 攻撃力上昇、移動速度上昇、跳躍力上昇、衝撃吸収、再生能力、火炎耐性、水中呼吸、暗視、透明化。
    - *攻撃ヒット時対象デバフ*: 毒、移動速度低下、弱体化、衰亡。
  - **遠距離武器（`ARROW_SELF` / `ARROW_TARGET`）**:
    - *弓矢命中時自身バフ*: 攻撃力上昇、移動速度上昇、跳躍力上昇、衝撃吸収。
    - *弓矢命中時対象デバフ*: 毒、移動速度低下、弱体化、衰亡。
  - **防具（`HURT_SELF` / `HURT_ATTACKER`）**:
    - *被ダメージ時自身バフ*: 攻撃力上昇、移動速度上昇、跳躍力上昇、衝撃吸収、再生能力、火炎耐性、透明化。
    - *被ダメージ時攻撃者デバフ*: 移動速度低下、毒、弱体化、衰亡。
  - **盾（`BLOCK_SELF` / `BLOCK_ATTACKER`）**:
    - *盾防御時自身バフ*: 攻撃力上昇、移動速度上昇、跳躍力上昇、衝撃吸収、再生能力。
    - *盾防御時攻撃者デバフ*: 移動速度低下、毒、弱体化、衰亡。
- **MODポーションのオプショナル対応**: Iron's Spells等の追加効果（マナ奔流、回避、強化、樹皮硬化、急速詠唱、追撃、帯電、真の不可視、引き裂き、凍結、感電、疫病など）にも対応可能。予期せぬ強力効果との組み合わせを防ぐため、デフォルトでは無効（`enableModdedPotionPool = false`）に設定されています。
- **近接武器間のポーション効果相互付与 (`allowCrossCategoryPotions`)**: 重武器専用だった毒や出血などの戦闘ポーションアフィックスを、剣や三叉槍でも相互に付与可能にします。
- **明快なツールチップ表記＆元MOD表示の完全保護**:
  - Apotheosis本体や他MODが元から持っているアフィックスの説明文は一切改変せず、元の表示を100%保持します。
  - 当MODが追加した動的アフィックスは、付与対象を明記した誤認のない書式で表示されます：
    - `攻撃ヒット時、自身に [効果名] (Lv.[N]) を [X]秒間付与（クールダウン: [Y]秒）`
    - `攻撃ヒット時、対象に [効果名] (Lv.[N]) を [X]秒間付与（クールダウン: [Y]秒）`
    - 被弾時・射撃時・盾防御時も同様に対象を完全明記。

#### 3. 特殊アビリティアフィックスの上位拡張
- 数値関数ベースの特殊能力アフィックスを上位レアリティへ動的スケーリング：
  - **`ThunderstruckAffix`**: 剣による連鎖雷撃のダメージと索敵範囲を上位レアリティで強化。
  - **`FestiveAffix`**: 撃破時のお祝い花火の爆風範囲とモブドロップ増加率を上位レアリティで強化。

#### 4. Apotheotic Additions の不具合修正
- **矢のポーション効果が剣に付くバグの修正**: ターゲット・カテゴリ適合性を厳格に検査（`category.isRanged()`）。矢命中時効果が剣や三叉槍に付与される問題を完全に排除。
- **採掘速度上昇が剣に付くバグの修正**: 破壊ツール判定（`category.isBreaker()`）を強制適用。ブロック破壊時ヘイスト（`haste_break`）が武器に付与される問題を遮断。
- **Esoteric剣のアフィックス固定化解消**: 有効なアビリティ候補数を大幅に拡張し、Esoteric剣でも多彩なアフィックスが完全ランダムにロールされるよう改善。

#### 5. 低重力効果の物理安全上限キャップ
- エンティティ重力属性（`forge:entity_gravity`, `minecraft:generic.gravity`, `apotheosis:gravitational`）に対する軽減率を安全上限（デフォルト: `0.50` / 最大50%軽減）で自動クランプ。
- 高レアリティ防具を装備しても、空中に静止して降りられなくなったり、ジャンプ不能に陥るバグを確実に防ぎます。

---

### コンフィグ設定ガイド (`config/apotheotic_additions_compat-common.toml`)

| セクション | 設定キー | デフォルト値 | 役割・解説 |
| :--- | :--- | :--- | :--- |
| **general** | `enableCompatLogic` | `true` | 全機能のマスタースイッチ。OFFでMODの全処理を停止。 |
| **general** | `enableOnDemandFallback` | `true` | 実行時に未計算のアフィックスが参照された際、即時補完するかどうか。 |
| **general** | `logExpansion` | `true` | アフィックスが上位レアリティ向けに自動計算された際、ログを出力。 |
| **features** | `enableAttributeExpansion` | `true` | Attributeアフィックスの上位レアリティ動的拡張を有効化。 |
| **features** | `enablePotionExpansion` | `true` | PotionAffixの上位レアリティ動的拡張を有効化。 |
| **features** | `enableVanillaPotionPool` | `true` | バニラ醸造可能ポーションによる動的アフィックスプールを登録。 |
| **features** | `enableModdedPotionPool` | `false` | 他MODポーション（ISS等）の動的登録。デフォルト無効。 |
| **features** | `allowCrossCategoryPotions`| `true` | 剣・重武器・三叉槍の間で近接ポーション効果を相互付与可能にする。 |
| **features** | `enableSpecialAffixExpansion`| `true` | 雷撃（Thunderstruck）や花火（Festive）の上位レアリティ拡張。 |
| **features** | `fixApotheoticAdditionsBugs` | `true` | 矢効果・採掘速度の誤付与などAAの不具合修正を有効化。 |
| **limits** | `capGravityReduction` | `true` | 重力軽減効果の安全上限クランプを有効化。 |
| **limits** | `maxGravityReduction` | `0.50` | 許容される重力軽減率の最大上限（0.50 = 最大50%軽減）。 |
| **multipliers**| `heirloomMinMultiplier` / `Max` | `1.15` / `1.25` | Heirloomレアリティの最小/最大スケーリング倍率。 |
| **multipliers**| `artifactMinMultiplier` / `Max` | `1.35` / `1.60` | Artifactレアリティの最小/最大スケーリング倍率。 |
| **multipliers**| `esotericMinMultiplier` / `Max` | `2.00` / `2.50` | Esotericレアリティの最小/最大スケーリング倍率。 |
| **blacklists** | `affixBlacklist` | `[]` | 動的拡張から除外するアフィックスIDのブラックリスト。 |
| **blacklists** | `attributeBlacklist` | `[]` | 動的拡張から除外する属性IDのブラックリスト。 |

---

### 導入方法・前提環境
1. **Minecraft**: `1.20.1`
2. **Forge**: `47.4.0` 以上
3. **必須前提MOD**:
   - [Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis) (`7.4.8` 以上)
   - [Placebo](https://www.curseforge.com/minecraft/mc-mods/placebo) (`8.6.3` 以上)
   - [Apotheotic Additions](https://www.curseforge.com/minecraft/mc-mods/apotheotic-additions) (`2.2.2` 以上)
4. 配布JARファイル（`apotheotic_additions_compat-1.3.4-Release.jar`）を `.minecraft/mods` フォルダに配置します。

---

### ライセンス (License)
本プロジェクトは **GNU General Public License v3.0 or later (GPL-3.0-or-later)** の下で公開されています。詳細については [LICENSE](LICENSE) をご覧ください。
