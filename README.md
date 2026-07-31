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

Phase 2（共通Recipe基盤とJEI）を完了し、Phase 3の最初の垂直進行を実装しました。Clay Ore系列とデータ駆動worldgen、圧縮粘土、初期工具・部品・筐体、Clay Bending Machine、Elemental Milling Machine、Clay Water WheelによるCE生成を含みます。

最初の縦スライスとしてClay Work Tableを独立したBlock Entity、Menu、Screen、永続インベントリを持つClayiumデバイスとして実装しています。vanilla作業台の継承やvanilla crafting recipe処理は使用しません。本家の手加工と同様、加工Recipeはtick待機ではなく、対応する操作ボタンを必要回数押すことで進行します。

Machine Recipe JSONはMinecraft 1.21.1のdatapack構造に従い、`data/<namespace>/recipe/`へ配置します。JEIは接続中ワールドのRecipeManagerと同じレシピだけを表示し、リソースJSONを直接読むフォールバックは設けません。

最初の共通機械はサーバー側でレシピを解決・実行し、入力1枠、出力1枠、内部CE、進捗、停止理由を共通実装で管理します。水車は周囲3×3×3の流動水を1秒ごとに評価し、隣接するClayium機械へCEを供給します。

通常のClay Oreは採掘時に粘土玉を直接ドロップします。Dense Clay OreおよびLarge Dense Clay Oreは初期炉で精錬せず、後続PhaseのGrinderとCondenserによる圧縮粘土Shard系列へ接続します。CE表示は本家と同じく値に応じて`uCE`、`mCE`、`CE`などの接頭辞を使用し、Tierは数値で表示します。

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
