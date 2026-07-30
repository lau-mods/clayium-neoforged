# AGENTS.md

## 1. プロジェクトの目的

本プロジェクトは、本家Minecraft Mod「Clayium」を、Minecraft 1.21.1およびNeoForge向けに再実装するプロジェクトである。

目的は旧版コードを機械的に変換することではない。Clayium固有のゲームデザインを維持しながら、Minecraft 1.21.1、NeoForge、および近年のModded Minecraft環境に適合する構造へ再設計する。

維持すべき中核要素は以下である。

* 粘土を中心とした工業化進行
* 圧縮粘土およびエネルギー粘土の段階的発展
* Clay Energy（CE）
* Clayium固有のTier進行
* 機械加工による部品製造
* レーザー、Clay Reactor、Clay Fabricator
* 反物質、Pure Antimatter、OEC、OPA
* PANを含む最終段階のコンテンツ
* Clayium固有の視覚的・機械的アイデンティティ

旧実装の技術的制約や不自然な操作体系は、そのまま再現しない。

---

## 2. 対象環境

* Minecraft: 1.21.1
* Mod loader: NeoForge
* Mod ID: `clayium`
* ライセンス: CC BY 4.0
* テクスチャ: 本家Clayiumのテクスチャを使用
* レシピ閲覧連携: JEIを初期実装対象とする

Java、Gradle、NeoForge、JEIなどの具体的なバージョンは、リポジトリ内で固定されたツールチェーンに従うこと。エージェントの判断で依存バージョンを無断更新してはならない。

---

## 3. 参照元に関する制約

実装上の情報源は、原則として以下に限定する。

1. 本家Clayiumのソースコード
2. 本家Clayiumのテクスチャ、モデル、言語ファイルおよびドキュメント
3. Minecraft 1.21.1の挙動
4. NeoForgeの公式ドキュメントおよび公式API
5. JEIの公式API、公式ドキュメントおよび公開ソース
6. 本リポジトリ内の設計文書
7. 現行Minecraft Mod環境の一般的な相互運用方式

本プロジェクトは、本家Clayiumと現行APIを基準とする独立した移植・再実装として扱うこと。

---

## 4. ライセンスと帰属表示

本プロジェクトはCC BY 4.0を継承する。

ソースコード、改変したアセット、データファイル、ドキュメントには、適切な範囲で以下を明示する。

* 原作品の名称
* 原作者
* 原作品の配布元
* CC BY 4.0で提供されていること
* 本プロジェクトで変更を加えていること
* CC BY 4.0ライセンスへの参照

リポジトリには最低限、以下を含めること。

```text
LICENSE
CREDITS.md
THIRD_PARTY_NOTICES.md
```

ソースファイルには、プロジェクトの方針に従って次のSPDX識別子を使用する。

```text
SPDX-License-Identifier: CC-BY-4.0
```

---

## 5. テクスチャおよびアセット方針

### 5.1 基本方針

ゲーム内テクスチャには、本家Clayiumのものを使用する。

次の目的で必要な変更は認める。

* Minecraft 1.21.1のリソース構造への移行
* Block ModelまたはItem Modelの変更への対応
* テクスチャアトラス上の配置変更
* 不足している面、状態、オーバーレイの補完
* 透過、色空間、ファイル形式などの技術的修正
* 新しい機械状態や方向表現への対応

変更した場合は、本家テクスチャを基礎とした改変であることを帰属表示に記録する。

### 5.2 禁止事項

* 本家テクスチャを理由なく描き直さない
* 一般的な工業Mod風の外観へ均質化しない
* 元のピクセルアートを不必要に高解像度化しない
* 色、意匠、Tier識別を独断で全面変更しない

テクスチャが未準備であることを理由に、恒久的な仮テクスチャをコミットしてはならない。開発用アセットが必要な場合は、配布対象から除外された開発用リソースとして明確に分離すること。

---

## 6. 移植の基本原則

### 6.1 再現対象

以下は原則として維持する。

* コンテンツの役割
* ゲーム内での進行上の位置
* 素材間の関係
* CEを中心とする内部経済
* Tierによる機械性能とレシピ制限
* 粘土圧縮の指数的進行
* Clayium固有の最終到達点
* 本家の視覚表現と名称

