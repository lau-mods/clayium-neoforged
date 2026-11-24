# M44 初回実装記録

## 対象

M44は `.harness-state/plan.json` の最終マイルストーンであるF-049を対象とする。本実装は、原作hotfix2 JARを正本とする統合台帳、原作レシピ意味抽出・比較、設定反映、欠落機能の修復、GameTest、2クライアント総合E2Eの実行経路を追加した。原作JARのSHA-256は `c16e35d241d46c290e07f5d0cfd35e498db79be33a374615782163ef0f5801f2` である。

## 監査結果

`scripts/generate-parity-ledger.mjs` が生成する `src/main/resources/data/clayium/parity_ledger.tsv` は2243行である。初回実装時点の判定は、同等1553件、明示的に変更済み45件、欠落269件、未確認376件である。欠落と未確認の合計645件は、`ParityLedgerGameTests.parityLedgerHasNoMissingOrUnverifiedEntries` が正式受入を拒否する対象であり、完了扱いしない。

原作レシピ意味表は3175行で、具体式2104件、条件付き具体式1063件、補助テンプレート6件、未確認1件、部分抽出1件である。比較器は原作の具体経路3167件中820件を独立比較し、106経路、56 Registry IDを完全一致として確認した。398経路には意味差異があり、2473経路は移植先候補、条件分岐または入力・出力IDの解決不足により比較未完了である。抽出済み件数と比較済み件数を同一視せず、未比較経路を含むRegistry IDを同等へ昇格しない。

GUI 20件とrenderer 25件は、原作画像、移植画像、差分画像、実行シナリオ、2クライアント、試験JARをハッシュで結合した実行証拠がないため未確認である。E2E名または予定スクリーンショット名だけでは判定を昇格しない。

## 検証結果

次の検証は初回実装時に実行した。

- `gradle build --no-daemon --offline` は成功した。
- `scripts/verify-recipe-parity.mjs` は移植版レシピJSON 884件をM39の型付き対応655件と照合し、許可された変更19件を含めて成功した。この検査は移植側台帳とJSONの整合であり、原作意味比較の代替ではない。
- JavaScript構文検査、JSON構文検査、`git diff --check` は成功した。
- GameTestは171件中170件が成功した。唯一の失敗は欠落269件と未確認376件を検出する正式台帳ゲートである。反応炉の未同期インターフェース自動結線、Synchronizerなしの構造成立と手動加工、生存進行閉包、Storage Container、PAN、設定反映を含む製品回帰試験は成功した。

専用サーバーは起動して `Done` へ到達したが、MC Pilotクライアントは `ERROR DISPLAY: Failed to initialize the mod loading system and display.` および `glfwInit` 失敗で停止した。実行環境には `DISPLAY`、`/tmp/.X11-unix`、Xvfb、westonが存在しなかった。このためGUI・renderer比較と2クライアントrelease gateは未実行であり、合格として記録しない。

## 正式受入

`.harness-state/progress.json` のM44は未完了のままとする。M44の正式完了には、台帳の欠落・未確認0件、全GameTest、全既存E2E、2クライアントrelease gate、buildの全合格が必要である。本記録は初回実装の到達点を示すものであり、F-049の正式受入合格を示さない。
