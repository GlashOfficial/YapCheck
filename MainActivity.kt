package com.yapcheck.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yapcheck.myapplication.ui.theme.YapCheckTheme
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle shared URL first
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                sendToBackend(sharedText)
                return  // Exit early for share handling
            }
        }

        // Normal app launch UI
        enableEdgeToEdge()
        setContent {
            YapCheckTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun sendToBackend(url: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("reel_url", url)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(mediaType, json.toString())

        val request = Request.Builder()
            .url("https://your-backend.com/factcheck")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BackendCall", "Request Failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("BackendCall", "Unexpected code $response")
                        return
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let { body ->
                        try {
                            val resJson = JSONObject(body)
                            val verdict = resJson.getString("verdict")
                            val explanation = resJson.getString("explanation")

                            val serviceIntent = Intent(
                                this@MainActivity,
                                FloatingWindowService::class.java
                            ).apply {
                                putExtra("verdict", verdict)
                                putExtra("explanation", explanation)
                            }

                            startService(serviceIntent)
                        } catch (e: Exception) {
                            Log.e("BackendCall", "JSON parsing error", e)
                        }
                    }
                }
            }
        })
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
    YapCheckTheme {
        Greeting("Android")
    }
}
