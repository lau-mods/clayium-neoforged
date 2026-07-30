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

Phase 2（共通Recipe基盤とJEI）を実装中です。共通Machine RecipeのCodec / StreamCodec、現在のRecipeManagerを参照する検索、完了時のトランザクション、および最初のJEIカテゴリを含みます。

最初の縦スライスとしてClay Work Tableを独立したBlock Entity、Menu、Screen、永続インベントリを持つClayiumデバイスとして実装しています。vanilla作業台の継承やvanilla crafting recipe処理は使用しません。

Machine Recipe JSONはMinecraft 1.21.1のdatapack構造に従い、`data/<namespace>/recipe/`へ配置します。JEIは接続中ワールドのRecipeManagerと同じレシピだけを表示し、リソースJSONを直接読むフォールバックは設けません。

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
