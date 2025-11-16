package com.negishitakuro.android.test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.negishitakuro.android.test.ui.theme.AndroidtestTheme

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = Firebase.analytics
        remoteConfig = Firebase.remoteConfig

        // Remote Config の設定
        val configSettings = remoteConfigSettings {
            // デバッグ中は短い間隔、リリースビルドでは長い間隔に設定
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // デフォルト値を設定
        remoteConfig.setDefaultsAsync(mapOf("feature_enabled" to false))


        enableEdgeToEdge()
        setContent {
            AndroidtestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Greeting(
                            name = "Android",
                        )
                        Button(onClick = {
                            setUserProperty(true)
                        } ) {
                            Text("Set is_internal_user to true")
                        }
                        Button(onClick = {
                            setUserProperty(false)
                        } ) {
                            Text("Set is_internal_user to false")
                        }
                        Button(onClick = {
                            showLog()
                        } ) {
                            Text("showLog")
                        }
                        Button(onClick = {
                            fetchAndLogRemoteConfig()
                        }) {
                            Text("Fetch Remote Config")
                        }
                    }
                }
            }
        }
    }

    private fun setUserProperty(isInternal: Boolean) {
        val propertyValue = isInternal.toString()
        firebaseAnalytics.setUserProperty("is_internal_user", propertyValue)
        // ログを追加して、プロパティが設定されたことを確認
        Log.d("MainActivity", "Set user property 'is_internal_user' to '$propertyValue'")
    }

    private fun showLog() {
        // 推奨される非同期の方法でインスタンスIDを取得
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainActivity", "Firebase Installation ID: ${task.result}")
            } else {
                Log.e("MainActivity", "Failed to get Firebase Installation ID", task.exception)
            }
        }
    }

    private fun fetchAndLogRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("MainActivity", "Remote Config params updated: $updated")
                    // fetchAndActivateが完了した後に値を取得
                    val featureEnabled = remoteConfig.getBoolean("feature_enabled")
                    Log.d("MainActivity", "feature_enabled: $featureEnabled")
                } else {
                    Log.e("MainActivity", "Remote Config fetch failed", task.exception)
                }
            }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidtestTheme {
        Greeting("Android")
    }
}
