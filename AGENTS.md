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

NeoForge `Capabilities.EnergyStorage.BLOCK` の `IEnergyStorage` は `canReceive=false`、`receiveEnergy=0` とし、隣接先への push と外部からの引出だけを許す。両経路は1 tick の出力上限を共有する。赤石入力中は変換と出力を停止し、満杯時はCEを消費しない。保存NBTは `inventory`、`energy`、`fe_energy`。`OVERCLOCK_ENERGY_EXPONENT=1.0` は真正な原作 hotfix2 JAR の `mods.clayium.block.tile.TileClayRFGenerator` コンストラクタの `overclockExponent=1` と同クラスの tick 処理での `Math.pow(overclockTotalFactor, overclockExponent)` に基づく。CE消費・FE生成・FE出力上限に線形に適用し、隣接オーバークロッカーからの倍率供給は M34 の `AugmentScanner` で接続する。

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

原木・葉・苗木の物質変換系列にサクラ、マングローブ、ツツジ、開花ツツジを追加し、既存系列と同じ Tier 8 の段階で処理する。粘土作業台・作業板の製作は `minecraft:planks` を使い、竹板材を受け付ける。竹は苗木として扱わない。アイテムタグ `clayium:laser_tree_sources` は `#minecraft:saplings` を参照する。M32 の同名ブロックタグは旧6樹種の苗木、サクラ苗木、マングローブの芽、ツツジ、開花ツツジを明示し、照射対象を決める。

## M32 粘土樹

`ClayTreeRegistration` は `clay_tree_sapling`、`clay_tree_leaf`、`clay_tree_log` の同名 Block/Item を登録する。M29 のレーザー受信 Capability はブロックタグ `clayium:laser_tree_sources` を照合し、受信した単一ビームの `energy()` が1000以上で、粘土樹苗木が生存できる位置だけを変換する。999以下、不適格ブロック、吊り下がったマングローブの芽は変換しない。

`clayium:clay_tree` ConfiguredFeature は高さ5～7の直立幹、半径2・高さ3の葉冠を持ち、`TreeGrower` の植樹成長だけに使用する。PlacedFeature や自然生成の Biome Modifier には追加しない。原木は自身を落とす。葉はハサミ・シルクタッチで自身を落とし、通常採取では粘土粉5%、高密度粘土粉0.5%を独立抽選し、幸運補正を適用する。原木の粉砕は Tier 5・200 tick・1000内部CEで有機粘土1個、遠心分離は Tier 6・400 tick・10000内部CEで発展工業用粘土粉16、マンガン粉5、リチウム粉3、ジルコニウム粉1を出す。GameTest は `laserThresholdTagsAndNaturalGenerationMatchClayTreeRules` と `plantedTreeGrowthDropsAndOriginalProcessingRouteAreComplete`、外観 E2E は `tests/e2e/clay-tree.mjs`。

GameTest は `rawOresPreserveMetalBalanceAndVanillaDrops` と `modernWoodsUseTagsAndBambooIsNotASapling`。

## M20 複線バッファと分配機

`MultitrackRegistration` は `multitrack_buffer_tier_4`～`13` の10種、`distributor_tier_7`～`9` の3種を登録する。BlockEntity と Menu の ID は `multitrack_buffer` と `distributor`。原作 JAR の `TileMultitrackBuffer.initParamsByTier` に従い、トラック数は Tier 4～8 が順に2、3、4、5、6、Tier 9～13 が6である。色は `DyeColor` の先頭から順に使う。7トラック固定ではない。

複線バッファはトラックごとに9スロットの在庫、フィルター、6面の `INPUT`／`OUTPUT`／`DISABLED` を持つ。同色の隣接複線バッファ間だけ直接転送する。通常の隣接在庫への Capability は面ごとの `face_tracks` で選んだ1トラックを公開する。初期面状態は下面が出力、他面が入力。保存 NBT は `tracks` 内の `inventory`、`filter`、`faces`、`next_output_direction`、`next_input_direction`、および本体の `face_tracks`、`transfer_tick`。表示中のトラックは各 Menu で保持する。`multitrack_control` は action 0～5 が選択トラックの面状態、6 が表示トラック、7 が面のトラック結合である。

分配機は9スロット在庫を持ち、初期面状態は下面が出力、他面が入力。原作 `TileClayDistributor` は Tier 7/8/9 の転送周期を各1 tick、最大数量を64/128/512とし、搬出開始面を成功時に進める。受入可能な出力面へ1個ずつ順送りし、満杯先は飛ばす。`distributor_control` の action 0～5 は面状態を変更する。保存 NBT は `inventory`、`faces`、`next_output_direction`、`next_input_direction`。複線バッファの Tier 別転送周期・数量は `TierManager.bufferTransport` を共通利用する。

GameTest は `matchingColorsCrossWithoutMixingAndKeepTierSpeedFiltersAndState` と `distributorsShareFairlyAcrossThreeTiersAndSkipFullOutputs`。外観 E2E は `tests/e2e/multitrack-logistics.mjs`。

## M21 大容量収納と金属チェスト

`StorageRegistration` は `storage_container`、`void_container` と57素材の `<material>_chest` を同名 Block/Item で登録する。BlockEntity/Menu ID は `storage_container`、`void_container`、`metal_chest`。ストレージは1品目を `long` で数え、初期容量65,536、Clay Core適用後は2,147,483,647。保存 NBT は `stored`、`amount`、`expanded`。破壊ドロップは同じ値を Item の `custom_data` に保持し、設置時に復元する。外部 `IItemHandler` は1スロットを通常スタック単位で公開し、搬入と搬出はその都度実残量を更新する。虚空コンテナは明示的な ItemHandler/GUI 投入だけを破棄する。金属チェストの保存 NBT は `inventory` で、Capability は全スロットを公開する。

