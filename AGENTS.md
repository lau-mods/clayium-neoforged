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

M01 の登録済み原作品種は `blockCompressedClay:0` → `dense_clay`、`:1` → `compressed_clay`、`blockClayium` → `clayium_block`、`blocksBendingMachine:1` → `clay_bending_machine`、`itemDusts:IndustrialClay` → `industrial_clay_dust`、`itemClayWrench` → `clay_wrench`、`itemClayPickaxe` → `clay_pickaxe`、`itemGadgetHolder` → `gadget_holder` とする。`blockClayOre:0` → `clay_ore` は M05 の `WorldgenRegistration` が担当する。機械系列の ID は Tier 1 の BendingMachine を `clay_bending_machine`、他の変種を `<stem>_tier_<Tier>` とする。

M05 の `WorldgenRegistration` は原作 `blockClayOre:0/1/2` をそれぞれ `clay_ore`、`dense_clay_ore`、`large_dense_clay_ore` に対応させ、`deepslate_clay_ore`、`deepslate_dense_clay_ore`、`deepslate_large_dense_clay_ore` を追加する。通常鉱石は幸運が効く粘土玉4～16個、シルクタッチでは自身を落とし、高密度・大型は工具や幸運に関係なく適正採掘時に自身を1個落とす。全6種の採掘ランクは石ツルハシ、硬度3.0、爆発耐性5.0。深層岩版モデルも対応する原作の石版PNGをそのまま参照する。

`clayium:composite_clay_ore` は `targets`（石・深層岩の置換タグと3ランクの状態）、`primary_size`、`dense_size`、`large_chance`、`discard_chance_on_air_exposure`、`independent_large` をCodecで読み込む。主要層は通常鉱脈の同基点に高密度部分を作り、高密度の基点+8,+0,+8だけを50%で大型化する。独立大型層は大型鉱石だけを生成する。露出抑制の初期値は0である。

| M05 の ConfiguredFeature / PlacedFeature ID | Y（両端を含む） | 試行回数/chunk | サイズ | 高密度サイズ |
|---|---:|---:|---:|---:|
| `clay_ore_main_middle` | 16～95 | 8 | 24 | 10 |
| `clay_ore_main_deep` | -48～15 | 3 | 24 | 10 |
| `large_dense_clay_ore_middle` | 8～23 | 1 | 6 | 独立 |
| `large_dense_clay_ore_deep` | -48～-9 | 1 | 6 | 独立 |

`add_clay_ores` NeoForge Biome Modifier は4層を `#minecraft:is_overworld` の `underground_ores` に追加する。新規チャンクだけが対象で、既存チャンクは再生成しない。

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

## M18 ガジェット

`gadget_holder` は16スロットで、装着品は ItemStack の `clayium:gadgets` Data Component に保存する。17品目と原作キーは `clay_gadget_template`=`item.itemGadget.Blank.name`、`clay_gadget_overclock_i/ii/iii/iv`=`item.itemGadget.AntimatterOverclock/PureAntimatterOverclock/OECOverclock/OPAOverclock.name`、`clay_gadget_flight/free_flight/supersonic_flight`=`item.itemGadget.Flight0/Flight1/Flight2.name`、`clay_gadget_extra_health_i/ii/iii`=`item.itemGadget.Health0/Health1/Health2.name`、`clay_gadget_economical_auto_eat/auto_eat`=`item.itemGadget.AutoEat0/AutoEat1.name`、`clay_gadget_repeatedly_attack`=`item.itemGadget.RepeatedlyAttack.name`、`clay_gadget_long_arm/longer_arm/longest_arm`=`item.itemGadget.LongArm0/LongArm1/LongArm2.name`。速度・跳躍ガジェットは登録しない。

プレイヤーの永続状態は `clayium:gadget_player_state` Data Attachment に置く。固定 modifier ID は `clayium:gadget_extra_health`（最大体力 +20/+80/+180、保存する）、`clayium:gadget_long_arm`（block_interaction_range +3/+7/+20）、`clayium:gadget_flight`（NeoForge `CREATIVE_FLIGHT` +1）。オーバークロック I～IV の使用間隔は8/4/2/1 tick、超音速飛行速度は0.2。通常飛行は飛行中80 tickごとに空腹消耗1、自由飛行は消耗なし。倹約自動摂食は不足空腹値を超えない最大栄養、通常型は最小栄養を選び、有害効果の食品は除く。効果は毎 PlayerTick とログイン・次元移動・死亡後 Clone で再評価し、ホルダー取外しで解除する。ログアウト時は保存前に体力 modifier を外さない。

