package com.example.clientserver.Work;

import android.content.Context;
import android.support.annotation.NonNull;
import com.example.clientserver.Server.MyOfficeServerInterface;
import com.example.clientserver.Server.ServerHolder;

import com.example.clientserver.data.UserResponse;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;



public class GetUserWorker extends Worker {
    public static final String KEY_TOKEN = "key_token";
    public static final String KEY_OUTPUT_USER = "key_output_user";

    public GetUserWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String token = getInputData().getString(KEY_TOKEN);
        try {
            Response<UserResponse> response = serverInterface.getUserResponse("token " + token) // pass to interface
                    .execute();
            UserResponse userResponse = response.body();
            String userAsJson = new Gson().toJson(userResponse);
            Data outputData = new Data.Builder()
                    .putString(KEY_OUTPUT_USER, userAsJson)
                    .build();

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