GR-08 の金属チェスト台帳は `素材:横×縦×ページ` とする。`SILICON:9×5×1 ALUMINIUM:9×6×1 CLAY_STEEL:9×8×1 CLAYIUM:13×8×1 ULTIMATE_ALLOY:13×8×3 MAGNESIUM:9×6×1 SODIUM:9×6×1 LITHIUM:9×7×1 ZIRCONIUM:9×7×1 ZINC:9×6×1 MANGANESE:9×6×1 CALCIUM:9×6×1 POTASSIUM:9×6×1 NICKEL:9×6×1 BERYLLIUM:10×8×1 LEAD:9×6×1 HAFNIUM:9×8×1 CHROME:13×8×1 TITANIUM:13×8×1 STRONTIUM:10×8×1 BARIUM:10×8×1 AZ91D_ALLOY:13×8×1 ZK60A_ALLOY:13×8×1 RUBIDIUM:13×3×1 CAESIUM:13×3×1 FRANCIUM:13×4×1 RADIUM:13×4×1 ACTINIUM:13×5×1 THORIUM:13×5×1 PROTACTINIUM:13×6×1 URANIUM:13×6×2 NEPTUNIUM:13×6×3 PLUTONIUM:13×6×4 AMERICIUM:13×6×5 CURIUM:13×6×6 LANTHANUM:13×2×2 CERIUM:13×2×4 PRASEODYMIUM:13×2×6 NEODYMIUM:13×2×8 PROMETHIUM:13×4×8 SAMARIUM:13×6×8 EUROPIUM:13×8×8 VANADIUM:4×8×1 COBALT:11×6×1 PALLADIUM:11×8×1 PLATINUM:13×8×2 IRIDIUM:13×8×3 OSMIUM:13×8×4 RHENIUM:13×8×5 TANTALUM:10×8×1 TUNGSTEN:13×8×1 MOLYBDENUM:13×8×2 ANTIMONY:9×6×1 BISMUTH:9×6×1 BRASS:9×6×1 ELECTRUM:13×8×1 INVAR:9×8×1`。ユーロピウムは832スロット。ページボタンは `clickMenuButton` の0始まりのページ番号でサーバーへ送る。各メニューは自分の表示ページを持ち、スロットの `isActive` とプレイヤーの shift 移動範囲を同じページへ限定する。

GameTest は `storageCapacityExpansionOverflowPersistenceAndVoidAreExact`、`storageContentsSurviveBreakingAndPlacement`、`allFiftySevenMetalChestsMatchTheCapacityLedger`、`metalChestMenusKeepIndependentServerAuthoritativePages`。外観 E2E は `tests/e2e/storage-containers.mjs`。
## M23 座標同期インターフェース

- `InterfaceRegistration` は `clay_interface_tier_5`～`13`、`redstone_interface_tier_5`～`13`、`clay_laser_interface_tier_7`～`13` の25種を同名 Block/Item に登録する。ID は `<stem>_tier_<Tier>`、共通 BlockEntity/Menu は `clayium:interface`。同期工具は `clayium:synchronizer`、記録先の Data Component は `clayium:synchronized_target`。
- インターフェースの保存 NBT は `target_dimension`、`target_pos`、`owner`。シンクロナイザーは BlockEntity を右クリックして次元と座標を記録し、インターフェースを右クリックして接続する。対象の次元、ロード済みチャンク、BlockEntity、操作権限を接続時と機能アクセス時にサーバーで確認し、未ロードチャンクをロードしない。
- 状態は `UNLINKED` が登録先なし、`CONNECTED` がアクセス可能な対象、`DISCONNECTED` が記録先にアクセスできない状態。GUI は原作 PNG とともに状態、次元、座標を常時表示する。ItemHandler は対象の Capability を代理公開し、赤石制御は当該 tick の中継だけ有効とする。レーザー受信と赤石制御は BlockCapability を介して接続し、レーザー本体とマルチブロック処理は後続マイルストーンが利用する。
- GameTest は `allInterfacesSynchronizeOnlyToValidatedPersistentTargets`、外観 E2E は `tests/e2e/interfaces.mjs`。

## M24 クリエイティブ用エネルギー源

`CreativeSourceRegistration` は原作キー `tile.blockCreativeCESource.name` を同名 Block/Item `creative_ce_source` と BlockEntity `clayium:creative_ce_source` に登録する。Tier は13。毎 tick、搬出前に内部在庫の `octuple_compressed_energetic_clay` を最大64個へ補充し、通常バッファの面設定と Tier 13 の `TierManager.bufferTransport` に従って搬出する。INPUT 面は搬入せず、外部からの ItemHandler 挿入も受け付けない。面設定と在庫などの NBT は通常バッファから継承する。FE Capability とサバイバル製作レシピは持たず、クリエイティブタブからのみ入手できる。

GameTest は `replenishesSixtyFourFuelAndTransfersEveryTick` と `suppliedItemsAreCeFuelWithoutFeOrSurvivalRecipe`。

## M25 自動クラフト装置

`auto_crafter_tier_{5,6,7,8,9}` は `clayium:auto_crafter` BlockEntity と Menu を共用する。3×3のゴーストパターン、材料9スロット、出力6スロット、CE燃料1スロットを持つ。上面と水平面の Item Capability は材料・燃料への搬入、下面は出力からの搬出に限定する。

保存 NBT は `inventory`、`pattern`、`energy`、`craft_tick`。ゴーストパターンは破壊時にドロップせず、結果プレビューは保存しない。