### 6.2 再設計対象

以下は現行環境に合わせて再設計する。

* Block Entity
* レジストリ
* レシピシステム
* ネットワーク同期
* ItemStack上の状態保存
* 液体輸送
* アイテム輸送
* フィルター
* ワールド生成
* GUI
* レシピ表示
* 他Modとの相互運用
* ワールド操作機械の安全性
* サーバー負荷管理

「本家に存在した」という理由だけで、旧Minecraftの制約に起因する仕様を維持してはならない。

---

## 7. アーキテクチャ原則

### 7.1 サーバー主導

ゲームロジックはサーバー側を正とする。

クライアント側は以下に限定する。

* 描画
* Screen
* 入力送信
* 表示用キャッシュ
* パーティクル
* サウンド開始要求の受信

クライアント側の値を加工結果、エネルギー消費、アイテム生成、機械状態判定の根拠にしてはならない。

### 7.2 コンポジション優先

機械実装は巨大な継承階層を避け、機能単位のコンポーネントとして構成する。

推奨される構成概念は以下である。

```text
MachineBlock
MachineBlockEntity
MachineDefinition
MachineTier
RecipeProcessor
ClayEnergyStorage
ItemPort
FluidPort
SideConfiguration
RedstoneControl
UpgradeContainer
MachineRuntimeState
```

機械ごとの差異は、可能な限り`MachineDefinition`、Recipe Type、構成コンポーネントおよびデータで表現する。

一つの基底クラスに全機械の特殊処理を条件分岐として追加してはならない。

### 7.3 静的で安定したID

Block、Item、Block Entity、Menu、Recipe Typeなどは、意味のある安定した`ResourceLocation`を持たせる。

例:

```text
clayium:clay_bending_machine
clayium:dense_clay_bending_machine
clayium:precision_grinder
clayium:clay_reactor
```

Tierや機械種別を数値メタデータだけで区別してはならない。

数値IDを永続化形式として使用してはならない。

### 7.4 Tierの保存

Tierは整数だけで保存せず、安定した名前付きIDとして扱う。

整数値は以下にのみ使用する。

* 性能比較
* レシピ要求Tierとの比較
* UI表示順
* 数式上の係数

永続データや外部データ形式では、可能な限り名前付きIDを使用する。

---

## 8. Clay Energy

CEはClayium固有のエネルギー体系として維持する。

すべてのClayium機械をFEで直接動作させてはならない。

### 8.1 原則

* 通常のClayium機械はCEを消費する
* CEはClayium内部APIで管理する
* FEとの変換は専用コンバーターだけが担当する
* CEとFEを同一ストレージとして扱わない
* 他ModのFE発電だけでClayium進行を迂回できる構造にしない
* CEの生成、圧縮、消費効率は進行バランスの主要要素として扱う

### 8.2 FE連携

CE-FEコンバーターは、NeoForgeの標準エネルギーCapabilityを公開する。

変換率は設定可能としてよいが、以下を保証すること。

* デフォルト値でゲームバランスが成立する
* 入出力方向が明確である
* 一tick当たりの変換上限を持つ
* オーバーフローを起こさない

---

## 9. レシピシステム

### 9.1 データ駆動

機械レシピはdatapackベースで実装する。

各機械レシピは、適切な以下の要素を持つ。

* Recipe Type
* Recipe Serializer
* CodecまたはMapCodec
* StreamCodec
* Recipe Input
* 入力アイテム
* 入力液体
* 出力アイテム
* 出力液体
* 確率出力
* 処理時間
* CE/t
* 最低Tier
* 必要な特殊条件

レシピをJavaまたはKotlinコードに大量にハードコードしてはならない。

### 9.2 共通レシピモデル

単純な加工機は、共通の機械レシピモデルを使用する。

特殊処理が必要な場合のみ専用Recipe型を追加する。

専用Recipe型の追加理由として認められる例:

* レーザー強度によって処理結果が変化する
* 多ブロック構造のランクが出力に影響する
* 周囲環境を入力条件として扱う
* 重み付き複数出力を持つ
* PAN固有の生成規則を持つ

単に入力スロット数や出力スロット数が異なるだけで、別の実行エンジンを作成してはならない。

### 9.3 リロード