## M03 機械レシピ

Serializer は `clayium:machine`。RecipeType は加工方法ごとに `clayium:bending_machine`、`wire_drawing_machine`、`pipe_drawing_machine`、`cutting_machine`、`lathe`、`milling_machine`、`condenser`、`grinder`、`decomposer`、`ec_condenser`、`assembler`、`inscriber`、`centrifuge`、`smelter`、`chemical_reactor`、`chemical_metal_separator`、`alloy_smelter`、`blast_furnace`、`electrolysis_reactor`、`reactor`、`matter_transformer`、`ca_condenser`、`ca_injector`、`ca_reactor`、`ec_decomposer`、`fluid_transfer_machine`、`salt_extractor`、`clay_work_table`（すべて `clayium:` 名前空間）を登録する。

レシピ JSON は `type: clayium:machine`、`processing_method`（上記 ID）、非負の `required_tier`、`clay_energy`（内部 long）、正の `processing_ticks` を必須にする。`item_inputs` は `{ingredient, count}`、`fluid_inputs` と `fluid_outputs` は FluidStack、`item_outputs` は ItemStack、`random_outputs` は `{stack, weight, chance}` の配列で、省略時は空配列。出力配列の少なくとも一つに要素が必要。`weight` は正、`chance` は 0～1。`environment` は省略時に制限なし、指定時は `dimension`、`minimum_y`、`maximum_y`、`requires_sky`、`requires_adjacent_water_source` を扱い、Y の下限は上限以下とする。NeoForge の `neoforge:conditions` で欠落した外部品目を無効化する。`ClayiumRecipeEnergy.e(v,t)` は `floor(v × 10^(t-2))`、原作の `floor(100 × v × 10^(t-4))` と同じ値を返す。

サーバーのデータパック適用後、全プレイヤー向け `OnDatapackSyncEvent` でキャッシュを失効させ、新しい RecipeManager を持つ `ClayiumRecipesReloadedEvent` を機械・表示・PAN に通知する。クライアントの `RecipesUpdatedEvent` でも検索キャッシュを更新する。GameTest 用レシピ ID は `gametest/machine_recipe`、`gametest/conditional_missing_external_item`、`gametest/reload_changed`、`gametest/reload_removed`、`gametest/chemical_fluid_input`。テスト中の追加レシピは `gametest/reload_added`。

## M06 共通加工機と加工レシピ

- `condenser_tier_{2,3,4,5,10}`、`grinder_tier_{2,3,4,5,6,10}`、`decomposer_tier_{2,3,4}` は同じ `processing_machine` BlockEntity と Menu を使う。Block/Item は同名 ID とする。
- 在庫は入力 0、出力 1、処理中退避 2、エネルギー粘土 3 の4スロット。退避スロットは GUI と Capability から操作できない。入力は処理開始時に退避し、処理完了時に消費する。出力満杯や CE 不足では進捗を止め、レシピ削除・変更時には退避分を入力へ戻す。保存 NBT は `inventory`、`energy`、`state`、`active_recipe`、`pending_outputs`、`pending_fluid_output`、`fluid_input`、`reserved_fluid`、`fluid_output`、`progress`、`required_progress`、`energy_per_progress`、`progress_remainder`、`progress_per_tick`、`faces` を使用する。
- ItemHandler は上面・水平面が入力、下面が出力。各面を入力・出力・無効に変更できる。`machine_face_mode` Payload はサーバーで開いている `processing_machine` メニュー、位置、距離、方向とモードの値域を検証する。
- 凝縮・粉砕・分解のレシピ JSON は `CondenserGrinderDecomposerRecipes` から生成する。Raw Ore 追加は M17 の担当であり、この系列では粘土・素材の加工を対象とする。

## M13 組立・印刷・遠心分離

