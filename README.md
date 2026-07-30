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

Phase 1（登録と共通データモデル）を実装中です。現在は、安定した登録基盤、名前付きTierと0〜13の進行比較値、CEストレージ、共通Machine RecipeのCodec / StreamCodec、Data Component、Datagenの基礎を含みます。

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
