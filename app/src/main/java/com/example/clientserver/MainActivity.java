package com.example.clientserver;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.work.Data;
import androidx.work.NetworkType;
import com.example.clientserver.Work.GetTokenWorker;
import com.example.clientserver.data.TokenResponse;
import com.google.gson.Gson;
import java.util.List;
import java.util.UUID;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import static com.example.clientserver.Work.GetTokenWorker.KEY_OUTPUT_TOKEN;

public class MainActivity extends AppCompatActivity {

    public static final String TOKEN = "token";

    private String tokenData;
    private Intent intent;
    private EditText userNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, UserDetailsActivity.class);
        userNameEditText = findViewById(R.id.edit_text);
        getUserName();

    }

    public void onButtonClick(View view) {
        if (userNameEditText.getText().length() == 0) {
            Toast.makeText(this, "Whoops, missing username", Toast.LENGTH_SHORT).show();
        } else if (tokenData != null) {
            startActivity(intent);
        }
    }

    private void getUserName() {

        userNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    getToken(userNameEditText.getText().toString());
                }

            }
        });
    }

    private void getToken(String userName) {
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetTokenWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString(GetTokenWorker.USER_NAME, userName).build())
                .addTag(workTagUniqueId.toString())
                .build();
        WorkManager.getInstance().enqueue(checkConnectivityWork);
        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString())
                .observe(this, new Observer<List<WorkInfo>>() {
                    @Override
                    public void onChanged(@Nullable List<WorkInfo> workInfos) {
                        if (workInfos == null || workInfos.isEmpty()) {
                            return;
                        }
                        WorkInfo info = workInfos.get(0);
                        String tokenAsJson = info.getOutputData().getString(KEY_OUTPUT_TOKEN);
                        TokenResponse token = new Gson().fromJson(tokenAsJson, TokenResponse.class);
                        if (token != null) {
                            tokenData = token.data;
                            intent.putExtra(TOKEN, tokenData);
                        }
                    }
                });
    }



}
