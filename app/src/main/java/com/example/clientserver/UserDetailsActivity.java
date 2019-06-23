package com.example.clientserver;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.clientserver.Work.GetUserWorker;
import com.example.clientserver.Work.SetImageUrlWorker;
import com.example.clientserver.Work.SetPrettyNameWorker;
import com.example.clientserver.data.SetUserImageUrlRequest;
import com.example.clientserver.data.SetUserPrettyNameRequest;
import com.example.clientserver.data.UserResponse;
import com.google.gson.Gson;
import java.util.List;
import java.util.UUID;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import static com.example.clientserver.MainActivity.TOKEN;
import static com.example.clientserver.Work.GetUserWorker.KEY_OUTPUT_USER;
import static com.example.clientserver.Work.GetUserWorker.KEY_TOKEN;
import static com.example.clientserver.Work.SetImageUrlWorker.KEY_IMAGE_URL;
import static com.example.clientserver.Work.SetPrettyNameWorker.KEY_OUTPUT;
import static com.example.clientserver.Work.SetPrettyNameWorker.KEY_PRETTY_NAME;

public class UserDetailsActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {


    private String tokenData;
    private EditText prettyName;
    private TextView userName;
    private TextView welcomeText;
    private ImageView imageView;
    private Button editButton;
    private Button saveButton;
    private Context context;
    private ProgressBar pgsBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        Intent intent = getIntent();
        welcomeText = findViewById(R.id.welcome);
        tokenData = intent.getStringExtra(TOKEN);
        prettyName = findViewById(R.id.set_pretty_name);
        prettyName.setEnabled(false);
        userName = findViewById(R.id.user_name);
        imageView = findViewById(R.id.image);
        editButton = findViewById(R.id.edit_button);
        saveButton = findViewById(R.id.save_button);
        pgsBar = findViewById(R.id.pBar);
        context = this;
        getUser();

    }

    private void getUser() {
        UUID workTagUniqueId = UUID.randomUUID();
        Log.d("TAG", "onChanged: " + tokenData);
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetUserWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString(KEY_TOKEN, tokenData).build()) // pass to work class
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

                        String userAsJson = info.getOutputData().getString(KEY_OUTPUT_USER);

                        UserResponse userResponse = new Gson().fromJson(userAsJson, UserResponse.class);
                        if (userResponse != null) {
                            Log.d("TAG", "onChanged: "+ userResponse.data.username+" "+userResponse.data.pretty_name+" "+userResponse.data.image_url);
                            userName.setText(userResponse.data.username);
                            prettyName.setText(userResponse.data.pretty_name);
                            Glide.with(context).load("http://hujipostpc2019.pythonanywhere.com"
                                    + userResponse.data.image_url).into(imageView);
                            if(userResponse.data.pretty_name != null && !userResponse.data.pretty_name.equals("")){
                                welcomeText.append(userResponse.data.pretty_name);
                                Log.d("TAG", "onChanged: here");
                            }
                            else{
                                welcomeText.append(userResponse.data.username);
                            }
                            Log.d("TAG", "onChanged: before progress bar");
                            pgsBar.setVisibility(View.GONE);
                            Log.d("TAG", "onChanged: After pBar");
                        }
                    }

                });


    }

    private void setImageUrl(String imageUrl) {
        SetUserImageUrlRequest imageUrlRequest = new SetUserImageUrlRequest();
        imageUrlRequest.image_url = imageUrl;
        String imageUrlAsJson = new Gson().toJson(imageUrlRequest);

        Data data = new Data.Builder()
                .putString(KEY_IMAGE_URL, imageUrlAsJson)
                .putString(KEY_TOKEN, tokenData)
                .build();

        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(SetImageUrlWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(data)
                .addTag(workTagUniqueId.toString())
                .build();
        WorkManager.getInstance().enqueue(checkConnectivityWork);
        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString())
                .observe(this, new Observer<List<WorkInfo>>() {
                    @Override
                    public void onChanged(@Nullable List<WorkInfo> workInfos) {
                        if (workInfos == null || workInfos.isEmpty())
                            return;

                        WorkInfo info = workInfos.get(0);
                        String userAsJson = info.getOutputData().getString(KEY_OUTPUT);
                        UserResponse userResponse = new Gson().fromJson(userAsJson, UserResponse.class);
                        if (userResponse != null) {
                            Glide.with(context).load("http://hujipostpc2019.pythonanywhere.com"
                                    + userResponse.data.image_url).into(imageView);
                        }
                    }
                });
    }

    private void setPrettyName(final String prettyname) {
        SetUserPrettyNameRequest prettyNameRequest = new SetUserPrettyNameRequest();
        prettyNameRequest.pretty_name = prettyname;
        String prettyNameAsJson = new Gson().toJson(prettyNameRequest);

        Data data = new Data.Builder()
                .putString(KEY_PRETTY_NAME, prettyNameAsJson)
                .putString(KEY_TOKEN, tokenData)
                .build();

        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(SetPrettyNameWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(data)
                .addTag(workTagUniqueId.toString())
                .build();
        WorkManager.getInstance().enqueue(checkConnectivityWork);
        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString())
                .observe(this, new Observer<List<WorkInfo>>() {
                    @Override
                    public void onChanged(@Nullable List<WorkInfo> workInfos) {
                        if (workInfos == null || workInfos.isEmpty())
                            return;

                        WorkInfo info = workInfos.get(0);
                        String userAsJson = info.getOutputData().getString(KEY_OUTPUT);
                        UserResponse userResponse = new Gson().fromJson(userAsJson, UserResponse.class);
                        if(userResponse != null){
                            prettyName.setText(userResponse.data.pretty_name);
                        }
                    }
                });
    }

    public void showPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                setImageUrl("/images/crab.png");
                return true;
            case R.id.item2:
                setImageUrl("/images/unicorn.png");
                return true;
            case R.id.item3:
                setImageUrl("/images/alien.png");
                return true;
            case R.id.item4:
                setImageUrl("/images/robot.png");
                return true;
            case R.id.item5:
                setImageUrl("/images/octopus.png");
                return true;
            case R.id.item6:
                setImageUrl("/images/frog.png");
                return true;
            default:
                return false;
        }

    }

    public void editPrettyName(View view) {
        saveButton.setVisibility(View.VISIBLE);
        editButton.setEnabled(false); // disable button
        prettyName.setEnabled(true);
        prettyName.setSelection(prettyName.getText().length());
        prettyName.requestFocus();
    }


    public void savePrettyName(View view) {
        saveButton.setVisibility(View.INVISIBLE);
        prettyName.setEnabled(false);
        editButton.setEnabled(true);
        setPrettyName(prettyName.getText().toString());

    }
}

