package com.example.clientserver.Server;
import com.example.clientserver.data.SetUserImageUrlRequest;
import com.example.clientserver.data.SetUserPrettyNameRequest;
import com.example.clientserver.data.TokenResponse;
import com.example.clientserver.data.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyOfficeServerInterface {

//    @GET("/users/0")
//    Call<User> connectivityCheck();

    @GET("users/{user_name}/token/")
    Call<TokenResponse> getToken(@Path("user_name") String userName);


    @GET("user/")
    Call<UserResponse> getUserResponse(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @POST("user/edit/")
    Call<UserResponse> setPrettyName(@Header("Authorization") String token, @Body SetUserPrettyNameRequest request);

    @Headers("Content-Type: application/json")
    @POST("user/edit/")
    Call<UserResponse> setImageUrl(@Header("Authorization") String token, @Body SetUserImageUrlRequest request);
}
