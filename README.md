# Monthly Goal Manager

月次目標管理Androidアプリケーション

## 概要

Monthly Goal Managerは、月次の目標を効率的に管理するためのAndroidアプリケーションです。Kotlin言語とJetpack Composeを使用して開発されています。

## 機能

- **目標一覧表示**: 設定した目標をカード形式で表示
- **目標編集**: 既存の目標の詳細を編集
- **進捗管理**: 目標の進捗をパーセンテージで管理
- **優先度設定**: High、Middle、Lowの3段階で優先度を設定
- **完了状態管理**: 目標の完了状態を管理

## 技術スタック

- **言語**: Kotlin
- **UIフレームワーク**: Jetpack Compose
- **アーキテクチャパターン**: MVVM
- **ナビゲーション**: Navigation Compose
- **ビルドシステム**: Gradle (Kotlin DSL)

## ビルド要件

- Android SDK 24以上
- Kotlin 2.0.21
- Android Gradle Plugin 8.1.4

## プロジェクト構造

```
app/src/main/java/com/ablomm/monthlygoalmanager/
├── MainActivity.kt          # メインアクティビティ
├── DataModels.kt           # データモデルとViewModel
├── Home.kt                 # ホーム画面とナビゲーション
├── GoalForm.kt             # 新規目標作成フォーム
├── GoalEditForm.kt         # 目標編集フォーム
├── Navigation.kt           # ナビゲーション定義
└── ui/theme/               # テーマ設定
```

## 使用方法

1. アプリを起動すると、設定済みの目標一覧が表示されます
2. 目標カードをタップすると編集画面に移動します
3. 編集画面で目標の詳細、進捗、優先度を変更できます
4. 「Save」ボタンで変更を保存します

## 改善予定

- データ永続化の実装（Room DB）
- 新規目標作成機能の完成
- 月次レポート機能
- 通知機能
- UI/UXの改善

## 開発者

このアプリは学習目的で開発されました。

## ライセンス

MIT License