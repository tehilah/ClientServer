package com.example.clientserver.Work;

import android.content.Context;
import android.support.annotation.NonNull;
import com.example.clientserver.Server.MyOfficeServerInterface;
import com.example.clientserver.Server.ServerHolder;
import com.example.clientserver.data.TokenResponse;
import com.google.gson.Gson;
import java.io.IOException;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;



public class GetTokenWorker extends Worker {
    public static final String USER_NAME = "user_name";
    public static final String KEY_OUTPUT_TOKEN = "key_output_token";

    public GetTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
        String userName = getInputData().getString(USER_NAME);
        try{
            Response<TokenResponse> response = serverInterface.getToken(userName).execute();
            TokenResponse token = response.body();
            String tokenAsJson = new Gson().toJson(token);

            Data outputData = new Data.Builder()
                    .putString(KEY_OUTPUT_TOKEN, tokenAsJson)
                    .build();

            return Result.success(outputData);

        }catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