原作 hotfix2 の Tier 5～9 は、周期／周期内の最大製作回数／1製作あたり内部CEが順に `20/1/0`、`1/1/10`、`1/16/40`、`1/64/160`、`1/256/640`。材料はゴーストと ItemStack の Data Components まで一致させる。`CraftingInput` で通常クラフトレシピを再検証し、結果と `getRemainingItems` の全品が出力へ収まることを先に確認してから、材料とCEを確定消費する。満杯・材料不足・無効レシピでは何も消費しない。

製作レシピは `auto_crafter/auto_crafter_tier_*` に置き、M13の組立レシピ形式を使う。GameTest は `allFiveTiersCraftAtOriginalCyclesWithoutInfiniteOutput`、`blockedAndPartialCraftsNeverConsumeMaterialsOrEnergy`、`vanillaRemainingItemsAndDamagedRecipesCommitExactlyOnce`。外観E2Eは `tests/e2e/auto-crafters.mjs`。

## M22 直接流体輸送

`FluidRegistration` は `fluid_translator_tier_4`～`13` と `fluid_transfer_machine_tier_5` を同名 Block/Item で登録し、共通の `clayium:fluid_transport` BlockEntity と Menu を使う。単一タンク容量と全方向合計の毎 tick 転送上限は GR-06 の値を使用する。外部接続は `Capabilities.FluidHandler.BLOCK` のみとし、1 mB 単位で直接搬送する。

面状態は `INPUT`、`OUTPUT`、`BOTH`、`DISABLED`。初期状態は下面が出力、上面と水平4面が入力。赤石条件は `IGNORE`、`POWERED`、`UNPOWERED`。`clayium:fluid_filter` Data Component を持つ流体フィルターは許可／禁止と流体 Registry ID を保存する。保存 NBT は `tank`、`pending`、`filter`、`faces`、`redstone_condition`、`next_output_direction`、`next_input_direction`。

搬送は SIMULATE 後に実行し、実行時に受入量が減った差分は永続保留タンクへ格納して次 tick 以降に先に再送する。差分全量を保留できない搬送は開始しない。Capability 経由で同 tick に受け渡された装置はその tick の自動搬送を行わず、面が `BOTH` の場合も同じ面へ同 tick に往復させない。5容量のカプセル Item・レシピ・設定・流体 ID 表は追加しない。GameTest は `FluidTransportGameTests`、外観 E2E は `tests/e2e/fluid-transport.mjs`。

## M29 色別粘土レーザー

- `ClayEnergyLaser` は `clay_energy_laser_tier_7`～`10` の同名 Block/Item と共通 BlockEntity `clayium:clay_energy_laser` に登録する。Tier 7/8/9/10 は青/緑/赤/白、消費は毎 tick 0.4/4/40/400 CE、成分は `[1,0,0]`/`[0,1,0]`/`[0,0,1]`/`[3,3,3]`。`laser_reflector` は同名 Block/Item と BlockEntity `clayium:laser_reflector` を使う。
- 光路は最大32ブロックで、ブロック変化時と10 tick ごとに再計算する。反射器は受信したビームの age を増やして同一 tick のビームを合成し、次の tick に一度転送する。age 10以上は成分ごとの最大値で合成し、それ未満は加算する。`LaserBeam.energy()` は原作の色別減衰式の積から1を引く。`LaserReceiver` はビームと age を受け渡し、レーザーインターフェースが同期先へ中継する。
- 赤石入力中は停止する。COMMON Config の `InvertClayLaserRSCondition` が true のときは停止条件を反転する。レーザーの保存 NBT は `inventory` と `energy`。照射状態、長さ、反射ビームは表示同期だけに使う。
- GameTest は `fourLasersConsumeMergeReflectAndHonorRedstone`。描画 E2E は `tests/e2e/clay-lasers.mjs`。

## M26 自動取引装置

`TradeRegistration` は原作キー `tile.blockAutoTrader.name` の `clayium:auto_trader` を同名 Block/Item/BlockEntity/Menu に登録する。Tier 8 は原作の自動取引装置の段階に対応し、取引1回の消費は `ClayiumRecipeEnergy.e(1.0, 8)` 内部CE。装置から x/z ±1、上方 y+1～y+4 の生存 `Merchant` を探索し、取引中の商人は使用しない。

検証順は商人の存在、取引中ロック、選択オファーの存在、在庫切れ、出力空き、必要材料、CE とする。要求数は現行割引を反映した `getCostA`/`getCostB` を使い、確定時に `notifyTrade` で商人の取引回数と経験値を更新する。失敗時は在庫、CE、取引回数を変更しない。スロットは入力A 0、入力B 1、出力 2、燃料 3。上面と水平面は入力、下面は出力。保存NBTは `inventory`、`energy`、`selected_offer`、`state`。

`clayium:trade_select` Payload は開いているメニュー、位置、距離、選択番号をサーバーで検証する。GUI の取引一覧、選択、現在価格、CE は `clayium:trade_snapshot` で同期する。GameTest は `selectedTradeUsesDiscountedCostsCeOutputUsesAndVillagerExperience` と `failedConditionsAreAtomicAndSelectionPayloadIsServerValidated`、画面 E2E は `tests/e2e/auto-trader.mjs`。

## M27 基本採掘機・右クリック代行機

`MinerRegistration` は原作キー `tile.blockMiner.name` と `tile.blockActivator.name` を Tier 6 の同名 Block/Item `block_breaker` と `activator` に登録する。両方が `clayium:basic_block_operation` BlockEntity を使用する。入力はスロット0、出力は1～9で、20 tick ごとに正面の対象を操作する。保存 NBT は `inventory`、`owner`、`operation_tick`。

