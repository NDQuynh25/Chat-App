package com.example.chatappfirebase.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.adapters.BotMessagesAdapter;
import com.example.chatappfirebase.models.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BotActivity extends AppCompatActivity {
    public final String waitMessage = "Chờ em tí (^-^)";
    public EditText question;
    public boolean ok;
    private RecyclerView recyclerView;
    private TextView welcomeTextView;
    private CardView send;
    private List<Message> messageList;
    private AppCompatImageView back;
    private BotMessagesAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);
        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        question = findViewById(R.id.message_edit_text);
        send = findViewById(R.id.send_btn);
        back = findViewById(R.id.back);

        // setup recycler view
        messageAdapter = new BotMessagesAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        ok = true;

        back.setOnClickListener(v -> onBackPressed());
        send.setOnClickListener(v -> {
            String request = question.getText().toString();
            if (ok) {
                addToChat(request, Message.SENT_BY_ME);
                ok = false;
                addResponse(waitMessage);
                question.setText("");
                callAPI(request);

                welcomeTextView.setVisibility(View.GONE);
            } else {
                Toast.makeText(BotActivity.this, "Gửi ít thôi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    void addResponse(String response) {
        if (!response.equals(waitMessage)) {
            messageList.remove(messageList.size() - 1);
            addToChat(response, Message.SENT_BY_BOT);
        } else {
            addToChat(response, Message.SENT_BY_BOT);
        }
    }

    void callAPI(String question) {
        // okhttp
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo-0613");

            JSONArray messageArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("role", "user");
            obj.put("content", question);
            messageArr.put(obj);

            jsonBody.put("messages", messageArr);

        } catch (JSONException e) {
            ok = true;
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.example.com/v1/chat/completions") // Thay đổi URL API
                .header("Authorization", "Bearer YOUR_API_KEY") // Thay đổi API Key
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ok = true;
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ok = true;
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().toString());
                }
            }
        });
    }
}