datapack reload後に、既存機械が安全にレシピを再解決すること。

リロード前のRecipeインスタンスを永続的に保持してはならない。Recipe IDまたは再検索可能な状態を保存する。

---

## 10. JEI対応

JEI対応は後付け機能ではない。レシピ基盤と同時期に実装する。

### 10.1 実装時期

以下の順序を厳守する。

1. 共通機械レシピ型を実装する
2. 最初のRecipe Serializerを実装する
3. JEIプラグインの基盤を実装する
4. 最初の機械レシピカテゴリを表示する
5. その後に大量の機械およびレシピを追加する

全機械を実装してからJEI対応を開始してはならない。

### 10.2 新規Recipe Typeの完了条件

新しい独自Recipe Typeは、以下を満たすまで完成扱いにしない。

* JEIカテゴリが存在する
* Recipe Catalystが登録されている
* 入力と出力が正しく表示される
* CE/tが表示される
* 処理時間が表示される
* 必要Tierが表示される
* 確率出力が明示される
* 液体量が表示される
* 特殊条件が説明される
* JEIから関連機械を確認できる
* サーバー上の実際のレシピ判定とJEI表示が一致する

### 10.3 実装分離

JEI連携コードは以下から分離する。

* Recipe Type登録
* 機械登録
* レシピ実行ロジック
* 共通API
* サーバー専用コード

JEIが存在しない環境でも、Clayium本体が正常にロードされなければならない。

### 10.4 表示内容

JEIでは最低限、以下を表示する。

```text
処理時間
CE/t
合計CE
必要Tier
アイテム入出力
液体入出力
確率出力
環境条件
レーザー条件
多ブロック条件
```

複雑なレシピをツールチップだけで説明してはならない。視覚的に表現できる条件はレイアウト内へ表示する。

---

## 11. Item、Fluid、Energyの相互運用

### 11.1 アイテム

機械の外部アイテム入出力にはNeoForgeの標準アイテムCapabilityを使用する。

以下を保証する。

* 側面別の搬入・搬出
* 挿入可能スロットの制限
* 抽出可能スロットの制限
* シミュレーション処理の正確性
* 不正なスタック数の防止
* 自動搬入とGUI操作の競合防止
* 外部パイプとの相互運用

内部インベントリ実装を外部Modへ直接公開してはならない。

### 11.2 液体

液体は標準のFluid Capabilityを使用する。

液体ごとの専用粘土カプセルは実装しない。

Clayium独自の携帯容器を実装する場合は、一種類または少数の汎用容器として実装する。

要件:

* 他Modのタンク、パイプ、セル、バケツと相互運用する
* 液体量は標準単位で管理する
* 入力専用、出力専用、両用タンクを区別する
* 混合禁止条件を明確にする
* レシピ処理開始前に全出力容量を検証する
* 処理途中の液体消失を防止する

### 11.3 Capabilityキャッシュ

隣接Capabilityへ頻繁にアクセスする処理ではキャッシュを使用する。

以下の場合に正しく無効化すること。

* 隣接Blockの変更
* Block Entityの削除
* チャンクのアンロード
* Capability提供条件の変更
* 側面設定の変更

毎tick無条件で周囲六面を再探索してはならない。

---

## 12. Item状態とData Component

ItemStack上の構造化された状態はData Componentを使用する。

対象例:

* フィルター設定
* メモリーカード
* 接続先座標
* 選択中の機械モード
* 採掘範囲
* I/O設定
* 携帯液体量
* 携帯CE量
* レーザー設定
* 工具の特殊モード

非構造化NBTへ独自データを無制限に追加してはならない。

Componentには以下を定義する。

* Codec
* StreamCodec
* デフォルト値
* 不変条件
* コピー時の挙動
* スタック可能性への影響
* Tooltip表示方針

---

## 13. Block Entity

Block Entityは、自身の状態だけを保存する。

保存対象例:

* インベントリ
* タンク
* CE
* 進捗
* 稼働状態
* Redstone制御
* 側面設定
* Upgrade
* 所有者
* 多ブロック接続情報の再構築に必要な最小状態

保存してはならないもの:

* Recipeオブジェクトそのもの
* Levelへの参照
* 他Block Entityへの直接参照
* 再計算できる巨大なキャッシュ
* クライアント表示専用状態
* 一時的な検索結果