- `AssemblySeries` は `assembler_tier_{3,4,6,10}`、`inscriber_tier_{3,4}`、`centrifuge_tier_{3,4,5,6}` の10機種を定義する。すべて共通の `processing_machine` BlockEntity と Menu を使い、対応する加工方法は `assembler`、`inscriber`、`centrifuge` である。
- 組立・印刷機は入力2、出力1、遠心分離機は入力1、出力4を使う。入力 `0..n-1`、出力 `n..n+outputSlots-1`、処理中退避 `n+outputSlots..2n+outputSlots-1`、燃料 `2n+outputSlots` の順に配置する。GUI は保存 CE ではなく、レシピと Tier 倍率からサーバーが計算した必要 CE を表示する。
- 機械筐体は Tier 3 `simple_machine_hull`、4 `basic_machine_hull`、5 `advanced_machine_hull`、6 `precision_machine_hull`、10 `antimatter_machine_hull`。レシピ ID は `machine_hull/tier_*`、`assembly_machine/*`、`assembly_parts/*`、`assembler/*`、`inscriber/*`、`centrifuge/*`、`reactor/clay_core` に分ける。Tier 3・4の3系列は作業台で製作でき、高Tier機の組立は原作 hotfix2 の CE 値を使う。
- GameTest は `allTenAssemblyMachinesMatchTheOriginalTierLedger`、`everyAssemblyTierProcessesItsRepresentativeRecipe`、`hotfixTwoAssemblyBalancesAndCeValuesArePreserved`、`centrifugeMultiOutputStopsSavesAndConsumesExactlyOnce` とし、外観 E2E は `tests/e2e/assembly-machines.mjs` を使用する。

## M14 化学反応・金属分離・物質変換

`chemical_reactor_tier_{4,5,8}`、`transformer_tier_{7,8,9,10,11,12}`、Tier 6 の `chemical_metal_separator` は共通の `processing_machine` BlockEntity・Menu を使用する。化学反応機の Item スロットは入力 0～1、出力 2～3、退避 4～5、燃料 6。他の2系列は入力 0、出力 1、退避 2、燃料 3。流体は `fluid_input`、`reserved_fluid`、`fluid_output` の直接タンク各 16,000 mB を使う。

レシピ ID は `chemical_reactor/`、`chemical_metal_separator/`、`matter_transformer/` を接頭辞にする。金属分離レシピは工業用粘土粉1個、40 tick、合計2 CE、Tier 6、時間・CE倍率とも1.0。出力19種の重みは Al 200、Mg 60、Na 40、Li 7、Zr 5、Zn 10、Mn 80、Ca 20、K 15、Ni 13、Fe 9、Be 8、Pb 6、Hf 4、Cr 3、Ti 3、Sr 2、Ba 2、Cu 1、合計488。出力容量を先に確認し、処理完了時に重みで1回抽選する。

GameTest は `metalSeparatorPreservesWeightsTimeEnergyAndFullOutput` と `chemicalAndMatterMachinesUseOriginalTiersRecipesAndDirectFluids`。外観 E2E は `tests/e2e/chemical-machines.mjs`。

## M08 水車と流水動力

`tile.blockClayWaterWheel.name` は `clayium:clay_water_wheel`（Tier 1）、`tile.blockDenseClayWaterWheel.name` は `clayium:dense_clay_water_wheel`（Tier 2）に対応する。両ブロックは `clayium:water_wheel` BlockEntity を使う。

中心3×3×3の流水数を20 tickごとに走査し、毎tick `random.nextInt(40) < 流水数` なら進捗へ効率を加える。初期効率は `1000*max(tier,1)^3`、閾値は20,000。閾値到達ごとに20%の確率で効率を1下げ、隣接するTier 2～3の `processing_machine` へ `max(tier,1)^8` 内部CEを供給する。供給先の蓄積は `5*max(tier,1)^8` 内部CEを上限とする。進捗と効率は保存する。

`WaterDetection` は `FluidState` で水存在・水源・流水を区別する。水入りブロックは水存在と水源に含め、流速のない水源は流水数へ含めない。

## M07 手動製作

`clay_work_table`、`clay_crafting_board`、`raw_clay_machine_hull`、`clay_machine_hull` を Block と Item に登録する。最初の2種類は同名の BlockEntity と Menu を持つ。`clay_work_table` レシピの `required_tier` は工具操作番号1～6を表す。操作1・2は工具不要、3は `clay_rolling_pin`、4・6は `clay_slicer` または `clay_spatula`、5は `clay_spatula` を要する。工具は完了時に1耐久を消費する。

原作台帳の工具品種 `raw_clay_rolling_pin`、`raw_clay_slicer`、`raw_clay_spatula` は Tier 0 で耐久なし。焼成後の `clay_rolling_pin`、`clay_slicer` は Tier 1・耐久60、`clay_spatula` は Tier 1・耐久36。`clay_io_configurator`、`clay_piping_configurator` は Tier 1・耐久128、`memory_card` と `direction_memory_card` は Tier 1・耐久なし。既存の `clay_wrench` も初期製作対象。機械への設定複写・回転は後続の装置実装で扱う。

