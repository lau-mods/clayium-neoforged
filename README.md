# Clayium Neoforged

Minecraft 1.21.1 / NeoForge向けに再設計するClayium移植プロジェクトです。
本家Clayiumの粘土中心の進行、Clay Energy（CE）、機械加工、および後期コンテンツの役割を維持しつつ、現行APIに適合する構造へ移行します。

## 識別子

- Mod ID: `clayium_neoforged`
- Java namespace: `net.claustra01.clayium`
- Display name: `Clayium Neoforged`
- Minecraft: `1.21.1`
- Loader: `NeoForge`
- License: `CC BY 4.0`

## 現在の実装状況

Phase 2（共通Recipe基盤とJEI）、Phase 3の最初の垂直進行、Phase 4（基本加工機）を実装し、Phase 5（物流と自動化）へ着手しています。Clay Ore系列とデータ駆動worldgen、圧縮粘土、初期工具・部品・筐体、2種類のWater Wheel、Phase 4加工機に加え、本家Tier構成のBuffer、Multi-track Buffer、Distributor、Storage Container、Void Containerを登録しています。

最初の縦スライスとしてClay Work Tableを独立したBlock Entity、Menu、Screen、永続インベントリを持つClayiumデバイスとして実装しています。vanilla作業台の継承やvanilla crafting recipe処理は使用しません。本家の手加工と同様、加工Recipeはtick待機ではなく、対応する操作ボタンを必要回数押すことで進行します。

Machine Recipe JSONはMinecraft 1.21.1のdatapack構造に従い、`data/<namespace>/recipe/`へ配置します。JEIは接続中ワールドのRecipeManagerと同じレシピだけを表示し、リソースJSONを直接読むフォールバックは設けません。

共通機械はサーバー側でレシピを解決・実行し、単純機械の1入力1出力、Assembler/Inscriberの2入力1出力、CentrifugeのTier依存1～4出力を共通Recipeモデルで管理します。全出力を事前検証してから入力を消費し、内部CE、進捗、停止理由、64bit CE同期を共通実装で保持します。Smelterは本家と同様、現在ロードされているvanilla/datapack/他Modの全Smelting Recipeを処理対象にします。

Phase 5の機械と物流ブロックは側面別Item Capabilityを公開し、Clay ConfiguratorによるI/O切替、I/O Memory Cardによる設定保存・適用、Data ComponentベースのSmart Filterに対応します。隣接Capabilityは無効化通知付きキャッシュを使用し、Bufferと機械の自動搬出、Distributorの巡回分配をサーバー側で実行します。Tier 4以上の加工機には本家と同じEnergetic Clay専用スロットを設け、圧縮段階ごとのCE値を必要時に内部CEへ変換します。

Mod内の登録済みItemは専用Creative Tab「Clayium Neoforged」にまとめ、vanilla Creative Tabへの重複追加は行いません。

通常のClay Oreは採掘時に粘土玉を直接ドロップします。Dense Clay OreおよびLarge Dense Clay Oreは初期炉で精錬せず、GrinderとCondenserによる3段階の圧縮粘土Shard系列へ接続します。Clay Water WheelとDense Clay Water Wheelは本家のTier別発電量と供給対象を再現します。CE表示は本家と同じく値に応じて`uCE`、`mCE`、`CE`などの接頭辞を使用し、Tierは数値で表示します。

本家Jarの解析結果や一時的な移植用スクリプトなど、配布対象外の作業ファイルは `.tmp/` に置きます。このディレクトリはGit管理対象外です。

## ビルド

```text
./gradlew build
./gradlew runData
```

ゲーム内のクライアントおよびDedicated Server起動確認は、ビルド後に手動で実施します。

## 帰属

本プロジェクトは、本家Clayium（作者 deb_rk）のゲームデザインおよびアセットを基礎とする独立した移植・再実装です。詳細は [CREDITS.md](CREDITS.md)、[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)、[LICENSE](LICENSE) を参照してください。

NeoForge MDKのマッピングおよび利用条件については、NeoForgedの公式ドキュメントを参照してください。
