package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.app.AlertDialog;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import com.example.notes.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.appcompat.app.AppCompatActivity;

public class ThirdActivity extends AppCompatActivity {

    private EditText titleEditText;
    private TextView bodyTextView;
    private Button done;

    private int noteIndex = -1; // tracks edited note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        titleEditText = findViewById(R.id.title1);
        bodyTextView = findViewById(R.id.body);
        done = findViewById(R.id.button3);

        // RECEIVE existing note (when clicking title)
        Intent intent = getIntent();
        if (intent.hasExtra("NOTE_TITLE")) {
            titleEditText.setText(intent.getStringExtra("NOTE_TITLE"));
            bodyTextView.setText(intent.getStringExtra("NOTE_BODY"));
            noteIndex = intent.getIntExtra("NOTE_INDEX", -1);
        } else {
            bodyTextView.setText("Type here...");
        }

        Button summarizeBtn = findViewById(R.id.buttonSummarize);

        summarizeBtn.setOnClickListener(v -> {
            String text = bodyTextView.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(this, "Nothing to summarize", Toast.LENGTH_SHORT).show();
                return;
            }
            summarizeWithPollinations(text);
            //summarizeWithAI(text);
        });

        // SAVE note (title + body)
        done.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String body = bodyTextView.getText().toString().trim();

            if (!title.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("NOTE_TITLE", title);
                resultIntent.putExtra("NOTE_BODY", body);
                resultIntent.putExtra("NOTE_INDEX", noteIndex);

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void summarizeWithAI(String noteText) {

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", "Summarize this note in 2 short sentences:\n" + noteText);

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            contents.put(content);

            json.put("contents", contents);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key="
                        + BuildConfig.G_API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ThirdActivity.this, "AI failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(ThirdActivity.this, "AI error: " + response.code(), Toast.LENGTH_LONG).show());
                    return;
                }

                try {
                    String res = response.body().string();
                    JSONObject obj = new JSONObject(res);

                    String summary = obj
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    runOnUiThread(() -> showSummary(summary));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void summarizeWithPollinations(String noteText) {

        OkHttpClient client = new OkHttpClient();

        try {
            String prompt = "Summarize this note in 2 short sentences: " + noteText;
            String url = "https://text.pollinations.ai/" + URLEncoder.encode(prompt, "UTF-8");

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ThirdActivity.this, "AI failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String summary = response.body().string();

                    runOnUiThread(() -> showSummary(summary));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSummary(String summary) {
        LinearLayout card = findViewById(R.id.summaryCard);
        TextView summaryText = findViewById(R.id.summaryText);

        summaryText.setText(summary);
        card.setVisibility(View.VISIBLE);
    }

}
