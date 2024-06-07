package com.zeewain.base.data.http;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TimeoutInterceptor implements Interceptor {
    public static final String CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
    public static final String READ_TIMEOUT = "READ_TIMEOUT";
    public static final String WRITE_TIMEOUT = "WRITE_TIMEOUT";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        int connectTimeout = chain.connectTimeoutMillis();
        int readTimeout = chain.readTimeoutMillis();
        int writeTimeout = chain.writeTimeoutMillis();

        Request request = chain.request();
        String connectTimeoutNew = request.header(CONNECT_TIMEOUT);
        String readTimeoutNew = request.header(READ_TIMEOUT);
        String writeTimeoutNew = request.header(WRITE_TIMEOUT);

        if (connectTimeoutNew != null && !connectTimeoutNew.isEmpty()) {
            connectTimeout = Integer.parseInt(connectTimeoutNew);
        }
        if (readTimeoutNew != null && !readTimeoutNew.isEmpty()) {
            readTimeout = Integer.parseInt(readTimeoutNew);
        }
        if (writeTimeoutNew != null && !writeTimeoutNew.isEmpty()) {
            writeTimeout = Integer.parseInt(writeTimeoutNew);
        }

        return chain.withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .withWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .proceed(request);
    }
}
