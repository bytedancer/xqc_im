package com.bonade.xxp.xqc_android_im.http;

import android.os.Build;

import com.bonade.xxp.xqc_android_im.App;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {

    private static RetrofitManager instance;
    private static Retrofit retrofit;
    private static Gson mGson;
    private static String cookie = "";
    // 茆家龙
    public static final String BASE_URL = "https://gs.bndxqc.com/api/";
//    public static final String BASE_URL = "http://192.168.12.31:8130";
    private JSONArray mRequestRoute;

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

    private OkHttpClient httpClient() {
        LoggingInterceptor logging = new LoggingInterceptor(new Logger());
        logging.setLevel(LoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request chainRequest = chain.request();
                        Request.Builder requestBuilder = chainRequest.newBuilder();
                        requestBuilder.header("channel", "MOBILE");

                        addReqHeader(chainRequest, requestBuilder);
                        return chain.proceed(requestBuilder.build());
                    }
                })
                .build();
        return client;
    }

    /**
     * 获取reqHead头RequestRoute的基本参数
     *
     * @return
     */
    private JSONArray obtainReqHead() {
        try {
            JSONArray requestRoute = new JSONArray();
            App application = App.getContext();
            //android sdk
            JSONObject route = new JSONObject();
            route.put("system", "android");
            route.put("version", Build.VERSION.SDK);//17
            requestRoute.put(route);
            //手机型号
            route = new JSONObject();
            route.put("system", Build.MODEL);//AMOI N828
            route.put("version", Build.VERSION.RELEASE);//4.2.1
            requestRoute.put(route);
            //手机分辨率 480x800
            route = new JSONObject();
            route.put("system", "Dpi");
            route.put("version", application.getDpi());
            requestRoute.put(route);
            //app版本
            route = new JSONObject();
            route.put("system", "xqc_app_" + "Beta");
            route.put("version", application.getAppVersionName());
            requestRoute.put(route);

            route = new JSONObject();
            route.put("system", "App");
            route.put("version", application.getAppVersionName());
            requestRoute.put(route);
            return requestRoute;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加入reqHead头
     *
     * @param chainRequest
     * @param builder
     */
    private void addReqHeader(Request chainRequest, Request.Builder builder) {
        if (mRequestRoute == null) {
            mRequestRoute = obtainReqHead();
        }

        JSONObject reqHead = new JSONObject();
        try {
            App application = App.getContext();
            String account = application.getAccount();
            String deviceToken = "AlHrTRdOVQLQGZvoWXYKouen5Op3kd-3O8u2IlDxG2gN";
            if (account == null) {
                account = deviceToken;
            }
            reqHead.put("user", account);
            reqHead.put("reqTime", String.valueOf(System.currentTimeMillis()));
            reqHead.put("requestDevice", deviceToken);
            if (chainRequest != null) {
                reqHead.put("reqUrl", chainRequest.url().url());
            }
            reqHead.put("RequestRoute", mRequestRoute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        builder.header("reqHead", reqHead.toString());
    }

}
