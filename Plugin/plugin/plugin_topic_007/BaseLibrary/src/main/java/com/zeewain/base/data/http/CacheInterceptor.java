package com.zeewain.base.data.http;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.DiskCacheManager;
import com.zeewain.base.utils.NetworkUtil;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;


public class CacheInterceptor implements Interceptor {
    private final Context context;

    public CacheInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String reqUrl = request.url().url().toString();
        String cacheKey = null;
        if(reqUrl.contains(BaseConstants.ApiPath.SERVICE_PACKAGE_INFO)
                || reqUrl.contains(BaseConstants.ApiPath.PRODUCT_RECOMMEND_LIST)
                || reqUrl.contains(BaseConstants.ApiPath.PRODUCT_ONLINE_QUERY_LIST)
                || reqUrl.contains(BaseConstants.ApiPath.PRODUCT_DETAIL)
                || reqUrl.contains(BaseConstants.ApiPath.SW_VERSION_LATEST)
                || reqUrl.contains(BaseConstants.ApiPath.SW_VERSION_NEWER)
                || reqUrl.contains(BaseConstants.ApiPath.USER_FAVORITES_PAGE_LIST)
                || reqUrl.contains(BaseConstants.ApiPath.USER_FAVORITES_ITEM_INFO)
                || reqUrl.contains(BaseConstants.ApiPath.USER_PLAY_RECORD_LIST)
                || reqUrl.contains(BaseConstants.ApiPath.PRODUCT_APP_LIST)){

            RequestBody requestBody = request.body();
            if("POST".equals(request.method()) && requestBody != null){
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                String paramsString = buffer.readString(StandardCharsets.UTF_8);
                String path = request.url().url().getPath();
                if(path != null && !path.isEmpty()){
                    cacheKey = path + "_" + paramsString;
                }else{
                    cacheKey = request.url().url().toString() + "_" + paramsString;
                }
            }else if("GET".equals(request.method())){
                String path = request.url().url().getPath();
                if(path != null && !path.isEmpty()){
                    cacheKey = path;
                }else{
                    cacheKey = request.url().url().toString();
                }
            }
        }

        if (!NetworkUtil.isNetworkAvailable(context) && cacheKey != null) {
            Response cacheResponse = createCacheResponse(cacheKey, request);
            if(cacheResponse != null) return cacheResponse;
        }

        Response response;
        try {
            response = chain.proceed(request);
        }catch (UnknownHostException | SocketTimeoutException | UnknownServiceException exception){
            Log.e("Response", "===>>> response exception " + exception);
            if(cacheKey != null) {
                Response cacheResponse = createCacheResponse(cacheKey, request);
                if (cacheResponse != null) {
                    return cacheResponse;
                }
            }
            throw exception;
        }

        if(cacheKey != null){
            if(response.isSuccessful() && response.code() == 200){
                ResponseBody responseBody = response.body();
                if(responseBody != null){
                    MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        String subtype = contentType.subtype();
                        if (subtype.contains("json")){
                            String bodyString = responseBody.string();
                            DiskCacheManager.getInstance().put(cacheKey, bodyString);
                            ResponseBody body = ResponseBody.create(bodyString, contentType);
                            return response.newBuilder().body(body).build();
                        }
                    }
                }
            }else if(500 == response.code() || 501 == response.code() || 502 == response.code()){
                Response cacheResponse = createCacheResponse(cacheKey, request);
                if(cacheResponse != null) return cacheResponse;
            }
        }
        return response;
    }

    private Response createCacheResponse(String cacheKey, Request request){
        String cacheContent = DiskCacheManager.getInstance().get(cacheKey);
        if(cacheContent != null){
            MediaType contentType = null;
            if(request.body()!= null){
                contentType = request.body().contentType();
            }else{
                contentType = MediaType.parse("application/json; charset=utf-8");
            }

            if(contentType != null && contentType.subtype().contains("json")){
                JSONObject jsonObject = JSON.parseObject(cacheContent);
                jsonObject.put("isCache", true);
                cacheContent = jsonObject.toString();
            }

            Response.Builder builder = new Response.Builder();
            builder.request(request);
            builder.protocol(Protocol.HTTP_1_1);
            builder.message("")
                    .code(200)
                    .body(ResponseBody.create(cacheContent, contentType));
            return builder.build();
        }

        return null;
    }
}
