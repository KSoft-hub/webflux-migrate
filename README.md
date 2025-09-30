# WebFlux Hello

## プロジェクト概要
Spring Boot 3 と WebFlux で構築したサンプルです。`/hello` エンドポイントを機能的ルーティングで提供し、リアクティブに文字列レスポンスを返します。

## 前提条件
- Java 21 (JDK)
- 付属の Gradle Wrapper (`./gradlew`)

## 主要タスク
- ビルド: `./gradlew clean build`
- テスト: `./gradlew test`
- アプリ起動: `./gradlew :app:bootRun`

起動後は `http://localhost:8080/hello` にアクセスするとレスポンスを確認できます。その他の例:
- `http://localhost:8080/hello/1` → `hello-1`
- `http://localhost:8080/hello/admin/2` → `admin-hello-2`

## ディレクトリ構成
```
webflux-migrate/
  README.md
  settings.gradle
  gradle.properties
  gradlew
  app/
    build.gradle
    src/main/java/com/example/webflux/WebfluxHelloApplication.java
    src/main/java/com/example/webflux/HelloHandler.java
    src/main/java/com/example/webflux/HelloRouterConfig.java
    src/test/java/com/example/webflux/HelloControllerTest.java
```

## 補足
- 依存関係は `app/build.gradle` で管理しています。
- Java のバージョンは Gradle のツールチェーン設定で 21 に固定しています。
