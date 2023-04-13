package com.example.cechatgpt

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val txtResponse = findViewById<TextView>(R.id.txtResponse)
        btnSubmit.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            if (question.isNotEmpty()){
                getResponse(question){response ->
                    runOnUiThread {
                        txtResponse.text = response
                        etQuestion.text.clear()
                    }
                }
            }
        }
    }

    fun getResponse(question: String, callback: (String) -> Unit) {
        val apiKeys = "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"
        val url = "https://api.openai.com/v1/engines/text-davinci-003/completions"
        val prompt = if (question.endsWith("?")) question else "$question?"
        val requestBody = """
           { 
            "prompt": "$prompt",
            "max_tokens": 500,
            "temperature": 0
           }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKeys")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error","API Failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                if (body != null) {
                    Log.v("data",body)
                }
                else{
                    Log.v("data","empty")
                }
                val jsonObject=JSONObject(body)
                val jsonArray:JSONArray=jsonObject.getJSONArray("choices")
                val textResult=jsonArray.getJSONObject(0).getString("text").removePrefix("?")
                callback(textResult)
            }
        })
    }
}