ロード時に不正値を検証し、範囲外の値を安全な値へ補正する。

---

## 14. ネットワークとGUI

### 14.1 同期方針

毎tick全状態を同期してはならない。

以下のいずれかの場合に限定して同期する。

* 値が変化した
* GUIを開いた
* ユーザー操作が発生した
* 表示上必要な間隔に達した
* Block Entityの描画状態が変化した

同期対象は必要最小限とする。

### 14.2 クライアントからの操作

クライアントから送られた値は必ずサーバー側で検証する。

検証項目:

* プレイヤーとの距離
* Menuの有効性
* Block Entityの存在
* 権限
* 値の範囲
* スロット番号
* モードID
* 対象座標
* ItemStackの所有状態

クライアント指定のItemStack、Fluid量、CE量を無条件に信用してはならない。

### 14.3 GUI設計

同一系統の機械は共通レイアウトを使用する。

TierごとにScreenクラスを複製してはならない。

GUIには最低限、以下を表示する。

* 処理進捗
* CE残量
* CE容量
* CE/t
* 入出力状態
* Redstone制御
* 稼働停止理由
* 必要Tier不足
* 出力詰まり
* 液体不足
* 構造不成立

---

## 15. ワールド生成

ワールド生成はConfigured Feature、Placed Feature、Biome Modifierなどのデータ駆動方式を使用する。

コードから直接鉱石を配置してはならない。

### 15.1 推奨分布

#### Clay Ore

* 通常石層を中心に生成
* 初期進行で入手できる高度
* 河川、湿地、鍾乳洞など、粘土と関連する環境では生成率を調整してよい

#### Dense Clay Ore

* 主にディープスレート層へ生成
* ディープスレート系テクスチャを使用する
* 通常Clay Oreより希少にする

#### Large Dense Clay Ore

* 深層の希少鉱床として生成
* ディープスレート系テクスチャを使用する
* Dense Clay Oreより希少にする

### 15.2 設定可能性

生成量、高度、鉱脈サイズ、Biome条件はdatapackまたはサーバー設定から変更可能にする。

Modpack側がClayiumのワールド生成を無効化しても、レシピ変更によって進行を再構築できる状態を保つこと。

---

## 16. 素材とタグ

一般素材は共通タグを優先する。

例:

```text
c:ingots/copper
c:ingots/tin
c:ingots/iron
c:dusts/silicon
c:plates/steel
```

Clayium固有素材にはClayium名前空間のタグを定義する。

例:

```text
clayium:clays
clayium:energetic_clays
clayium:clay_ores
clayium:machine_hulls
clayium:laser_components
```

レシピ入力では可能な範囲でタグを使用する。

ただし、出力を他Mod製品へ自動的に置換する強制unificationは行わない。Clayiumの機械出力は原則としてClayiumのItemとし、Modpack側がdatapackで変更できる構造にする。

---

## 17. フィルターと設定工具

旧来の細分化されたフィルターItemや設定工具は、現代的な操作体系へ統合する。

### 17.1 Smart Filter

単一のSmart Filterまたは少数のフィルター系Itemへ統合する。

対応候補:

* Item ID一致
* 完全なItemStack一致
* Data Componentを無視した一致
* 特定Data Component一致
* アイテムタグ
* Mod namespace
* 表示名
* 耐久値範囲
* BlockState条件
* AND
* OR
* NOT
* White list
* Black list

鉱石辞書フィルターはItem Tag Filterへ置き換える。

メタデータフィルターはBlockState PropertyまたはData Component条件へ置き換える。

### 17.2 Clay Configurator

搬入設定、搬出設定、回転、外観変更などは、可能な限り共通設定工具へ統合する。

操作は一貫させる。

例:

```text
右クリック: 現在のモードを実行
Shift + 右クリック: モード切替
GUI操作: 詳細設定
```

操作方法はTooltipだけに依存せず、ゲーム内ガイドまたはJEI説明へ記載する。

---

## 18. 実装順序

以下のPhase順を基本とする。

技術的に実装できるかではなく、依存関係と縦方向の進行完成度を基準にする。

### Phase 1: 登録と共通データモデル