`BlockOperationService` は所有者 UUID の FakePlayer でサーバー側の破壊・使用イベントと権限を確認する。採掘機は LootTable と工具の幸運・シルクタッチを使い、出力容量を確認してから破壊する。右クリック代行機は機械側から対象面をクリックし、消費・耐久・返却物を在庫に反映する。保護対象と未ロードチャンクは操作せず、チャンクを強制ロードしない。GameTest は `blockBreakerUsesLootProtectionAndLoadedChunks` と `activatorMatchesPlayerUseAndDeniedActionsHaveNoSideEffects`。

## M28 3×3チャンクローダー

`clay_3x3_chunk_loader` は Tier 6 の同名 Block/Item と `clayium:chunk_loader` BlockEntity/Menu を使う。NeoForge `TicketController` の所有者は装置の BlockPos で、中心チャンクから X/Z 各±1の ticking ticket 9件だけを取得する。9件の途中で拒否された場合は取得済み分を戻し、`ticket_status=unavailable` を保存してGUIへ稼働不可を表示する。

撤去時は9件を明示解放する。次元アンロード時の実行中 ticket は NeoForge の dimension lifecycle に委ね、永続 ticket はサーバー再起動時の `LoadingValidationCallback` で検証する。正しい位置に装置があり、ticking 9件が正確な3×3で、non-ticking ticket がない所有者だけを復元する。不正な範囲、不正な種別、装置がない所有者、Entity所有 ticket は孤児として削除する。GameTest は `loadsExactlyNineTickingChunks` と `ticketsReleaseAndRestartValidationRemovesOrphans`、外観 E2E は `tests/e2e/chunk-loader.mjs`。GameTest は level の live ticket を解放して次元アンロードを模擬し、保存済み ticket だけから再取得することで再起動時の検証を行う。実際の次元アンロードでは NeoForge がその ServerLevel の live ticket を破棄する。NeoForge 21.1 の TicketController は上限での拒否を返さず、false は同一所有者の重複 ticket を示すため、拒否表示は E2E で保存状態を用いて確認する。

## M30 マーカーと範囲装置

`clay_marker`、`clay_marker_open_pit`、`clay_marker_ground_leveling`、`clay_marker_prism` は共通の `clayium:area_marker` BlockEntity を使う。通常は選択したYを維持し、露天掘は下限、整地は上限、角柱は上下限を対象 Level の `getMinBuildHeight()` と `getMaxBuildHeight()-1` まで広げる。選択範囲は両端を含み、x→z→yの順次カーソルで走査する。マーカー保存NBTは `second_corner` と `owner`。同じ次元で同種・同一所有者の2点を順に右クリックすると対角を選ぶ。各軸64ブロック以内とし、64は原作 `TileClayMarker.maxRange` に由来する。最後に選んだマーカーを範囲装置へ右クリックで適用すると選択記憶を消去し、選択が無ければ装置のGUIを開く。

`area_item_collector` Tier 7、`area_miner` Tier 8、`advanced_area_miner` Tier 9、`area_replacer` Tier 10、`area_activator` Tier 8 は共通の `clayium:area_machine` BlockEntity とMenuを使う。スロット0は工具・使用品・置換材、1はM10の統合 ItemFilter、2～10は出力、11はCE燃料。M27の `BlockOperationService` で権限、保護イベント、LootTable、幸運・シルクタッチ、右クリックを処理し、未ロードチャンクや記録次元外を操作しない。保存NBTは `inventory`、`energy`、`progress`、`area_min`、`area_max`、`cursor`、`target_dimension`、`owner`、`status`。未ロード、保護、出力満杯、置換材不足では現在座標を維持し、状態を `UNLOADED`、`PROTECTED`、`OUTPUT_FULL`、`NO_MATERIAL` とする。所有者・次元・CE不足は `NO_OWNER`、`WRONG_DIMENSION`、`NO_ENERGY`。正常な再試行で状態を解除する。

原作 hotfix2 JAR の `TileAreaMiner` は進捗100/tick、400/採掘作業、内部CE1000/tick、`TileAreaCollector` は進捗100/tick、10/収集作業、内部CE100/tick。Tier 8と9の採掘機でこの基本値は同じである。両クラスのレーザー倍率は `1+4*log10(1+floor(laserEnergy)/1000)`、CEと進捗の双方に乗じる。5機種ともM29の `LaserReceiver` Capabilityを受け、レーザーは次tickに適用する。マーカーと装置の BlockEntityRenderer は選択枠を描画し、原作PNGを参照する。GameTest は `markerHeightExpansionUsesWorldBoundsAndNeverScansOutside` と `areaMachinesPreserveCountsProtectionTierSpeedAndLaserAcceleration`、E2E は `tests/e2e/area-machines.mjs`。
## M33 粘土高炉・粘土反応炉

`clay_blast_furnace` と `clay_reactor` は制御 Block/Item、`clayium:multiblock_controller` は共通 BlockEntity。Tier 7～9 の `clay_steel_machine_hull`、`clayium_machine_hull`、`ultimate_machine_hull` は `AssemblyMachineRegistration.MACHINE_HULLS` に置く。Tier 3～6、10 の既存筐体も同じ登録表から参照する。

構造は制御ブロックを前面中央に置いた中空3×3×3。中心は空気、背面中央は同期済みアイテムインターフェース、上面中央は同期済み赤石インターフェース、反応炉のみ下面中央は同期済みレーザーインターフェースとする。ほかの外殻は筐体。高炉は各部材 Tier 6 以上、反応炉は Tier 7 以上とし、構造 Tier は全構成部材の最低 Tier。ブロック変更とインターフェース同期変更時に再判定し、結果をキャッシュする。制御ブロックの `FORMED` BlockState と BlockEntity NBT の `formed`、`structure_tier` を保存・同期する。構造破損時には予約入力を戻して処理を止める。

