# 進捗

最終更新: 2026-10-13

## 現在地
- 実装中: なし
- 次にやること: 3. 面ごとの樹皮剥ぎ（パネルで仕組みを作る）

## 完了
| 日付 | 機能 | コミット | メモ |
|---|---|---|---|
| 2026-10-13 | プロジェクト作成（MDK 26.3 から） | 初回コミット | refs は Log Fences and Gates と同じコミット。MDK の wrapper jar が公式と不一致のため検証済み 9.2.1 に差し替え。neo_version を 26.3.0.42-beta に。`./gradlew build` 成功 |
| 2026-10-13 | 1. 共通基盤 | (このコミット) | LogWood（fence からコピーし名前を変更）、DeferredRegister（ModBlocks/ModItems/専用タブ）、datagen 入口（GatherDataEvent.Client）、言語プロバイダー（en_us/ja_jp）。runData 成功、MOD 一覧に表示を確認 |
| 2026-10-13 | 2. オークの原木パネル（剥ぎなし） | (このコミット) | RotatedPillarBlock 継承、燃えやすさは原木と同じ 5/5。モデルはバニラの woodProvider().logWithHorizontal()。oak_logs タグ・縦 2 → 2 のレシピと逆レシピ。GameTest 1 件（レシピの重なりなし・パネル 1 個で板材 4）成功。ゲーム内で設置・見た目・ドロップ・レシピ・燃料・タブを確認 |

## 保留・課題
- NeoForge 26.3 はベータ版。正式版が出たら `neo_version` を上げて追従する（`mcsrc/` も展開し直す）

## 遊んでみた感想・調整したいこと
- 
