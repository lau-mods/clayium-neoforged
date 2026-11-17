# Clayium 開発・検証

- Minecraft 1.21.1、NeoForge 21.1.264、Java 21 を使用する。バージョンの正本は `gradle.properties`。
- Mod ID は `clayium`。Java の `ClayiumMod.MOD_ID` と生成される Mod metadata を一致させる。
- 通常のビルド確認は `./gradlew build`。

## Harness E2E runtime

- `conveyor` の構成を参考に、独立した runtime を `.harness-state/runtime/` に置く。兄弟リポジトリの runtime を直接参照・変更しない。
- MC Pilot CLI 0.20.2 は `pilot/node_modules/.bin/mct`。`pilot/node_modules/@kzheart_/mc-pilot/data/variants.json` の `1.21.1-neoforge` も NeoForge 21.1.264 に合わせる。
- 専用サーバーは `server/`、接続先は `127.0.0.1:25593`。`server.properties` の `server-ip=127.0.0.1` を維持する。
- クライアントは `clayium-a`、offline account は `clayium_a`、Minecraft 1.21.1 / NeoForge 21.1.264、画面は 1280×720。作成時の WebSocket port は 25594、Harness 起動時は空きポートを割り当てる。
- サーバーは Java 21、最大ヒープ 3 GiB。クライアントは macOS ARM64 の `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java`、最大ヒープ 3072 MiB。別環境では instance の Java パスを調整する。
- サーバーは offline mode、creative、peaceful の flat world。`harness-world` は各 E2E 起動時に削除・再生成される。保存用ワールドを置かない。
- Harness が自 Mod の JAR を server と client の `mods/` に配置し、クライアントへ operator 権限を付与する。client には MC Pilot の `mct-client-mod-neoforge-1.21.1.jar` も必要。
- JEI 19.64.0.455（Minecraft 1.21.1 NeoForge 版）を server と client の `mods/` に配置する。配布元は BlameJared Maven の `mezz.jei:jei-1.21.1-neoforge:19.64.0.455`。再構築時も同じ JAR を両側へ配置する。
- Gradle の JEI バージョンは `gradle.properties` の `jei_version` が正本。`localRuntime` で開発起動時に読み込み、配布 Mod の必須依存にはしない。
- runtime 本体、キャッシュ、ログ、撮影画像は Git 管理外。設定の正本は `.harness-config.json`。

MC Pilot を手動実行するときも Harness と同じ環境を使用する。

```sh
export MCT_HOME="$PWD/.harness-state/runtime/mct-home"
export MCT_CACHE_DIR="$PWD/.harness-state/runtime/mct-cache"
export MCT_SKILL_TARGETS=none
.harness-state/runtime/pilot/node_modules/.bin/mct client list
```

再構築時は上記バージョンの MC Pilot、NeoForge dedicated server と client を用意する。サーバーの `eula.txt` を設定し、`mct client create clayium-a --version 1.21.1 --loader neoforge --ws-port 25594 --account clayium_a --width 1280 --height 720 --java <Java 21 executable>` で client を作成する。

runtime の検証は Harness の `deployMod` → `startRuntime` → 撮影 → `stopRuntime` を通し、自 Mod が両側で読み込まれ、world に参加できることを確認する。ローカル疎通確認のログ・画像は `.harness-state/runtime/smoke/` に保存する。製品 AC の E2E シナリオは `tests/e2e/` に置く。

## 登録と Datagen の追加規約

- Registry 本体は `ClayiumRegistries` に一度だけ定義する。機能ごとの登録物は `registry` パッケージの `*Registration` クラスから既存の DeferredRegister へ追加し、新しい集約クラスを作らない。
- Block と対応 Item は同じ名前付き path を使う。metadata、数値 ID、旧 TileEntity 別名は作らない。
- BlockState、ItemModel、英日 Lang、Tag、LootTable、Recipe は `ClayiumDataGenerators` からドメイン別 Provider を追加し、`runData` の成果物を `src/generated/resources/` にコミットする。
- PNG は Datagen で生成しない。原作 JAR のバイトを維持し、旧 `textures/blocks` と `textures/items` だけ現行の単数形ディレクトリへ移す。

## 原作登録台帳

`CBlocks`、`CItems`、`CMaterials` と旧 lang の調査結果を次の方針で名前付き ID へ移す。`OriginalClassInventory` は CBlocks 92フィールド、カプセルを除く CItems 31フィールド、CMaterials 268素材・25形状を保持する。33 機械系列の機械可読台帳は `OriginalRegistryCatalog` が Tier 列と名前付き ID 規則を保持する。原作 `.lang` の表示名 747 種と `CMaterials.materialShapeMap` の実在する 245 組の原作キー・登録予定 ID は `data/clayium/original_varieties.tsv` に記録する。GameTest は現在登録済みの ID をこの台帳と照合する。