* Blocks
* Items
* Block Entity Types
* Menu Types
* Data Components
* Recipe Types
* Recipe Serializers
* ClayTier
* ClayEnergy
* MachineDefinition
* 共通の保存、同期、設定基盤

完了条件:

* 空のNeoForge ModとしてClientとDedicated Serverが起動する
* Datagenが成功する
* 登録IDが安定している
* Client専用クラスがServer側から参照されていない

### Phase 2: 共通Recipe基盤とJEI

* 共通Machine Recipe
* Codec
* StreamCodec
* Recipe Input
* レシピ検索
* レシピ実行のトランザクション設計
* JEI Plugin
* 最初のJEI Recipe Category
* CE、時間、Tier表示

完了条件:

* datapackから機械レシピを追加できる
* reload後に機械が正しいRecipeを再解決する
* JEI上の表示とサーバー処理が一致する
* JEIなしでも本体が起動する

### Phase 3: 最初の垂直進行

* Clay Ore
* Dense Clay Ore
* Large Dense Clay Ore
* ワールド生成
* Clay Work Table
* 初期工具
* 圧縮粘土
* 初期マシン筐体
* Bending Machine
* Milling Machine
* Waterwheel
* 最初のCE生成
* 最初のCE消費機械
* 対応する全JEIカテゴリ

完了条件:

新規ワールドから外部Modなしで、最初のCE機械を作成し、稼働させられる。

### Phase 4: 基本加工機

* Grinder
* Condenser
* Decomposer
* Smelter
* Lathe
* Cutting Machine
* Wire Drawing Machine
* Pipe Drawing Machine
* Assembler
* Inscriber
* Centrifuge
* 対応JEIカテゴリ

完了条件:

* Tier 1から中間Tierまでの主要部品を製造できる
* 全Recipe TypeがJEIに表示される
* 出力詰まり時に入力を消費しない
* チャンク再ロード後も処理が継続する

### Phase 5: 物流と自動化

* 側面別I/O
* 自動搬入・搬出
* Clay Buffer
* Multi-track Buffer
* Distributor
* Storage Container
* Void Container
* Smart Filter
* Clay Configurator
* Memory機能

完了条件:

* 外部パイプから搬入・搬出できる
* Clayium単体で複数機械を自動接続できる
* Itemの消失、複製、無限ループがない
* フィルター設定がData Componentとして保存される

### Phase 6: Fluidと化学処理

* 汎用Fluid Capability
* Fluid Buffer
* Clay Canister
* Chemical Reactor
* Electrolysis Reactor
* Salt Extractor
* Silicon処理
* Aluminum処理
* 液体対応JEI表示

完了条件:

* 他Modの一般的なFluid Containerと相互運用する
* 専用カプセルなしで機械へ液体を搬入できる
* JEIで液体量と入出力方向が確認できる

### Phase 7: Clay Steelと初期多ブロック

* Alloy Smelter
* Clay Blast Furnace
* Clay Interface
* Redstone Interface
* Clay Steel
* Clay Steel工具
* 多ブロック構造検証基盤
* 多ブロックJEIまたは説明表示

完了条件:

* 多ブロックがチャンク再ロード後に再構築される
* 構成Tierが処理条件へ反映される
* 不完全構造で処理を開始しない

### Phase 8: ClayiumおよびUltimate

* Clay Laser
* Laser Interface
* Clay Reactor
* Matter Transformer
* Solar Clay Fabricator
* Clay Fabricator
* Auto Clay Condenser
* Overclocker
* Energy Storage Upgrade
* CE-FE Converter
* 関連JEIカテゴリ

完了条件:

* レーザー条件がJEIで確認できる
* CE-FE変換で増殖ループが発生しない
* ClayiumおよびUltimate Tierへ到達できる

### Phase 9: Antimatter、OEC、OPA、PAN

実装順:

1. CA Injector
2. CA Condenser
3. Resonator
4. Resonating Collector
5. CA Reactor
6. Pure Antimatter圧縮系列
7. OEC
8. OPA
9. PAN Core
10. PAN Adapter
11. PAN Duplicator

完了条件:

* 新規ワールドからPAN到達までのレシピグラフが連結している
* CA Reactorの構造検証が決定論的である
* 大規模構造探索がサーバー負荷上の上限を持つ
* 全特殊RecipeがJEIで説明される