原作 hotfix2 JAR の `mods.clayium.util.crafting.CRecipes.recipeBlastFurnace` にある合金レシピは次の3件。CEは内部値で、括弧内の `e(値,Tier)` は原作 `CRecipes.e` と同値。

| ID (`blast_furnace/` 以下) | 入力 | 出力 | Tier | CE | tick |
|---|---|---|---:|---:|---:|
| `clay_steel_impure_manganese` | 工業用粘土粉2、不純マンガン粉1 | 粘土鋼インゴット2 | 6 | 50,000 (`e(5,6)`) | 200 |
| `clay_steel_advanced` | 発展工業用粘土粉1、不純マンガン粉1 | 粘土鋼インゴット1 | 7 | 500,000 (`e(5,7)`) | 5 |
| `clay_steel_manganese` | 発展工業用粘土粉1、マンガン粉1 | 粘土鋼インゴット1 | 8 | 5,000,000 (`e(5,8)`) | 1 |

粉→インゴットの高温処理も同じ原作 `recipeBlastFurnace` から生成する。カルシウム・カリウムは Tier 5 / 1,000 CE内部値 (`e(1,5)`) / 500 tick、ベリリウム・ハフニウム・粘土鋼・鋼は Tier 6 / 10,000 CE内部値 (`e(1,6)`) / 500 tick、マンガン・ストロンチウム・バリウム・クレイウムは Tier 7 / 200,000 CE内部値 (`e(2,7)`) / 1,000 tick、チタン・究極合金は Tier 8 / 4,000,000 CE内部値 (`e(4,8)`) / 2,000 tick、クロムは Tier 9 / 40,000,000 CE内部値 (`e(4,9)`) / 2,000 tick。ID は `blast_furnace/<素材名>`。鉄インゴット1と石炭2→鋼インゴット1は `blast_furnace/steel_from_iron`、Tier 6 / 10,000 CE内部値 (`e(1,6)`) / 500 tick。外部 Mod 固有の原作レシピと既定無効の HardcoreOsmium 分岐は初期登録から除外する。

`machine_hull/tier_7..9` は原作 `CRecipes` の大板、回路または Clay Core/Brain、Clay Energy Excitor の格子で生成する。`multiblock/clay_blast_furnace` は必要組立機 Tier 4、120 tick、`e(1,6)`。`multiblock/clay_reactor` は必要組立機 Tier 6、1,200 tick、`e(1,7)`。反応炉のレーザー加速は原作 `mods.clayium.block.tile.TileClayReactor.proceedCraft` に従い、照射された `ClayLaser.getEnergy()` を `long` へ切り捨て、その tick の通常進捗1に加算する。任意の倍率上限を設けない。

GameTest は `blastFurnaceRejectsMissingLowTierAndMisplacedInterfaces`、`blastFurnaceProcessesOnlyThroughItsSynchronizedInterface`、`blastFurnaceStartsAdvancedClaySteelRecipe`、`blastFurnaceStartsOriginalDustSmeltingRecipe`、`everyShellChangeInvalidatesTheCachedStructure`、`reactorLaserAcceleratesAndStructureDamageStopsProcessing`。外観 E2E は `tests/e2e/clay-multiblocks.mjs`。

## M34 CA機械・共鳴と拡張

`CaRegistration` は `ca_condenser_tier_{9,10,11}`、`ca_injector_tier_{9,10,11,12,13}` を同名 Block/Item と共通 `processing_machine` BlockEntity/Menu で登録する。`ca_resonating_collector` は Tier 10、専用 BlockEntity で、出力9スロットと保存 NBT `inventory`、`progress`、`progress_remainder` を持つ。共鳴が1以下では停止し、出力満杯時には進捗を消費しない。

拡張 Block/Item は素材名 `antimatter`、`pure_antimatter`、`oec`、`opa` に `_resonator`、`_overclocker`、`_energy_storage_upgrade` を付けた12 ID。共鳴倍率は順に1.08/1.10/2.0/20.0、中心を含む各軸±2の5×5×5で積を取る。オーバークロッカーは6面隣接だけを対象に1.5/2.3/3.5/5.0を乗算する。一般機械の積をF、GをF>10ならF/10、それ以外は1として、時間は基本値/G、CEは基本値×G^1.5、進捗はF/G。FE変換器は専用指数1.0で線形に計算する。ストレージ拡張は6面隣接で燃料枠に+1/+3/+7/+63、合計上限64。撤去後の余剰はアイテムとして落とす。

CA凝縮出力は基礎個数×(1+ln(共鳴倍率))の整数切り捨て。CA注入の共鳴進捗指数はTier 9が0.2、10が0.9、11が3.0、12～13が1.0。開始時の倍率と出力は処理完了まで保持する。レシピ ID は `ca_condenser/antimatter`、`ca_injector/{antimatter_resonator,resonating_collector,pure_antimatter_resonator,oec_resonator,opa_resonator}` と各素材の `_energy_storage_upgrade`、`reactor/{antimatter,pure_antimatter,oec,opa}_overclocker`。原作反応炉の後者4件の時間は順に10^13、10^14、10^15、10^15 tick で、レシピの `processing_ticks` は long 値として保存する。

GameTest は `allEightCaMachinesPreserveOriginalTiersRecipesAndResonance`、`resonatingCollectorStopsWithoutResonanceAndNeverConsumesWhenFull`、`overclockAndResonanceUseOriginalProductsAndBoundaries`、`activeCaRecipeKeepsItsStartValuesWhenAugmentsChangeAcrossReload`、`energyStorageUpgradesCapAtSixtyFourAndDropOnlyTheExcess`。外観 E2E は `tests/e2e/ca-machines.mjs`。
## M31 高度な粘土工具