| 原作区分 | 1.21.1 の追跡単位 | 状態・除外理由 |
|---|---|---|
| `CBlocks` の通常 Block | metadata ごとの snake_case 名前付き Block ID | 対象。圧縮粘土、鉱石、筐体、素材、設備、物流、CA、PAN を各担当 Feature で登録 |
| `CBlocks` の33機械系列 | 下表の ID stem と Tier の組 | 対象。全168機種を欠番のまま追跡 |
| `CItems` の工具・フィルター・銃・ガジェット | 品種ごとの snake_case Item ID | 対象。旧 damage 値は名前付き ID または Data Component へ移行 |
| `CItems.itemsCapsule` | `capsule_1000mb`、`capsule_125mb`、`capsule_25mb`、`capsule_5mb`、`capsule_1mb` | 除外。NeoForge Fluid Capability の直接輸送へ置換 |
| `CMaterials` の素材×形状 | 原作 `materialShapeMap` に実在する組合せだけ | 対象。未定義の直積は生成しない |
| 外部 Mod 固有の旧互換素材名 | 共通タグまたは導入済み Mod の条件付きデータ | 初回必須登録から除外。特定 Mod 依存を作らない |
| 数値 Block/Item/Entity ID | なし | 除外。名前空間付き Registry ID のみ使用 |
| 旧 TileEntity 代替名 | なし | 除外。旧ワールド移行を実装しない |

M01 の登録済み原作品種は `blockCompressedClay:0` → `dense_clay`、`:1` → `compressed_clay`、`blockClayOre:0` → `clay_ore`、`blockClayium` → `clayium_block`、`blocksBendingMachine:1` → `clay_bending_machine`、`itemDusts:IndustrialClay` → `industrial_clay_dust`、`itemClayWrench` → `clay_wrench`、`itemClayPickaxe` → `clay_pickaxe`、`itemGadgetHolder` → `gadget_holder` とする。機械系列の ID は Tier 1 の BendingMachine を `clay_bending_machine`、他の変種を `<stem>_tier_<Tier>` とする。

M02 では `CompressionTier` の旧 metadata 0～12 を `dense_clay`、`compressed_clay`、`industrial_clay`、`advanced_industrial_clay`、`energetic_clay`、`compressed_energetic_clay`、`double_compressed_energetic_clay`、`triple_compressed_energetic_clay`、`quadruple_compressed_energetic_clay`、`quintuple_compressed_energetic_clay`、`sextuple_compressed_energetic_clay`、`septuple_compressed_energetic_clay`、`octuple_compressed_energetic_clay` に対応させる。粘土換算量は順に 9、81、729、6,561、59,049、531,441、4,782,969、43,046,721、387,420,489、3,486,784,401、31,381,059,609、282,429,536,481、2,541,865,828,329。CE 価値は旧 metadata 0～3 が 0、4～12 が順に 1、10、100、1,000、10,000、100,000、1,000,000、10,000,000、100,000,000。隣接段階は Datagen の 9:1 圧縮・解凍クラフトで往復する。

`original_varieties.tsv` にある 245 素材×形状だけを `MaterialRegistration` が登録する。`CLAY:BALL` と `CLAY:BLOCK` はバニラ、`DENSE_CLAY:BLOCK` と工業用・発展工業用・8重圧縮エネルギー粘土の BLOCK は既存圧縮段階に対応する。原作に単独の品目がある `impure_osmium_ingot` も登録する。`original_material_properties.tsv` は原作の形状別 Tier と色3層、`original_material_translations.tsv` は原作の英日表示名の正本。回路・Clay Core・モーター等の名前付き部品は `COMPONENT_DEFINITIONS` に置く。粉・インゴット・板・大板・宝石の `c:` タグは登録 ID の素材名を使う。汎用アイコンは原作 PNG の base/dark/light 層を不透明 ARGB の tint で着色し、専用 PNG は無改変で参照する。

M04 の CE は `ClayEnergy` の非負 `long` 内部値で保持し、1 CE を 100,000 内部値とする。通常機械用の `ClayEnergyStorage` は FE Capability を公開せず、Tier 4 以上の圧縮エネルギー粘土だけを不足時に燃料へ変換する。`TierManager` の一般機械、精錬機、CA 凝縮機、通常搬送、バッファ搬送の既定値は原作 hotfix2 の `UtilTier.setTierManagers` を正本とする。正のレシピは原作の切り捨て後も最低 1 内部値・1進捗を必要とする。


