package com.jayaraj.hime.notifications;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {

  private static Retrofit retrofit = null;

  private Client() {}

  public static Retrofit getClient(String url) {
    if (retrofit == null) {
      retrofit =
          new Retrofit.Builder()
              .baseUrl(url)
              .addConverterFactory(GsonConverterFactory.create())
              .build();
    }
    return retrofit;
  }
}
