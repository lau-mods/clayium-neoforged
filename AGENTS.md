# Clayium 開発・検証

- Minecraft 1.21.1、NeoForge 21.1.264、Java 21 を使用する。バージョンの正本は `gradle.properties`。
- Mod ID は `clayium_neoforged`。Java の `ExampleMod.MODID` と生成される Mod metadata を一致させる。
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