| 原作系列 | 名前付き ID stem | Tier |
|---|---|---|
| BendingMachine | `bending_machine` | 1,2,3,4,5,6,7,9 |
| WireDrawingMachine | `wire_drawing_machine` | 1,2,3,4 |
| PipeDrawingMachine | `pipe_drawing_machine` | 1,2,3,4 |
| CuttingMachine | `cutting_machine` | 1,2,3,4 |
| Lathe | `lathe` | 1,2,3,4 |
| Condenser | `condenser` | 2,3,4,5,10 |
| Grinder | `grinder` | 2,3,4,5,6,10 |
| Decomposer | `decomposer` | 2,3,4 |
| MillingMachine | `milling_machine` | 3,4 |
| Assembler | `assembler` | 3,4,6,10 |
| Inscriber | `inscriber` | 3,4 |
| Centrifuge | `centrifuge` | 3,4,5,6 |
| Smelter | `smelter` | 4,5,6,7,8,9 |
| Buffer | `buffer` | 4,5,6,7,8,9,10,11,12,13 |
| MultitrackBuffer | `multitrack_buffer` | 4,5,6,7,8,9,10,11,12,13 |
| ChemicalReactor | `chemical_reactor` | 4,5,8 |
| SaltExtractor | `salt_extractor` | 4,5,6,7 |
| AlloySmelter | `alloy_smelter` | 6 |
| ClayInterface | `clay_interface` | 5,6,7,8,9,10,11,12,13 |
| RedstoneInterface | `redstone_interface` | 5,6,7,8,9,10,11,12,13 |
| ElectrolysisReactor | `electrolysis_reactor` | 6,7,8,9 |
| ClayLaserInterface | `clay_laser_interface` | 7,8,9,10,11,12,13 |
| Distributor | `distributor` | 7,8,9 |
| AutoCrafter | `auto_crafter` | 5,6,7,8,9 |
| Transformer | `transformer` | 7,8,9,10,11,12 |
| CACondenser | `ca_condenser` | 9,10,11 |
| CAInjector | `ca_injector` | 9,10,11,12,13 |
| CAReactorCore | `ca_reactor_core` | 10,11,12,13 |
| ClayEnergyLaser | `clay_energy_laser` | 7,8,9,10 |
| PANAdapter | `pan_adapter` | 10,11,12,13 |
| PANDuplicator | `pan_duplicator` | 4,5,6,7,8,9,10,11,12,13 |
| FluidTranslator | `fluid_translator` | 4,5,6,7,8,9,10,11,12,13 |
| FluidTransferMachine | `fluid_transfer_machine` | 5 |

## M03 機械レシピ

Serializer は `clayium:machine`。RecipeType は加工方法ごとに `clayium:bending_machine`、`wire_drawing_machine`、`pipe_drawing_machine`、`cutting_machine`、`lathe`、`milling_machine`、`condenser`、`grinder`、`decomposer`、`ec_condenser`、`assembler`、`inscriber`、`centrifuge`、`smelter`、`chemical_reactor`、`alloy_smelter`、`blast_furnace`、`electrolysis_reactor`、`reactor`、`matter_transformer`、`ca_condenser`、`ca_injector`、`ca_reactor`、`ec_decomposer`、`fluid_transfer_machine`（すべて `clayium:` 名前空間）を登録する。

レシピ JSON は `type: clayium:machine`、`processing_method`（上記 ID）、非負の `required_tier`、`clay_energy`（内部 long）、正の `processing_ticks` を必須にする。`item_inputs` は `{ingredient, count}`、`fluid_inputs` と `fluid_outputs` は FluidStack、`item_outputs` は ItemStack、`random_outputs` は `{stack, weight, chance}` の配列で、省略時は空配列。出力配列の少なくとも一つに要素が必要。`weight` は正、`chance` は 0～1。`environment` は省略時に制限なし、指定時は `dimension`、`minimum_y`、`maximum_y`、`requires_sky` を扱い、Y の下限は上限以下とする。NeoForge の `neoforge:conditions` で欠落した外部品目を無効化する。`ClayiumRecipeEnergy.e(v,t)` は `floor(v × 10^(t-2))`、原作の `floor(100 × v × 10^(t-4))` と同じ値を返す。

サーバーのデータパック適用後、全プレイヤー向け `OnDatapackSyncEvent` でキャッシュを失効させ、新しい RecipeManager を持つ `ClayiumRecipesReloadedEvent` を機械・表示・PAN に通知する。クライアントの `RecipesUpdatedEvent` でも検索キャッシュを更新する。GameTest 用レシピ ID は `gametest/machine_recipe`、`gametest/conditional_missing_external_item`、`gametest/reload_changed`、`gametest/reload_removed`。テスト中の追加レシピは `gametest/reload_added`。