作業板は上下・水平の6方向を順に調べ、最初に見つかった隣接 `IItemHandler` の先頭27スロットまでを表示する。3×3の配置で製作した後、消費で空いた配置スロットを隣接在庫の同一アイテム・同一コンポーネントの品で補充する。隣接 BlockEntity が撤去された場合はメニューを無効にする。GameTest は `vanillaResourcesReachTierOneParts`、`clayWorkTablePressesConsumeInputsAndDamageTools`、`clayCraftingBoardUsesAdjacentItemHandlerWithoutLoss`、`workbenchMenusExposeOperableSlots`。外観 E2E は `tests/e2e/clay-workbench.mjs`。

## M19 粘土銃

Item ID と原作キーは `basic_clay_shooter`=`item.itemClayShooter0.name`、`advanced_clay_shooter`=`item.itemClayShooter1.name`、`sniper_clay_shooter`=`item.itemClayShooter2.name`、`sniper_clay_shooter_pro`=`item.itemClayShooter3.name`、`instant_teleporter`=`item.itemInstantTeleporter.name`。Entity ID は `clay_ball` と `teleport_ball`。

| 銃 | 耐久 | 直進 tick | 初速 | 拡散 | ダメージ | 原作射撃 rate | チャージ tick | Tier |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| 粘土銃 | 10000 | 2 | 1.2 | 25 | 4 | 6 | 0 | 6 |
| 発展粘土銃 | 1000 | 4 | 4.5 | 3 | 7 | 12 | 0 | 6 |
| スナイパー | 8000 | 2 | 15 | 0 | 25 | 8 | 20 | 6 |
| スナイパープロ | 2500 | 3 | 30 | 0 | 100 | 8 | 40 | 6 |
| テレポート | 2500 | 3 | 30 | 0 | 100 | 8 | 40 | 11 |

射撃 cooldown は原作 rate の整数除算 `rate / 3` tick。チャージ割合 `f=min(使用 tick, チャージ tick)/チャージ tick` の威力は `min((f²+2f)/3,1)`、消費耐久は `1+floor(威力×チャージ tick)`。弾丸は1200 tickで消滅する。転移は使用者と弾丸の次元一致、建築高度、ワールド境界、ロード済みチャンク、プレイヤー衝突箱の空間、`EntityTeleportEvent` を確認する。

## M16 CE→FE変換器

`EnergyConverterRegistration` は次の同名 Block/Item を登録し、全機種が `clayium:energy_converter` BlockEntity と燃料1スロットを使う。原作キーは各行の `tile.block<原作名>RFGenerator.name`。

| Tier | ID | 原作名 | 内部CE/t | FE生成/t | FE出力上限/t | FE容量 |
|---:|---|---|---:|---:|---:|---:|
| 4 | `ce_rf_converter` | Basic | 100 | 10 | 10 | 10,000 |
| 5 | `advanced_ce_rf_converter` | Advanced | 1,000 | 30 | 30 | 30,000 |
| 6 | `precision_ce_rf_converter` | Precision | 10,000 | 90 | 90 | 90,000 |
| 7 | `clay_steel_ce_rf_converter` | ClaySteel | 100,000 | 270 | 270 | 270,000 |
| 8 | `clayium_ce_rf_converter` | Clayium | 1,000,000 | 810 | 810 | 810,000 |
| 9 | `ultimate_ce_rf_converter` | Ultimate | 10,000,000 | 2,430 | 2,430 | 2,430,000 |
| 10 | `antimatter_ce_rf_converter` | Antimatter | 100,000,000 | 7,290 | 7,290 | 7,290,000 |
| 11 | `pure_antimatter_ce_rf_converter` | PureAntimatter | 1,000,000,000 | 21,870 | 21,870 | 21,870,000 |
| 12 | `oec_ce_rf_converter` | OEC | 10,000,000,000 | 65,610 | 65,610 | 65,610,000 |
| 13 | `opa_ce_rf_converter` | OPA | 100,000,000,000 | 196,830 | 196,830 | 196,830,000 |

NeoForge `Capabilities.EnergyStorage.BLOCK` の `IEnergyStorage` は `canReceive=false`、`receiveEnergy=0` とし、隣接先への push と外部からの引出だけを許す。両経路は1 tick の出力上限を共有する。赤石入力中は変換と出力を停止し、満杯時はCEを消費しない。保存NBTは `inventory`、`energy`、`fe_energy`。`OVERCLOCK_ENERGY_EXPONENT=1.0` は真正な原作 hotfix2 JAR の `mods.clayium.block.tile.TileClayRFGenerator` コンストラクタの `overclockExponent=1` と同クラスの tick 処理での `Math.pow(overclockTotalFactor, overclockExponent)` に基づく。CE消費・FE生成・FE出力上限に線形に適用し、隣接オーバークロッカーからの倍率供給は M33 で接続する。