### Phase 10: 周辺機能

* Block Breaker
* Ranged Miner
* Advanced Ranged Miner
* Ranged Replacer
* Item Collector
* Activator
* Ranged Activator
* Auto Trader
* Chunk Loader
* Clay Gun
* Teleporter
* Metal Chest
* 装飾ブロック
* Jade連携
* 必要に応じたEMI連携

これらを中核進行より先に実装してはならない。

---

## 19. 機械一台のDefinition of Done

機械は以下をすべて満たした場合のみ完成とする。

### 登録

* Blockが登録されている
* BlockItemが登録されている
* Block Entity Typeが登録されている
* 必要ならMenu Typeが登録されている
* 安定したResource IDを持つ

### リソース

* Blockstateが存在する
* Block Modelが存在する
* Item Modelが存在する
* Loot Tableが存在する
* 翻訳キーが存在する
* 本家テクスチャが適切に割り当てられている

### ゲームロジック

* 保存とロードが成立する
* チャンク再ロード後に状態が保持される
* 入出力がトランザクションとして処理される
* 出力容量不足時に入力を消費しない
* CE不足時に不正進行しない
* Tier条件が適用される
* Redstone制御が成立する
* 側面設定が成立する

### 相互運用

* Item Capabilityが正しい
* Fluid使用機械ではFluid Capabilityが正しい
* FE使用機械ではEnergy Capabilityが正しい
* 自動化Modからアクセスできる
* Simulation呼び出しで状態を変更しない

### UIと情報表示

* GUIが存在する
* 停止理由を確認できる
* JEI Recipe Categoryが存在する
* Recipe Catalystが登録されている
* CE、時間、Tier、確率が表示される
* 必要ならJade表示が存在する

### テスト

* Recipe単体テスト
* 保存・ロードテスト
* Capabilityテスト
* 出力詰まりテスト
* GameTest
* Dedicated Server起動確認

---

## 20. ワールド操作機械

ワールドへ作用する機械は、通常機械より厳格に扱う。

### 20.1 共通要件

* 未ロードチャンクへ無断アクセスしない
* ブロック保護イベントを尊重する
* Loot Contextを使用する
* Block Entityを持つBlockへの処理方針を明示する
* 一tick当たりの最大処理数を持つ
* 範囲をサーバー設定で制限する
* 所有者を記録する
* 失敗時にItemやBlockを消失させない
* サーバースレッド外でLevelを操作しない

### 20.2 Ranged Miner

* 採掘順序を決定論的にする
* 範囲全体を毎tick再走査しない
* 採掘対象タグをdatapack化する
* ブラックリストを設定可能にする
* Fortune、Silk Touchなどの処理を通常採掘と整合させる
* インベントリ満杯時は停止する

### 20.3 Activator

* プレイヤー操作を無条件に完全模倣しない
* Fake Playerまたは適切なイベント経路を使用する
* 権限、距離、対象Blockを検証する
* GUIを開く操作をデフォルトで拒否してよい
* 禁止対象をタグまたは設定で指定可能にする

### 20.4 Chunk Loader

* デフォルト無効を許容する
* 所有者単位の上限を持つ
* サーバー全体の上限を持つ
* 範囲を明示する
* 機械設置だけで自動有効化しない
* チャンクチケットを確実に解放する

---

## 21. 性能要件

以下を避ける。

* 毎tickの全レシピ線形探索
* 毎tickの多ブロック全体再走査
* 毎tickの周囲Capability再取得
* 毎tickの全状態ネットワーク同期
* チャンク全体の同期走査
* 無制限の再帰フィルター
* 無制限の隣接物流探索
* GUI表示のためのサーバー全レシピ送信
* 大容量インベントリの全スロット常時同期

レシピ検索はRecipe Type、入力内容、Tierなどで索引可能な構造を検討する。

多ブロック構造は、Block更新時または一定間隔で再検証し、通常tickではキャッシュされた結果を使用する。

大量機械が存在する環境を前提とし、一台当たりのアイドル時処理を最小化する。

---

## 22. テスト方針

### 22.1 必須検証

各変更後に可能な範囲で以下を実行する。

```text
compile
build
```

