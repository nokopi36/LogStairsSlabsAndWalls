# Log Stairs, Slabs and Walls

オリジナル MOD。仕様は `docs/SPEC.md`、進捗は `docs/PROGRESS.md`。作業開始時に両方を読むこと。
姉妹 MOD: Log Fences and Gates（`../fence`）。共通部分（`LogWood`、剥ぎの処理、datagen の土台）はコピーして持つ。

## 環境
- Minecraft 26.3 / NeoForge 26.3.0.42-beta / Java 25（Gradle の toolchain が自動取得。Gradle 本体は JAVA_HOME の JDK 24 で動く）
- mod_id: `log_stairs_slabs_and_walls` / パッケージ: `com.nokopi.logstairsslabsandwalls` / 作者: `nokopi`
- 連携 MOD: なし

## 参考資料（refs/ — 読み取り専用）
`./setup_refs.sh` で取得。コミットは `refs/SOURCES.lock` に固定（Log Fences and Gates と同じコミット）。

| パス | 用途 |
|---|---|
| `refs/mdk/` | NeoForge 26.3 の雛形（ModDevGradle）。build.gradle・mods.toml の書き方の基準 |
| `refs/neoforge-docs/docs/` | NeoForge ドキュメント（最新版。26.x 用の versioned_docs はまだない） |
| `mcsrc/` | バニラ + NeoForge パッチ済みのソース（`build/moddev/artifacts/minecraft-patched-*-sources.jar` と、`~/.gradle/caches/modules-2/.../neoforge-<ver>-sources.jar` を展開したもの。NeoForge を更新したら展開し直す）。`StairBlock`・`SlabBlock`・`WallBlock`・`RotatedPillarBlock`・`Blocks`・datagen の見本はここ |
| `../fence/` | 姉妹 MOD のソース（自分のコード）。剥ぎの処理・datagen・GameTest の書き方の見本 |

## ルール
- `refs/`・`mcsrc/` 配下は編集しない・実行しない。`../fence/` も編集しない（コピーして使う）。
- NeoForge と Minecraft の API を記憶で書かない。`refs/` や `mcsrc/` で実在とシグネチャを確認してから使う。見つからなければ推測せず報告する。
- 古い書き方（`ResourceLocation` → 26.x では `Identifier`、`GameRegistry`、`@ObjectHolder`、アイテムの NBT 直書き、手書きの大量の JSON）を使わない。
- 1 回に 1 機能。計画 → 承認 → 実装 → `./gradlew build` → GameTest（あれば）→ `runClient` 確認手順の提示 → PROGRESS 更新とコミット。
- SPEC にない機能を勝手に足さない。仕様を変えるときは SPEC を先に更新し、理由を `docs/DEVIATIONS.md` に書く。
- 依存の追加・ビルド設定の変更は理由を説明してから行う。
- 数値は定数か config にまとめ、調整しやすくする。
- 言語ファイルは `en_us.json` と `ja_jp.json` の両方を更新する。可能な限りデータ生成で出力する。
- テクスチャは新規に作らず、バニラの原木（`minecraft:block/<wood>_log` / `_log_top` / `stripped_<wood>_log` / `stripped_<wood>_log_top`）をモデルから参照する。

## 26.3 での注意（ドキュメントが追いついていない点。Log Fences and Gates で確認済み）
- レシピ・ルートテーブルの datagen はリロード可能なレジストリ方式: `event.createReloadableRegistryObjects(new RegistrySetBuilder().add(Registries.LOOT_TABLE, new LootTableProvider(...)).add(RecipeProvider.asBootstrap(...)))`。見本は `mcsrc/net/neoforged/neoforge/client/ClientNeoForgeMod.java`
- タグの `add` は `ResourceKey`（`DeferredHolder#getKey()`）。ブロックとアイテム共通のタグは `BlockItemTags`
- 燃料は `Item.Properties#cookingFuel(ContextIntProviders.*)`。テクスチャは `Material`（`TextureMapping.getBlockTexture`）
- 斧で剥ぐ処理などのブロック変換はデータ駆動（`data/minecraft/block_transformer/axe.json`）。`ItemAbilities.AXE_STRIP` は廃止。面ごとの剥ぎはブロックの `useItemOn` を上書きし、斧の判定は `minecraft:block_transformer` コンポーネントで行う
- Gradle Wrapper: MDK の `gradle-wrapper.jar` は公式チェックサムと一致しなかったため、検証済みの 9.2.1 の jar と `gradlew` に差し替えた

## 利用範囲とライセンス
CurseForge で公開予定。ライセンスは MIT（`LICENSE`）。
公開前に CurseForge の最新の規約（AI 利用に関する規定を含む）と、同名の MOD がないかを確認する。