`clay_pickaxe`、`clay_shovel`、`clay_steel_pickaxe`、`clay_steel_shovel` は原作の `item.itemClayPickaxe.name`、`item.itemClayShovel.name`、`item.itemClaySteelPickaxe.name`、`item.itemClaySteelShovel.name` に対応する。耐久は粘土工具500、粘土鋼工具10,000。粘土ツルハシの粘土鉱石速度は32、粘土シャベルの粘土鉱石速度は12、粘土・テラコッタ速度は32。粘土鋼工具の有効ブロック速度は基礎速度の6倍。通常粘土鉱石のドロップ補正はツルハシ4倍、シャベル3倍の幸運係数を使い、シルクタッチでは補正しない。

粘土鋼工具の操作モードは `clayium:advanced_tool_mode`、記憶形状は `clayium:advanced_tool_pattern` Data Component に保存する。モード0は単体、1～最大値未満は `(2m+1)×(2m+1)` の面内採掘、最大値は記憶形状。サーバー設定 `ClaySteelPickaxeRange` は初期2、範囲0～64で、形状登録時の半径にも使う。粘土ブロックへの使用で周囲の粘土形状を記録する。追加採掘は M27 の `BlockOperationService` を通し、有効な工具対象、保護、権限とドロップを確認する。

GameTest は `threeByThreeMiningStaysInPlaneAndRespectsBreakProtection`、`rememberedClayShapeRotatesMinesAndSurvivesItemSerialization`、`fourClayToolsKeepOriginalDurabilitySpeedOreHarvestAndConfig`。外観 E2E は `tests/e2e/advanced-clay-tools.mjs`。

## M35 巨大CA反応炉

`CaReactorRegistration` は `ca_reactor_core_mk_{i,ii,iii,iv}` と `ca_reactor_coil_mk_{i,ii,iii,iv}` を Tier 10～13、`ca_reactor_hull_rank_{1..10}` を同名 Block/Item として登録する。原作キーはコアが `tile.block{Antimatter,PureAntimatter,OEC,OPA}CAReactorCore.name`、コイルが `tile.blockCAReactorCoil.{antimatter,pureantimatter,oec,opa}.name`、筐体が `tile.blockCAReactorHull.{0..9}.name`。筐体アイテムの Tier はランク1～4が10～13、ランク5～10は13。4コアは `clayium:ca_reactor_core` BlockEntity と共通加工機 Menu を使う。

コアに直接接するコイルから中空リングの連結成分を探索する。コイルは最低8個・最大128個、各コイルの隣接コイルは2個、非コイル面は筐体、同期済みアイテム・レーザーインターフェース、または唯一のコアで覆う。別のコアが同じリングに接すると無効。筐体は50個以上、コイルTierはコアと同じ、インターフェースTierはコア以上、筐体ランクはコアTier−3以下。インターフェースの同期先はコアの次元・座標と一致させる。変更通知時に構造キャッシュを失効させ、毎tickは探索しない。コアから各軸8ブロック以内の変更と、探索済み部材に接する変更を監視する。

筐体数N・筐体平均ランクRから出力倍率 `0.2*1.02^N*7.5^R`、CE倍率 `1.01^(N*R)`、圧縮ランク `min(int(R),8)`、時間倍率 `9^R` を求める。tick・内部CE・出力個数は切り捨て、正の出力が1未満なら1個とする。出力はスタック上限64個で分割し、出力枠が埋まる場合は開始しない。非有限・負値・long超過時は安全停止する。圧縮はレシピ出力が純反物質の場合だけ適用する。`ca_reactor/pure_antimatter` は反物質1→純反物質1、Tier10、内部CE100,000,000、20tick。構造表示用の `reactor_formed`、`reactor_coils`、`reactor_hulls`、`reactor_average_rank` はクライアントへの更新タグのみで送る。`CAReactorGlittering` は初期true。

GameTest は `reactorAcceptsOnlyCoveredTierMatchedRingsAndInvalidatesItsCache` と `reactorUsesHotfixTwoFormulaCompressionEnergyTimeAndSafeStops`。外観 E2E は `tests/e2e/ca-reactor.mjs`。

## M36 PAN

`pan_core` と `pan_cable` は Tier 11。`pan_adapter_tier_10`～`13` と `pan_duplicator_tier_4`～`13` は原作台帳に対応する。コアと複製機は共通の `clayium:pan` BlockEntity と Menu を使い、複製機のスロットは見本0、反物質1、出力2、CE燃料3。保存NBTは `inventory`、`energy`、`progress`、`active_consumption`。後者は再読み込み後の閉包変更時だけ進捗を破棄するために使う。

探索は深度1000、最大10000ノード、1 tick 256ノード、200 tickごとの再走査とし、設置・撤去・レシピ再読込で失効する。レシピ収集と閉包反復も1 tick 256件まで。原作 hotfix2 JAR の `mods.clayium.block.tile.TilePANCore` は粘土ブロック・丸石・原木を原価1、圧縮粘土を順に10～10^13、塩を `TileCobblestoneGenerator.progressMax * TileSaltExtractor.energyPerWork / 6 = 500`、反物質を100000、金属分離の不純粉を `(TileChemicalMetalSeparator.baseConsumingEnergy * baseCraftTime + 1000) * 488 / weight` とする。`PANACFactoryCraftingTable` はクラフト費用10を加える。循環・零費用だけでは候補を増やさず、未知の Data Component 付きスタックを禁止する。

