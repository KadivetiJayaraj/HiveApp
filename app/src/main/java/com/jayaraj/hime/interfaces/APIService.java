package com.jayaraj.hime.interfaces;

import com.jayaraj.hime.notifications.RetrofitResponse;
import com.jayaraj.hime.notifications.Sender;
import com.jayaraj.hime.util.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
  @Headers({"Content-Type:application/json", "Authorization:key=" + Constants.FCMAPIKEY})
  @POST("fcm/send")
  Call<RetrofitResponse> sendNotification(@Body Sender body);
}
