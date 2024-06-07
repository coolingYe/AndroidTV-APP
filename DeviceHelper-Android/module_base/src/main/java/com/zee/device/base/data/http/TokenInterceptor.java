package com.zee.device.base.data.http;

import android.util.Log;


import com.zee.device.base.config.SharePrefer;
import com.zee.device.base.utils.SPUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class TokenInterceptor implements Interceptor {

    public TokenInterceptor() { }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
       // Log.d("Request", "userToken-->" + userToken + ", Platform-Info=" + BaseApplication.platformInfo);

        if(userToken != null && !userToken.isEmpty()) {
            builder.addHeader("x_auth_token", userToken);
        }

     /*   if(BaseApplication.platformInfo != null){
            builder.addHeader("Platform-Info", BaseApplication.platformInfo);
        }*/

        Response response = chain.proceed(builder.build());
       /* if(response.code() == 401){
            BaseApplication.handleUnauthorized();
        }*/
        return response;
    }
}

