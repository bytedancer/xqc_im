package com.bonade.xxp.xqc_android_im.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {

    private static RetrofitManager instance;
    private static Retrofit retrofit;
    private static Gson mGson;
    private static String cookie = "";
    private static final String BASE_URL = "";

    private RetrofitManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient())
                .addConverterFactory(GsonConverterFactory.create(gson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    public static RetrofitManager getInstance() {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                instance = new RetrofitManager();
            }
        }
        return instance;
    }

    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    public static void reset() {
        instance = null;
    }

    public static Gson gson() {
        if (mGson == null) {
            synchronized (RetrofitManager.class) {
                mGson = new GsonBuilder().setLenient().create();
            }
        }
        return mGson;
    }

    private static OkHttpClient httpClient() {
//        LoggingInterceptor logging = new LoggingInterceptor(new Logger());
//        logging.setLevel(LoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
//                .addInterceptor(logging)
                .build();
        return client;
    }

}