組立レシピは `energy_converter/<id>` に10件を初期有効で生成し、全て40 tick、CEは `ClayiumRecipeEnergy.e(1.0, Tier)` とする。Tier 4 は `basic_machine_hull` と `clay_energy_excitor`、Tier 5～13 は一段下の変換器と順に `advanced_circuit`、`precision_circuit`、`integrated_circuit`、`clayium_plate`、`ultimate_alloy_plate`、`antimatter_plate`、`pure_antimatter_plate`、`octuple_energetic_clay_plate`、`octuple_pure_antimatter_plate` を使う。必要組立機Tierは変換器Tier 4～5が4、6～9が6、10～13が10。データパックの NeoForge 条件で無効化できる。GameTest は `convertersOnlyTurnClayEnergyIntoExternalFe` と `allConverterTiersPreserveRatesRecipesRedstoneAndStoredFe`。

## M09 成形加工機

- `FormingSeries` は BendingMachine Tier 1,2,3,4,5,6,7,9、WireDrawingMachine・PipeDrawingMachine・CuttingMachine・Lathe Tier 1～4、MillingMachine Tier 3,4 の26機種を定義する。登録 ID は `OriginalRegistryCatalog.machineId` に従い、Tier 1 の曲げ加工機だけ `clay_bending_machine`、その他は `<stem>_tier_<Tier>` とする。
- レシピ種別は順に `bending_machine`、`wire_drawing_machine`、`pipe_drawing_machine`、`cutting_machine`、`lathe`、`milling_machine`。すべて `processing_machine` BlockEntity と共通 GUI を使い、Tier の時間・CE 倍率は `TierManager.general` に従う。GUI の正面像は系列ごとの原作ブロック PNG を参照する。
- `FormingRecipes` が板・大板、棒、線材、管、切断品、旋盤品、フライス加工品、粘土と高密度粘土の切断ヘッド・ベアリング・スピンドル、および26機種の製作レシピを生成する。製作レシピの系列別部材は異なるため、同じ Tier のクラフト格子は衝突しない。
- GameTest は `allTwentySixFormingMachinesMatchTheOriginalTierLedger`、`representativeFormingRecipesPreserveOriginalInputsOutputsAndConditions`。E2E は `tests/e2e/forming-machines.mjs` で全機種と6系列の稼働 GUI を撮影する。

## M11 精錬・合金・電気分解

`SmeltingSeries` は `smelter_tier_{4,5,6,7,8,9}`、`alloy_smelter_tier_6`、`electrolysis_reactor_tier_{6,7,8,9}` の11機種を登録する。Smelter は `TierManager.smelter`、他の2系列は `TierManager.general` を用いる。英日表示名は原作の Smelter / Alloy Smelter / Electrolysis Reactor 各 Tier 名に対応する。

共通機械の入力数を `n` とすると、入力は `0..n-1`、出力は `n`、処理中退避は `n+1..2n`、燃料は `2n+1`。合金機だけ `n=2`、他は `n=1`。バニラ製錬フォールバック中かどうかを保存する NBT キーは `vanilla_smelting_recipe`。

Smelter のみバニラ `SMELTING` レシピをフォールバックとして参照し、基礎消費4内部CE・200 tickで処理する。これは原作 JAR の `mods.clayium.util.crafting.SmeltingRecipe` の `energySmelting=4`、`timeSmelting=200` に対応する。原作 `CRecipes.recipeSmelter` の専用レシピは Tier 5 のアルミニウム・ナトリウム・亜鉛・ニッケルと Tier 6 のマグネシウム・リチウム・ジルコニウム・亜鉛アルミニウム化合物・亜鉛ジルコニウム化合物・AZ91D・ZK60A。原作で通常かまどへ登録する不純シリコン・シリコン・シリコーン・鉄・金・鉛・銅・ブロンズ・真鍮・エレクトラム・インバーの粉→インゴットも生成し、Smelter のフォールバックから利用する。`cfgHardcoreAluminium` が有効な場合の不純アルミニウムは既定では対象外。原作 `CRecipes` では粘土鋼・クレイウム・究極合金の粉→インゴットは `recipeBlastFurnace` に登録され、Smelter のレシピではない。合金機の8レシピと電気分解機の19レシピは `SmeltingRecipes` から生成する。入力は原作 OreDictionary に相当する `c:ingots/<material>` と `c:dusts/<material>` を使う。

