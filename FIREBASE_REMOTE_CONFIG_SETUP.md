# Firebase Realtime Remote Config 導入手順

このドキュメントでは、Android プロジェクトに Firebase Realtime Remote Config を導入する手順を説明します。

## 1. Firebase プロジェクトへの接続

1.  **Firebase コンソールでプロジェクトを作成または選択:**
    *   [Firebase コンソール](https://console.firebase.google.com/) にアクセスします。
    *   既存のプロジェクトを選択するか、「プロジェクトを追加」をクリックして新しいプロジェクトを作成します。

2.  **Android アプリを Firebase プロジェクトに追加:**
    *   プロジェクトの概要ページで、「アプリを追加」をクリックし、Android アイコンを選択します。
    *   「Android パッケージ名」を入力します（通常は `app/build.gradle.kts` または `app/build.gradle` の `applicationId`）。
    *   （オプション）「アプリのニックネーム」を入力します。
    *   （オプション）「デバッグ用の署名証明書 SHA-1」を入力します。これは、Google サインインなどの Firebase Authentication 機能を使用する場合に必要です。開発中は `./gradlew signingReport` コマンドで取得できます。
    *   「アプリを登録」をクリックします。

3.  **`google-services.json` ファイルのダウンロードと配置:**
    *   「`google-services.json` をダウンロード」をクリックします。
    *   ダウンロードした `google-services.json` ファイルを、Android Studio プロジェクトの **`app` モジュール**のルートディレクトリ（通常は `YourProjectName/app/`）に移動します。

## 2. 必要な依存関係の追加

1.  **プロジェクトレベルの `build.gradle.kts`（または `build.gradle`）の変更:**
    *   `buildscript` ブロックの `dependencies` に Google Services プラグインを追加します。
        ```kotlin
        // build.gradle.kts (プロジェクトルート)
        buildscript {
            repositories {
                google()
                mavenCentral()
            }
            dependencies {
                // ... 他のプラグイン
                classpath("com.google.gms:google-services:4.4.1") // 最新バージョンを確認してください
            }
        }

        plugins {
            // ... 他のプラグイン
            id("com.google.gms.google-services") version "4.4.1" apply false // apply false を確認
        }
        ```
        または Groovy の場合:
        ```groovy
        // build.gradle (プロジェクトルート)
        buildscript {
            repositories {
                google()
                mavenCentral()
            }
            dependencies {
                // ... 他のプラグイン
                classpath '''com.google.gms:google-services:4.4.1''' // 最新バージョンを確認してください
            }
        }
        ```

2.  **アプリレベルの `build.gradle.kts`（または `app/build.gradle`）の変更:**
    *   Google Services プラグインを適用します。
        ```kotlin
        // app/build.gradle.kts
        plugins {
            // ... 他のプラグイン
            id("com.google.gms.google-services")
        }
        ```
        または Groovy の場合:
        ```groovy
        // app/build.gradle
        apply plugin: '''com.google.gms.google-services''' // ファイルの末尾近くに配置
        ```
    *   Firebase BoM (Bill of Materials) と Firebase Remote Config のライブラリを `dependencies` ブロックに追加します。BoM を使用すると、互換性のある Firebase ライブラリのバージョンが自動的に管理されます。
        ```kotlin
        // app/build.gradle.kts
        dependencies {
            // ... 他の依存関係

            // Firebase BoM (Bill of Materials)
            implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // 最新バージョンを確認

            // Firebase Remote Config
            implementation("com.google.firebase:firebase-config-ktx")

            // Firebase Analytics (Remote Config の条件付き配信などに推奨)
            implementation("com.google.firebase:firebase-analytics-ktx")
        }
        ```
        または Groovy の場合:
        ```groovy
        // app/build.gradle
        dependencies {
            // ... 他の依存関係

            // Firebase BoM (Bill of Materials)
            implementation platform('''com.google.firebase:firebase-bom:33.1.0''') // 最新バージョンを確認

            // Firebase Remote Config
            implementation '''com.google.firebase:firebase-config-ktx'''

            // Firebase Analytics (Remote Config の条件付き配信などに推奨)
            implementation '''com.google.firebase:firebase-analytics-ktx'''
        }
        ```
    *   **重要:** Firebase のライブラリのバージョンは変更される可能性があるため、[Firebase Android BoM のドキュメント](https://firebase.google.com/docs/android/setup#available-libraries) で最新のバージョンを確認してください。

3.  **プロジェクトの同期:**
    *   Android Studio のツールバーに表示される "Sync Now" をクリックして、プロジェクトの依存関係を同期します。

## 3. Firebase Remote Config の初期化

アプリ内で Remote Config を使用するには、インスタンスを取得し、デフォルト値を設定します。

1.  **FirebaseRemoteConfig インスタンスの取得:**
    ```kotlin
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.remoteconfig.ktx.remoteConfig
    import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
    import com.google.firebase.remoteconfig.FirebaseRemoteConfig

    // MainActivity.kt や Application クラスなど
    lateinit var remoteConfig: FirebaseRemoteConfig

    // onCreate などで初期化
    // remoteConfig = Firebase.remoteConfig
    ```

2.  **デフォルト値の設定:**
    アプリ内で使用するパラメータのデフォルト値を設定します。これは、ネットワーク接続がない場合や、サーバーから値を取得する前に使用されます。
    デフォルト値は、XML リソースファイルまたはプログラムで設定できます。

    *   **XML リソースファイルで設定 (推奨):**
        1.  `res/xml/remote_config_defaults.xml` のような XML ファイルを作成します。
            ```xml
            <!-- res/xml/remote_config_defaults.xml -->
            <?xml version="1.0" encoding="utf-8"?>
            <defaultsMap>
                <entry>
                    <key>welcome_message</key>
                    <value>Welcome to our app!</value>
                </entry>
                <entry>
                    <key>feature_enabled</key>
                    <value>false</value>
                </entry>
            </defaultsMap>
            ```
        2.  コード内でこの XML ファイルをデフォルト値として設定します。
            ```kotlin
            // remoteConfig は初期化済みであること
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // デフォルト値の設定が完了
                    } else {
                        // デフォルト値の設定に失敗
                    }
                }
            ```

    *   **プログラムで設定:**
        ```kotlin
        val defaultConfigMap = mapOf(
            "welcome_message" to "Welcome to our app!",
            "feature_enabled" to false
        )
        remoteConfig.setDefaultsAsync(defaultConfigMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // デフォルト値の設定が完了
                } else {
                    // デフォルト値の設定に失敗
                }
            }
        ```

3.  **フェッチ間隔の設定 (オプション):**
    サーバーから新しい設定値をフェッチする最小間隔を設定します。開発中は短い間隔（例: 0秒、ただし頻繁なフェッチはスロットリングされる可能性あり）に設定し、本番環境では長い間隔（例: 12時間 = 43200秒）に設定することが推奨されます。リアルタイム更新リスナーを使用する場合でも、アプリ起動時の初回フェッチのためにこの設定は役立ちます。
    ```kotlin
    val configSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600) // 例: デバッグ時は0秒、リリース時は1時間
        .build()
    remoteConfig.setConfigSettingsAsync(configSettings)
    ```

## 4. 値のフェッチと有効化 (初回起動時など)

アプリ起動時などに、サーバーから最新の設定値を取得し、アプリで利用できるようにします。リアルタイムリスナーを設定する前に、一度フェッチと有効化を行うことが推奨されます。

```kotlin
remoteConfig.fetchAndActivate()
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val updated = task.result
            // フェッチと有効化が成功
            // `updated` が true の場合、新しい値が有効化された
            Log.d("RemoteConfig", "Config params updated: $updated")
            // ここでUI更新などの処理を行う
            displayWelcomeMessage() // 例
        } else {
            // フェッチまたは有効化に失敗
            Log.e("RemoteConfig", "Fetch and activate failed", task.exception)
        }
    }
```
`fetchAndActivate()` は、最新の値をフェッチし、成功すればそれを有効化します。

## 5. 値の取得と使用

設定したキーを使って、Remote Config から値を取得します。

```kotlin
fun displayWelcomeMessage() {
    // remoteConfig は初期化済みであること
    val welcomeMessage = remoteConfig.getString("welcome_message")
    val isFeatureEnabled = remoteConfig.getBoolean("feature_enabled")

    // welcomeMessage や isFeatureEnabled を使ってアプリの動作を変更
    //例:
    // textView.text = welcomeMessage
    // if (isFeatureEnabled) { /* 新機能を有効化 */ }
}
```
利用可能なゲッター:
*   `getString(key)`
*   `getBoolean(key)`
*   `getLong(key)`
*   `getDouble(key)`
*   `getByteArray(key)`

## 6. Firebase コンソールでのパラメータ設定

1.  Firebase コンソールのプロジェクトに移動します。
2.  左側のナビゲーションメニューから「エンゲージメント」セクションの「Remote Config」を選択します。
3.  「最初のパラメータを追加」（または「パラメータを追加」）をクリックします。
4.  「パラメータキー」を入力します（例: `welcome_message`、`feature_enabled`）。
5.  「データ型」を選択します（文字列、ブール値、数値、JSON）。
6.  「デフォルト値」を入力します。これは、条件に一致しない場合や、アプリ側でデフォルト値が設定されていない場合に使用されます。
7.  （オプション）「条件付きの値を追加」で、特定の条件下で異なる値を配信するように設定できます（例: 特定の国、アプリのバージョン、ユーザープロパティなど）。
8.  パラメータを追加したら、「変更を公開」をクリックして、設定をアプリに反映させます。

## 7. リアルタイム更新の購読 (addOnConfigUpdateListener)

Remote Config サーバー上でパラメータが更新されると、アプリ側でほぼリアルタイムにその変更を検知できます。

1.  **更新リスナーの追加:**
    `addOnConfigUpdateListener` を使用して、設定の更新をリッスンします。このリスナーは、サーバーで新しいバージョンの設定が公開され、クライアントが次に値をフェッチする際に利用可能になるとトリガーされます。
    ```kotlin
    import com.google.firebase.remoteconfig.ConfigUpdate
    import com.google.firebase.remoteconfig.ConfigUpdateListener
    import com.google.firebase.remoteconfig.FirebaseRemoteConfigException

    // MainActivity.kt や Application クラスなど、適切なライフサイクルを持つ場所で登録
    // remoteConfig は初期化済みであること

    // リスナー登録の戻り値 (registration) は、リスナーを削除する際に使用します
    val registration = remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
        override fun onUpdate(configUpdate: ConfigUpdate) {
            Log.d("RemoteConfig", "Updated keys: " + configUpdate.updatedKeys)

            // 更新されたキーに興味のあるものが含まれているかを確認できます
            // if (configUpdate.updatedKeys.contains("welcome_message")) {
            //    Log.d("RemoteConfig", "welcome_message was updated")
            // }

            // 更新を有効にするには activate() を呼び出す必要があります。
            // これにより、新しい値がアプリで使用できるようになります。
            remoteConfig.activate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 有効化成功。新しい値を使ってUIなどを更新
                    Log.d("RemoteConfig", "Remote config activated")
                    displayWelcomeMessage() // 例: UIを更新するメソッド
                } else {
                    // 有効化失敗
                    Log.e("RemoteConfig", "Remote config activation failed", task.exception)
                }
            }
        }

        override fun onError(error: FirebaseRemoteConfigException) {
            Log.w("RemoteConfig", "Config update error with code: " + error.code, error)
        }
    })
    ```

2.  **リスナーの削除:**
    Activity や Fragment のライフサイクルに合わせて、リスナーが不要になったら必ず削除してください。メモリリークを防ぐために重要です。
    ```kotlin
    // onDestroy() や onStop() など、適切なタイミングで
    // registration.remove() // 上記で取得した registration オブジェクトを使用
    ```

## 注意点

*   **スロットリング:** `fetchAndActivate()` や手動フェッチを短時間に何度も行うと、SDK がスロットリングされる可能性があります。リアルタイムリスナーを使用している場合でも、`minimumFetchIntervalInSeconds` はアプリ起動時の初期フェッチや、リスナーが一時的に機能しない場合のフォールバックとして意味を持ちます。
*   **値のキャッシュ:** Remote Config はフェッチした値をローカルにキャッシュします。`activate()` を呼び出すことで、キャッシュされた最新の値をアプリが利用できるようになります。リアルタイムリスナーのコールバック内で `activate()` を呼び出すことで、更新された値を即座に反映できます。
*   **リアルタイム性の挙動:** `addOnConfigUpdateListener` は、サーバー側で設定が更新・公開されたことを検知し、クライアントに通知します。通知を受け取った後、`activate()` を呼び出すことで、その新しい値がアプリで実際に使われるようになります。ネットワーク状況などにより、若干の遅延が発生する可能性はあります。

これで、Firebase Realtime Remote Config を Android アプリに導入する基本的な手順は完了です。