原作 `mods.clayium.block.tile.TilePANDuplicator.proceedCraft` は複製 CE を `100000 * multConsumingEnergy` 内部値ずつ毎 tick 消費して進む。倍率は `UtilTier.setTierManagers` の一般機械値で、Tier 4～13 は順に1、5、25、1、1、1、250、1、1、1。必要 tick は `ceil(consumption / (100000 * 倍率))`、最低1。反物質1個を消費し見本を保持する。GameTest は `networkTraversalIsIterativeBoundedAndInvalidated`、`realNetworkScanStopsAtDepthAndNodeLimits`、`recipeClosureRejectsCyclesFreeRecipesAndUnknownComponents`、`connectedAdapterRebuildsClosureAfterRecipeReload`、`duplicatorConsumesOneAntimatterAndExactCoreEnergyWhileKeepingSample`、`allPanTiersDuplicateAtOriginalCeRates`。外観 E2E は `tests/e2e/pan-machines.mjs`。

## M37 レシピ閲覧・標準他Mod互換

- JEI API は `compileOnly`、開発確認用本体は `localRuntime`、EMI API は `compileOnly` とする。両Modは `neoforge.mods.toml` のクライアント任意依存であり、プラグインクラスを通常のClayiumロード経路から参照しない。
- M03の全 `ClayiumRecipeMethod` にJEI/EMIカテゴリを用意し、クライアントの実際の `RecipeManager` からレシピを登録する。必要Tier、材料、生成物、TierManager適用後の処理tickと合計CE、直接流体入出力の正確なmB量を表示する。粘土作業台、CA反応炉、PAN複製も閲覧対象にする。
- 他Modとのアイテム・流体・FE接続は `Capabilities.ItemHandler.BLOCK`、`Capabilities.FluidHandler.BLOCK`、`Capabilities.EnergyStorage.BLOCK` だけを使用する。FE入力はCE→FE変換器と通常機械のいずれも受け付けず、既存の `c:`、`minecraft:`、`clayium:` タグを正本とする。
- GameTest は `itemFluidAndFeTransferOnlyThroughNeoForgeCapabilities`、`commonMinecraftAndClayiumTagsResolveToRegisteredItems`、`recipeViewerDataMatchesReloadedDatapackRecipes`。閲覧Mod未導入で全GameTestが起動することも確認する。JEI画面のE2Eは `tests/e2e/recipe-viewers.mjs`。

## M38 未登録素材・設置ブロック・初期フライス盤

- `MaterialRegistration` は原作 `blockMetal` の57種を `<material>_block`、`blockSiliconeColored` の16色を `<color>_silicone_block` として同名Block/Itemに登録する。金属は `metalblock_base/dark/light.png` の3層を素材色でtintし、シリコーンは `siliconecolored.png` を登録IDに対応する固定 `DyeColor` でtintする。全ブロックはツルハシ採掘、硬度・爆発耐性2.0、自身ドロップ。
- シャードは `compressed_clay_shard`、`industrial_clay_shard`、`advanced_industrial_clay_shard`。GrinderレシピIDは `grinder/<ore>_to_<shard>` で石・深層岩の6鉱石を扱い、出力2/3/5、内部CE 1、3/6/9 tick。Condenserは `condenser/{compressed,industrial,advanced_industrial}_clay_shard`、入力4、内部CE 1、3/6/9 tick。
- 追加筐体は `dense_clay_machine_hull` Tier 2、`pure_antimatter_machine_hull` Tier 11、`oec_machine_hull` Tier 12、`opa_machine_hull` Tier 13、`az91_d_alloy_hull` と `zk60_a_alloy_hull` Tier 6。標準筐体は `MACHINE_HULLS`、全筐体は `MACHINE_HULL_DEFINITIONS` を正本とする。レシピIDは `machine_hull/tier_{2,11,12,13}` と `machine_hull/{az91_d_alloy_hull,zk60_a_alloy_hull}`。
- `elemental_milling_machine` は通常の26成形機とは別のTier 1機種で、共通 `processing_machine` BlockEntity/Menuと原作の `milling_machine` RecipeTypeを使う。本体レシピは `forming_machine/elemental_milling_machine`、高密度粘土板1→粘土回路基板1の専用経路は `elemental_milling_machine/clay_circuit_board`、内部CE 1、32 tick。
- 金属は `material_block/<id>` でインゴット9→ブロック1、`material_ingot/<material>` でブロック1→インゴット9。染色は `silicone/<color>` でシリコーンブロック8と `c:dyes/<color>` 1→固定色ブロック8。
- GameTest は `allEightyThreeRestoredVarietiesHaveRegistriesDataAndNames`、`oreShardsAndCondensationPreserveOriginalBalances`、`metalAndSiliconeRecipesPreserveMaterialWithoutGrowth`、`elementalMillingMachineExecutesItsOriginalFirstCircuitRecipe`、`allOriginalPngHashesAndTintLayersRemainUnmodified`。外観E2Eは `tests/e2e/material-blocks.mjs`。

## M39 生存製作経路

生存製作台帳の正本は `data/clayium/survival_recipe_ledger.tsv`。1行を1個の名前付き Registry ID とし、`recipe_ids`、`method`、`required_tier`、`processing_ticks`、`clay_energy`、`inputs`、`outputs`、`original_source` を記録する。同じ品目の複数経路は各列を `|` で対応付ける。M39で追加した経路を `restored`、以前から有効な経路を `runtime_existing`、原作の創造専用品を `intentional_exclusion`、カプセル廃止・直接流体・B/W統合を `explicit_change` と分類する。