ここではビルドが通るだけで一旦完成と判断する。ゲームサーバーの起動およびゲーム内テストはエージェントではなく人間による手動で実施する。

### 22.2 Recipeグラフ検証

全Recipeを有向グラフとして解析し、以下を検査する。

* 新規ワールドから各Tierへ到達できる
* 自己参照だけで生成される素材がない
* 機械を作るために同じ機械を必要としない
* Tier Nの設備でTier N+1へ進める
* 外部Modなしで本流を完走できる
* 共通タグを持つ外部素材でも進行できる
* 廃止されたItem IDをRecipeが参照していない
* 存在しないタグを参照していない

---

## 23. コード品質

### 23.1 必須事項

* Resource IDを定数化する
* Nullの意味を明確にする
* Magic Numberを避ける
* Tier数値を直接散在させない
* public APIと内部実装を分離する
* ClientとCommonの依存方向を守る
* 不変条件をコンストラクタまたはCodecで検証する
* 失敗理由をログまたはUIで確認できるようにする
* 過剰なReflectionを使用しない
* 無意味なMixinを使用しない
* NeoForgeイベントまたはAPIで解決できる処理にMixinを使用しない

### 23.2 禁止事項

* 空の例外処理
* 例外の握りつぶし
* 未検証のunchecked cast
* Recipe実行中の部分的入力消費
* LevelやBlock Entityの静的保持
* クライアントクラスのCommonコードからの直接参照
* 登録後に変更されるグローバル可変レジストリ
* 本番コードに残る無条件のデバッグ出力
* 理由のないTODO
* 動作しないスタブを完成扱いにする

---

## 24. エージェントの作業手順

エージェントは、各タスクについて以下の順序で作業する。

1. 本家Clayiumにおける対象機能の役割を確認する
2. 現行リポジトリの設計と既存APIを確認する
3. Minecraft 1.21.1およびNeoForgeで対応する概念を確認する
4. 既存の共通実装で表現できるか検討する
5. 最小の垂直スライスを実装する
6. JEI表示を追加または更新する
7. Datagenを更新する
8. 保存、同期、Capabilityを検証する
9. このAGENTS.mdおよび関連ドキュメントを更新する

大量の登録コードを先に生成し、後から基盤を修正する進め方は禁止する。

一度に複数Tier、複数Recipe Type、複数特殊機械を未検証のまま追加してはならない。

---

## 25. 判断が必要な場合の優先順位

仕様が不明確な場合は、以下の優先順位で判断する。

1. ゲーム進行が破綻しない
2. 本家Clayiumの役割とアイデンティティを維持する
3. サーバー側で決定論的に動作する
4. NeoForge標準APIと相互運用する
5. datapackから変更できる
6. JEIで理解できる
7. 自動化できる
8. サーバー負荷が予測可能である
9. 実装が単純で保守しやすい
10. 本家の細部を厳密に再現する

本家の細部を再現することで、現行環境における操作性、相互運用性、安定性が大幅に悪化する場合は、役割を維持した再設計を選ぶ。

---

## 26. 非目標

本プロジェクトは以下を目的としない。

* 旧Minecraft内部構造の再現
* 旧数値IDの維持
* 旧メタデータ形式の維持
* 独自物流以外を排除する閉鎖的設計
* 全機械のFE化
* 他の工業Modと同一の進行体系への変更
* テクスチャの全面刷新
* 本家Clayiumを一般的なTech Modへ置き換えること

---

## 27. 最終受け入れ条件

正式な移植版として完成と判断するためには、以下を満たす必要がある。

* CC BY 4.0の帰属表示が適切である
* 本家Clayiumのテクスチャが使用されている
* 新規ワールドからPANまで進行できる
* 外部Modなしでも本流が成立する
* Item、Fluid、FEの標準Capabilityと相互運用する
* CEの独自性が維持されている
* すべての独自機械RecipeがJEIで閲覧できる
* datapackからRecipeとWorld Generationを変更できる
* Dedicated Serverで正常動作する
* チャンク再ロード後に機械状態が壊れない
* 自動化ラインでItem、Fluid、Energyが増殖または消失しない
* ワールド操作機械が保護、負荷、所有権を考慮している
* 中核機能にGameTestが存在する
* 本家Clayiumの進行と視覚的アイデンティティが維持されている
