package com.zeewain.base.utils;

import com.zeewain.base.BaseApplication;
import com.zeewain.base.data.http.CacheInterceptor;
import com.zeewain.base.data.http.TokenInterceptor;
import com.zeewain.base.data.http.logging.Level;
import com.zeewain.base.data.http.logging.LoggingInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final int DEFAULT_TIMEOUT =5;
    private static Retrofit retrofit;

    private static class SingletonHolder {
        private static final RetrofitClient INSTANCE = new RetrofitClient();
    }

    public static RetrofitClient getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private RetrofitClient() {
        this(BaseApplication.baseUrl, null);
    }

    private RetrofitClient(String url, Map<String, String> headers) {
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        final boolean isDebug = CommonUtils.isDebugEnable();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor())
                .addInterceptor(new LoggingInterceptor
                        .Builder()//构建者模式
                        .loggable(isDebug) //是否开启日志打印
                        .setLevel(Level.BASIC) //打印的等级
                        .log(Platform.INFO) // 打印类型
                        .request("Request") // request的Tag
                        .response("Response")// Response的Tag
                        .addHeader("log-header", "I am the log request header.")
                        .build()
                )
                .addInterceptor(new CacheInterceptor(BaseApplication.applicationContext))
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(8, 15, TimeUnit.SECONDS))
                .build();
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(url)
                .build();

    }

    public <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }

}
