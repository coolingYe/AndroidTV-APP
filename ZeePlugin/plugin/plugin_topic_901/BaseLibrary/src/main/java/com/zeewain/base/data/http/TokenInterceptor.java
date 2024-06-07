package com.zeewain.base.data.http;

import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    public TokenInterceptor() { }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if(BaseApplication.userToken != null) {
            builder.addHeader("x_auth_token", BaseApplication.userToken);
        }

        if(BaseApplication.platformInfo != null){
            builder.addHeader("Platform-Info", BaseApplication.platformInfo);
        }

        Request request = chain.request();
        String url = request.url().url().toString();

        Response response;
        if(url.contains(BaseConstants.basePathHolder)){
            url = url.replace(BaseConstants.basePathHolder, BaseApplication.basePath);
            response = chain.proceed(builder.url(url).build());
        }else{
            response = chain.proceed(builder.build());
        }

        if(response.code() == 401){
            BaseApplication.handleUnauthorized();
        }
        return response;
    }
}

