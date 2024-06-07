package com.zeewain.base.data.http;

import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.utils.SPUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class TokenInterceptor implements Interceptor {

    public TokenInterceptor() { }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        boolean topicLogin = SPUtils.getInstance().getBoolean(SharePrefer.TopicLogin, false);
        if(topicLogin){
            Request request = chain.request();
            String reqUrl = request.url().url().toString();
            if(reqUrl.contains(BaseConstants.ApiPath.DEVICE_HEALTH)
                    || reqUrl.contains(BaseConstants.ApiPath.DEVICE_OFFLINE)){
                String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
                if(userToken != null && !userToken.isEmpty()) {
                    builder.addHeader("x_auth_token", userToken);
                }
            }
        }else{
            String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
            if(userToken != null && !userToken.isEmpty()) {
                builder.addHeader("x_auth_token", userToken);
            }
        }

        if(BaseApplication.platformInfo != null){
            builder.addHeader("Platform-Info", BaseApplication.platformInfo);
        }

        Response response = chain.proceed(builder.build());
        if(response.code() == 401){
            BaseApplication.handleUnauthorized();
        }
        return response;
    }
}

