package com.example.save_food

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// Data model cho tin nhắn
data class ChatMessage(val message: String, val isUser: Boolean)

class ChatBotAIActivity : AppCompatActivity() {
    private var botResponse: String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    // TODO: Cập nhật đường dẫn endpoint Gemini và API key của bạn
    private val geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-001:generateContent"
    private val geminiApiKey = "AIzaSyCzxUglgdjkm7KDPMNk_Be9AHdowFoFLc0"

    // Biến kiểm tra tin nhắn đầu tiên của người dùng
    private var isFirstUserMessage = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot_ai)

        // Thiết lập Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "CHAT BOT AI"

        // Ánh xạ view
        recyclerView = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Khởi tạo adapter và RecyclerView
        chatAdapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // Xử lý nút gửi tin nhắn
        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Thêm tin nhắn của người dùng vào danh sách và cập nhật giao diện
                addMessage(ChatMessage(messageText, true))
                etMessage.setText("")

                // Gọi API Gemini để lấy phản hồi từ bot
                sendMessageToGeminiStream(messageText)
            }
        }
    }

    // Hàm thêm tin nhắn và cuộn RecyclerView về cuối
    private fun addMessage(chatMessage: ChatMessage) {
        messageList.add(chatMessage)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        recyclerView.scrollToPosition(messageList.size - 1)
    }

    // Sử dụng Coroutine để gọi API Gemini bất đồng bộ
    private fun sendMessageToGemini(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                botResponse = callGeminiApi(message)
                withContext(Dispatchers.Main) {
                    // Cập nhật tin nhắn của bot lên giao diện
                    addMessage(ChatMessage(botResponse, false))
                }
            } catch (e: Exception) {
                Log.e("ChatBotAI", "Lỗi khi gọi Gemini API", e)
                withContext(Dispatchers.Main) {
                    addMessage(ChatMessage("Xin lỗi, tôi không thể phản hồi lúc này.", false))
                }
            }
        }
    }

    // Hàm gọi API Gemini sử dụng OkHttp
    private fun callGeminiApi(message: String): String {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaTypeOrNull()

        // Xây dựng prompt tùy theo tin nhắn đầu tiên hay không
        val prompt = if (isFirstUserMessage) {
            // Sau khi sử dụng tin nhắn đầu tiên thì cập nhật biến flag
            isFirstUserMessage = false
            "Xin chào! Tôi là ChatBot AI của Connect Share. " +
                    "Bạn là 1 CHAT BOT AI được phát triển để hỗ trợ người dùng về tư vấn và giải đáp mọi thắc mắc của người dùng với style dễ thương và nghiêm túc về ứng dụng Connect Share này, " +
                    "bạn được phát triển bởi 1 đội NCKH K17 tại trường SIU. Nếu câu hỏi của người dùng có chứa từ khóa liên quan đến 'bạn là ai', 'bạn từ đâu', 'bạn do ai phát triển', v.v., thì hay giới thiệu, " +
                    "còn không thì tập trung vô câu hỏi người dùng mà trả lời và không cần phải giới thiệu lại. Lời nhắn: $message (Chú ý hãy đọc kỹ lời nhăn và tập trung trả lời lời nhắn thôi, đừng trả lời thừa thải!)"
        } else {
            "Bạn là 1 CHAT BOT AI được phát triển để hỗ trợ người dùng về tư vấn và giải đáp mọi thắc mắc của người dùng với style dễ thương và nghiêm túc về ứng dụng Connect Share này, " +
                    "bạn được phát triển bởi 1 đội NCKH K17 tại trường SIU. Nếu câu hỏi của người dùng có chứa từ khóa liên quan đến 'bạn là ai', 'bạn từ đâu', 'bạn do ai phát triển', v.v., thì hay giới thiệu, " +
                    "còn không thì tập trung vô câu hỏi người dùng mà trả lời và không cần phải giới thiệu lại. Lời nhắn: $message (Chú ý hãy đọc kỹ lời nhăn và tập trung trả lời lời nhắn thôi, đừng trả lời thừa thải!)"
        }

        val jsonBody = """
        {
            "contents": [{
                "parts": [{"text": "$prompt"}]
            }]
        }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody(mediaType)

        // Nối API key vào URL dưới dạng query parameter
        val urlWithKey = "$geminiApiUrl?key=$geminiApiKey"

        val request = Request.Builder()
            .url(urlWithKey)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            val responseBody = response.body?.string() ?: return "Không có phản hồi từ server."
            val jsonResponse = Gson().fromJson(responseBody, Map::class.java)
            val candidates = jsonResponse["candidates"] as? List<Map<String, Any>>
            val candidate = candidates?.firstOrNull()
            val contentMap = candidate?.get("content") as? Map<String, Any>
            val parts = contentMap?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String
            return text ?: "Không thể đọc phản hồi."
        }
    }

    private fun callGeminiApiStream(message: String, onChunk: (String) -> Unit) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaTypeOrNull()

        val prompt = "Bạn là 1 CHAT BOT AI được phát triển để hỗ trợ người dùng về tư vấn và giải đáp mọi thắc mắc của người dùng về ứng dụng Connect Share này, " +
                "bạn được phát triển bởi 1 đội NCKH K17 tại trường SIU. Nếu câu hỏi của người dùng có chứa từ khóa liên quan đến 'bạn là ai', 'bạn từ đâu', 'bạn do ai phát triển', " +
                "v.v., thì hãy giới thiệu, còn không thì tập trung vào câu hỏi người dùng mà trả lời và không cần phải giới thiệu lại. Lời nhắn: $message"

        val jsonBody = """
    {
        "contents": [{
            "parts": [{"text": "$prompt"}]
        }]
    }
    """.trimIndent()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val urlWithKey = "$geminiApiUrl?key=$geminiApiKey"

        val request = Request.Builder()
            .url(urlWithKey)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("ChatBotAI", "API call failed: ${e.message}")
                runOnUiThread {
                    onChunk("Xin lỗi, tôi không thể phản hồi lúc này.")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d("ChatBotAI", "Response code: ${response.code}, Body: $responseBody")

                if (!response.isSuccessful) {
                    runOnUiThread {
                        onChunk("Lỗi: ${response.code}")
                    }
                    return
                }

                try {
                    val jsonResponse = Gson().fromJson(responseBody, Map::class.java)
                    Log.d("ChatBotAI", "Parsed JSON: $jsonResponse")
                    val candidates = jsonResponse["candidates"] as? List<Map<String, Any>>
                    val candidate = candidates?.firstOrNull()
                    val contentMap = candidate?.get("content") as? Map<String, Any>
                    val parts = contentMap?.get("parts") as? List<Map<String, Any>>
                    val text = parts?.firstOrNull()?.get("text") as? String
                    if (text != null) {
                        runOnUiThread {
                            onChunk(text)
                        }
                    } else {
                        Log.e("ChatBotAI", "No text found in response")
                        runOnUiThread {
                            onChunk("Không thể đọc phản hồi từ API.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatBotAI", "Error parsing JSON: ${e.message}, Response: $responseBody")
                    runOnUiThread {
                        onChunk("Lỗi khi xử lý phản hồi từ API.")
                    }
                }
            }
        })
    }


    private fun sendMessageToGeminiStream(message: String) {
        val botMessageIndex = messageList.size
        addMessage(ChatMessage("", false))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                botResponse = callGeminiApi(message)
                val words = botResponse.split(" ") // Chia phản hồi thành các từ

                withContext(Dispatchers.Main) {
                    var currentText = ""
                    for (word in words) {
                        currentText += "$word "
                        messageList[botMessageIndex] = ChatMessage(currentText.trim(), false)
                        chatAdapter.notifyItemChanged(botMessageIndex)
                        recyclerView.scrollToPosition(botMessageIndex)
                        kotlinx.coroutines.delay(10L) // Delay nhỏ để tạo hiệu ứng stream
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    messageList[botMessageIndex] = ChatMessage("Xin lỗi, tôi không thể phản hồi lúc này.", false)
                    chatAdapter.notifyItemChanged(botMessageIndex)
                    recyclerView.scrollToPosition(botMessageIndex)
                }
            }
        }
    }


    // Adapter cho RecyclerView với 2 kiểu view: user & bot
    inner class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_USER = 1
        private val VIEW_TYPE_BOT = 2

        override fun getItemViewType(position: Int): Int {
            return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_USER) {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_bot_ai_view_user, parent, false)
                UserViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_bot_ai_view_ai, parent, false)
                BotViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]
            if (holder is UserViewHolder) {
                holder.bind(message)
            } else if (holder is BotViewHolder) {
                holder.bind(message)
            }
        }

        override fun getItemCount(): Int = messages.size

        inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessageUser: TextView = itemView.findViewById(R.id.tvMessageUser)
            fun bind(chatMessage: ChatMessage) {
                tvMessageUser.text = chatMessage.message
            }
        }

        inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessageBot: TextView = itemView.findViewById(R.id.tvMessageBot)
            fun bind(chatMessage: ChatMessage) {
                tvMessageBot.text = chatMessage.message
            }
        }
    }
}