GameTest は `allElevenSmeltingMachinesUseOriginalTiersAndRejectFe`、`smeltingAlloyAndElectrolysisPreserveOriginalBalances`。外観 E2E は `tests/e2e/smelting-machines.mjs`。

## M10 アイテムバッファと統合フィルター

- `buffer_tier_4`～`buffer_tier_13` は `LogisticsRegistration` に登録する。BlockEntity と Menu は `clayium:buffer`、単体フィルター Menu は `clayium:item_filter`。
- 原作台帳の `white_list_item_filter` をホワイト／ブラック統合フィルター、`direction_memory_card` を設定複写アイテムとして使う。Data Component は `clayium:item_filter` と `clayium:buffer_configuration`。フィルターは手持ちで使用して編集でき、バッファ GUI からも編集できる。
- バッファは100スロットを GUI に表示し、別にフィルターと条件用見本の各1スロットを持つ。見本はフィルターを通さない。バッファの見本は破壊時にドロップし、単体フィルター画面の見本は閉じるときに返す。保存 NBT は `inventory`、`filter`、`example`、`faces`、`redstone_condition`、`transfer_limit`、`transfer_tick`、`next_output_direction`、`next_input_direction`、`selected_rule_type`、`selected_tag`。
- 各面は `INPUT`、`OUTPUT`、`DISABLED`。初期状態は下面が出力、他の面が入力。赤石条件は `IGNORE`、`POWERED`、`UNPOWERED`。数量制限0は Tier の最大値を使用する。搬送は ItemHandler Block Capability だけを通し、Tier 4～13 の周期と上限は GR-04 に従う。
- `clayium:buffer_control` の action 0～5 は Direction の面設定、6 は赤石、7 は白黒、8 は条件種別、9 は条件追加、10 は最後の条件削除、11 は数量制限、12 は次のタグ。`clayium:filter_control` の action 0～4 は白黒、条件種別、次のタグ、追加、削除。サーバーは開いた Menu と距離を確認する。
- 条件種別は `FUZZY`、`TAG`、`DISPLAY_NAME`、`INTERNAL_ID`、`MOD_ID`、`DURABILITY`、`COMPONENTS`、`NESTED`。タグは見本のタグを順送りできる。入れ子は最大16段まで評価する。
- バッファにフィルターを持って右クリックすると空きフィルタースロットに1個装着する。既存フィルターを上書きしない。`direction_memory_card` はしゃがんで使用するとコピー、通常使用で貼り付ける。

## M15 環境機械とクォーツるつぼ

- 丸石製造機は `clay_cobblestone_generator` から `clay_steel_cobblestone_generator` までの Tier 1～7を登録し、水と溶岩を水平4方向の `FluidState` で判定する。生成間隔は Tier 1～7 で順に40/20/10/5/2/1/1 tick。BlockEntity ID は `cobblestone_generator`、保存NBTは `inventory` と `progress`。水入りブロックも水として扱い、内部出力枠は Item Capability から搬出できる。
- `salt_extractor_tier_{4,5,6,7}` は共通 `processing_machine` を使う。`salt_extractor/salt` は入力品なし、上下・水平の6方向に水源隣接、Tier 4、20 tick、1進捗あたり100内部CEで塩1個を生成し、環境不成立中はCEを消費しない。
- `quartz_crucible` は不純シリコンインゴットを最大8個保持し、1個あたり600 tickの待機後、糸1個で同数のシリコンインゴットを取り出す。BlockEntity ID は `quartz_crucible`、個数と残り待機時間を `impure_silicon` と `heating_ticks` のNBTに保存し、途中破壊では保持中の不純シリコンを返す。
- GameTest は `environmentMachinesDistinguishWaterLavaSourcesAndAir`、`waterloggedFluidStatesDriveEnvironmentMachines`、`quartzCrucibleRequiresHeatingAndOneStringWithoutDuplication`。外観E2Eは `tests/e2e/environment-machines.mjs`。

## M12 粘土生産装置

`ClayProductionRegistration` は `energetic_clay_condenser_mk_i/ii`（Tier 3/4、原作 ECCondenser）、`automatic_clay_condenser_mk_i/ii`（Tier 5/7、原作 AutoClayCondenser）、`solar_clay_fabricator_mk_i/ii`（Tier 5/6）、`lithium_solar_clay_fabricator`（Tier 7）、`clay_fabricator_mk_i/ii/iii`（Tier 8/9/13）を同名 Block/Item で登録する。BlockEntity と Menu は `clayium:clay_production_machine`。原作キーは順に `tile.blockEnergeticClayCondenser.name`／`tile.blockEnergeticClayCondenserMK2.name`、`tile.blockAutoClayCondenser.name`／`tile.blockAutoClayCondenserMK2.name`、`tile.blockSolarClayFabricatorMK1.name`／`tile.blockSolarClayFabricatorMK2.name`、`tile.blockLithiumSolarClayFabricator.name`、`tile.blockClayFabricatorMK1.name`／`MK2.name`／`MK3.name` に対応する。