製作レシピは `ClayiumDataGenerators` に登録した `ProgressionRecipeProvider` が `datagen/progression/` のドメイン別生成クラスを呼び、`progression/` 以下へ生成する。材料・数量・配置・加工方法・Tier・tick・CE は hotfix2 `CRecipes` の各登録呼出しに従う。原作の作業台・組立機・CA注入機・反応炉の区別を保ち、既存Providerの経路を重複生成しない。金属チェスト57種は素材インゴット8個と `Tags.Items.CHESTS_WOODEN`、または素材インゴット8個と `clayium:metal_chests` の中央素材で作り、内容物は引き継がない。

GameTest は `allOneHundredTwentyThreeCandidateMachinesHaveClassifiedRuntimePaths`、`survivalDependencyGraphReachesNonTierCaPanAndContainersWithoutTierLeap`、`fiftySevenMetalChestsHaveBothOriginalCraftingRoutesAndEquipmentIsReachable`、`reloadedViewerDataMatchesMachineAcceptanceAndExecution`。外観E2Eは `tests/e2e/survival-recipes.mjs`。

## M40 原作固有動作

- 原作 hotfix2 の `ItemCraftingTools` は粘土麺棒・スライサー・ヘラをスタック1、耐久60/60/36とする。クラフト残余は耐久を1増やした同じ工具で、破損する回だけ粘土玉4/3/2個へ置き換える。通常クラフトと粘土作業台は同じ残余規則を使い、工具と粘土玉を同時に返さない。
- `TileAreaMiner` のフィルター順は通常採掘対象、幸運、シルクタッチの3スロット。幸運はレベル3で、同じ対象が幸運とシルクタッチの両方に一致するとシルクタッチが上書きする。フィルタースロットは外部 Item Capability に公開しない。範囲代行機の対象はブロック・Entity・両方、視線判定とスニークは独立設定で、`RayTraceMemory` とともに保存する。
- `ClayLaserManager` の照射距離は描画長32、探索距離1～31、透過対象は空気とガラス。照射履歴は10件で、対象ブロック・状態または距離が変わると履歴と累積を消去する。岩石破壊閾値は `floor(hardness+1)*100`。石炭・鉄・金・ダイヤ・レッドストーン・ラピス・エメラルド鉱石は対応する素材ブロックへ変換し、深層岩版も同じ結果とする。苗木は累積300～999で段階を1つ進め、300,000以上で粘土樹苗木へ変換する。既存 `LaserReceiver` を先に処理し、受理されなかったブロックだけ一般照射を行う。破壊・変換は FakePlayer の保護イベントを通す。
- Quartz Crucible は不純シリコンを最大8個保持する。右クリックと開口部へ落下した `ItemEntity` の双方から空き分だけ取り込み、加熱完了後は糸1個だけを消費する。同じEntityを二重に消費しない。
- `item_filter_duplicator` は原作 `filterduplicator.png` を使うTier 7品。主手に複写器、副手に統合フィルターを持って使用すると、最初は設定を複写器へ保存し、次の使用で副手のフィルターへ適用する。`clayium:item_filter` は両アイテムの設定、`clayium:filter_copy` は複写器だけのCopy状態を保存する。条件上限は100、入れ子深度は16で、祖先と同じ設定へ戻る循環を拒否する。Copy状態はスニーク使用で解除し、コピー後のフィルター編集はコピー元へ波及しない。
- レーザー本体と反射器の保存NBT `irradiation` は `total`、`history`、`target`、`target_block`、`target_distance` を含む。範囲装置の保存NBTは `activator_target`、`activator_ray_trace`、`activator_sneaking`、`ray_trace_memory` を含む。`clayium:area_machine_control` Payload の action 0 は対象モード、1 は視線判定、2 はスニーク、3 は視線記憶を変更する。

GameTest は `craftingToolsReturnDamageAndRawClayExactlyOnce`、`areaFiltersAndActivatorPreservePriorityProtectionAndInteraction`、`laserTransmitsConvertsAccumulatesAndHonorsProtection`、`crucibleAndFilterCopiesConsumeValidateAndPersistExactly`。外観 E2E は `tests/e2e/original-behaviors.mjs`。

## M42 ワールド描画

- PANケーブルだけが `down/up/north/south/west/east` の6接続BlockStateを持ち、設置時と隣接Block更新時にPANブロックへの接続を計算する。コア・アダプター・複製機には接続プロパティを持たせない。インターフェースの `linked` は同期・切断時に変更し、範囲機械の `active` は所有者・次元・CEを満たして稼働可能な間維持する。
- `QuartzCrucibleRenderer` は中身の量・加熱段階をUV切り取りで表示し、描画距離64・描画領域1ブロック。`StorageContainerRenderer` は品目と保持数を描画距離64・描画領域1ブロックで表示する。`MetalChestRenderer` はサーバーの開閉イベントによる蓋アニメーションを描画距離64・描画領域1ブロックで表示する。`CaReactorCoreRenderer` は構造成立時の停止alpha 96、稼働時alpha 180±55の光彩を描画距離96・描画領域1ブロック付近で表示する。
- BER用の更新タグはるつぼの `impure_silicon`・`heating_ticks`、ストレージの `stored`・`amount`・`expanded`、CAコアの `state`・`reactor_formed`・`reactor_coils`・`reactor_hulls`・`reactor_average_rank` を同期する。金属チェストの蓋は保存NBTではなくサーバーのブロックイベントで同期する。
- GameTestは `panCableConnectionsRejectStoneAndRebuild`、`rendererDataSurvivesSaveReloadAndUpdateTags`、`visualConnectionStatesAreServerAuthoritative` と既存の `allOriginalPngHashesAndTintLayersRemainUnmodified` を使う。外観E2Eは `tests/e2e/world-rendering.mjs`、原作描画対応表は `tests/e2e/render-parity.json`。
