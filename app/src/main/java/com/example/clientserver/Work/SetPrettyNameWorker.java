package com.example.clientserver.Work;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.clientserver.Server.MyOfficeServerInterface;
import com.example.clientserver.Server.ServerHolder;
import com.example.clientserver.data.SetUserPrettyNameRequest;
import com.example.clientserver.data.UserResponse;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.example.clientserver.Work.GetUserWorker.KEY_TOKEN;

public class SetPrettyNameWorker extends Worker {

    public static final String KEY_PRETTY_NAME = "key_pretty_name";
    public static final String KEY_OUTPUT = "key_output";

    public SetPrettyNameWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
        String prettyNameAsJson = getInputData().getString(KEY_PRETTY_NAME);
        String token = getInputData().getString(KEY_TOKEN);

        SetUserPrettyNameRequest prettyNameRequest = new Gson().fromJson(prettyNameAsJson, SetUserPrettyNameRequest.class);
        try {
            Response<UserResponse> response = serverInterface.setPrettyName("token "+token,
                    prettyNameRequest).execute();
            UserResponse responseBody = response.body();
            String responseAsJson = new Gson().toJson(responseBody);
            Log.d(TAG, "doWork: " + responseAsJson);
            Data outputData = new Data.Builder()
                    .putString(KEY_OUTPUT, responseAsJson)
                    .build();

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}