在庫は入力0、出力1、処理中退避2、燃料3、圧縮上限見本4。燃料はエネルギー粘土凝縮機だけ、上限見本は全自動圧縮機だけが使用する。保存キーは `inventory`、`energy`、`state`、`progress`、`required_progress`、`energy_per_tick`、`displayed_ce_per_tick`、`active_input_level`、`active_recipe`。エネルギー粘土凝縮の `clayium:ec_condenser` レシピは `clayproduction/ec_condenser_level_4`～`12`。9個を入力し、必要Tier・CE・tickをデータパックのレシピから読む。全自動圧縮は9:1、Mk II は出力の再圧縮を同 tick に進める。

ソーラーは上面の空が見え、昼で雨が降っていない間だけ進む。Mk I/II の入力段階上限は4/6、リチウム型は9で `lithium_ingot` を段階8として段階9へ変換する。ソーラー所要tickは `floor(base^入力段階 × 初期倍率)`、初期倍率は `ClayProductionRules.solarTicks` に定義し、表示CE/tは `(出力CE内部値−入力CE内部値)/所要tick`。粘土製造機は見本を保持し、そのスタック数を複製する。Mk I は段階11（6重）まで、Mk II/III は段階13（8重）まで。Mk I/II の時間は `max(1, floor(base^段階 × 個数^指数 × 初期倍率))`、Mk III は1 tick。GameTest は `ClayProductionGameTests` の凝縮・ソーラー・複製テスト、外観 E2E は `tests/e2e/clay-production.mjs`。

## M17 現行バニラ素材

`VanillaCompatRecipes` は `grinder/raw_{iron,gold,copper}`（Raw Ore 1個→対応粉1個）、`grinder/raw_{iron,gold,copper}_block`（原鉱石ブロック1個→対応粉9個）、`grinder/{iron,gold,copper}_ore`（石・深層岩鉱石1個→対応粉2個）を生成する。いずれも Tier 4、40 tick、`ClayiumRecipeEnergy.e(1.0, 4)`。入力は NeoForge の `Tags.Items.RAW_MATERIALS_*`、`STORAGE_BLOCKS_RAW_*`、`ORES_*` を使用し、バニラの鉱石 LootTable は変更しない。Raw Ore と粉の精錬はそれぞれ1個のインゴットを作る。

原木・葉・苗木の物質変換系列にサクラ、マングローブ、ツツジ、開花ツツジを追加し、既存系列と同じ Tier 8 の段階で処理する。粘土作業台・作業板の製作は `minecraft:planks` を使い、竹板材を受け付ける。竹は苗木として扱わない。`clayium:laser_tree_sources` は `#minecraft:saplings` で、サクラ苗木、マングローブの芽、ツツジ、開花ツツジを含む。レーザーによる利用は M32 が担当する。

GameTest は `rawOresPreserveMetalBalanceAndVanillaDrops` と `modernWoodsUseTagsAndBambooIsNotASapling`。

## M20 複線バッファと分配機

`MultitrackRegistration` は `multitrack_buffer_tier_4`～`13` の10種、`distributor_tier_7`～`9` の3種を登録する。BlockEntity と Menu の ID は `multitrack_buffer` と `distributor`。原作 JAR の `TileMultitrackBuffer.initParamsByTier` に従い、トラック数は Tier 4～8 が順に2、3、4、5、6、Tier 9～13 が6である。色は `DyeColor` の先頭から順に使う。7トラック固定ではない。

複線バッファはトラックごとに9スロットの在庫、フィルター、6面の `INPUT`／`OUTPUT`／`DISABLED` を持つ。同色の隣接複線バッファ間だけ直接転送する。通常の隣接在庫への Capability は面ごとの `face_tracks` で選んだ1トラックを公開する。初期面状態は下面が出力、他面が入力。保存 NBT は `tracks` 内の `inventory`、`filter`、`faces`、`next_output_direction`、`next_input_direction`、および本体の `face_tracks`、`transfer_tick`。表示中のトラックは各 Menu で保持する。`multitrack_control` は action 0～5 が選択トラックの面状態、6 が表示トラック、7 が面のトラック結合である。

分配機は9スロット在庫を持ち、初期面状態は下面が出力、他面が入力。原作 `TileClayDistributor` は Tier 7/8/9 の転送周期を各1 tick、最大数量を64/128/512とし、搬出開始面を成功時に進める。受入可能な出力面へ1個ずつ順送りし、満杯先は飛ばす。`distributor_control` の action 0～5 は面状態を変更する。保存 NBT は `inventory`、`faces`、`next_output_direction`、`next_input_direction`。複線バッファの Tier 別転送周期・数量は `TierManager.bufferTransport` を共通利用する。

GameTest は `matchingColorsCrossWithoutMixingAndKeepTierSpeedFiltersAndState` と `distributorsShareFairlyAcrossThreeTiersAndSkipFullOutputs`。外観 E2E は `tests/e2e/multitrack-logistics.mjs`。

## M23 座標同期インターフェース

- `InterfaceRegistration` は `clay_interface_tier_5`～`13`、`redstone_interface_tier_5`～`13`、`clay_laser_interface_tier_7`～`13` の25種を同名 Block/Item に登録する。ID は `<stem>_tier_<Tier>`、共通 BlockEntity/Menu は `clayium:interface`。同期工具は `clayium:synchronizer`、記録先の Data Component は `clayium:synchronized_target`。
- インターフェースの保存 NBT は `target_dimension`、`target_pos`、`owner`。シンクロナイザーは BlockEntity を右クリックして次元と座標を記録し、インターフェースを右クリックして接続する。対象の次元、ロード済みチャンク、BlockEntity、操作権限を接続時と機能アクセス時にサーバーで確認し、未ロードチャンクをロードしない。
- 状態は `UNLINKED` が登録先なし、`CONNECTED` がアクセス可能な対象、`DISCONNECTED` が記録先にアクセスできない状態。GUI は原作 PNG とともに状態、次元、座標を常時表示する。ItemHandler は対象の Capability を代理公開し、赤石制御は当該 tick の中継だけ有効とする。レーザー受信と赤石制御は BlockCapability を介して接続し、レーザー本体とマルチブロック処理は後続マイルストーンが利用する。
- GameTest は `allInterfacesSynchronizeOnlyToValidatedPersistentTargets`、外観 E2E は `tests/e2e/interfaces.mjs`。

## M24 クリエイティブ用エネルギー源

`CreativeSourceRegistration` は原作キー `tile.blockCreativeCESource.name` を同名 Block/Item `creative_ce_source` と BlockEntity `clayium:creative_ce_source` に登録する。Tier は13。毎 tick、搬出前に内部在庫の `octuple_compressed_energetic_clay` を最大64個へ補充し、通常バッファの面設定と Tier 13 の `TierManager.bufferTransport` に従って搬出する。INPUT 面は搬入せず、外部からの ItemHandler 挿入も受け付けない。面設定と在庫などの NBT は通常バッファから継承する。FE Capability とサバイバル製作レシピは持たず、クリエイティブタブからのみ入手できる。

GameTest は `replenishesSixtyFourFuelAndTransfersEveryTick` と `suppliedItemsAreCeFuelWithoutFeOrSurvivalRecipe`。

## M22 直接流体輸送

`FluidRegistration` は `fluid_translator_tier_4`～`13` と `fluid_transfer_machine_tier_5` を同名 Block/Item で登録し、共通の `clayium:fluid_transport` BlockEntity と Menu を使う。単一タンク容量と全方向合計の毎 tick 転送上限は GR-06 の値を使用する。外部接続は `Capabilities.FluidHandler.BLOCK` のみとし、1 mB 単位で直接搬送する。

面状態は `INPUT`、`OUTPUT`、`BOTH`、`DISABLED`。初期状態は下面が出力、上面と水平4面が入力。赤石条件は `IGNORE`、`POWERED`、`UNPOWERED`。`clayium:fluid_filter` Data Component を持つ流体フィルターは許可／禁止と流体 Registry ID を保存する。保存 NBT は `tank`、`pending`、`filter`、`faces`、`redstone_condition`、`next_output_direction`、`next_input_direction`。

搬送は SIMULATE 後に実行し、実行時に受入量が減った差分は永続保留タンクへ格納して次 tick 以降に先に再送する。差分全量を保留できない搬送は開始しない。Capability 経由で同 tick に受け渡された装置はその tick の自動搬送を行わず、面が `BOTH` の場合も同じ面へ同 tick に往復させない。5容量のカプセル Item・レシピ・設定・流体 ID 表は追加しない。GameTest は `FluidTransportGameTests`、外観 E2E は `tests/e2e/fluid-transport.mjs`。
