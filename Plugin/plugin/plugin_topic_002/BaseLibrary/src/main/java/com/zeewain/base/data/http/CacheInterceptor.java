package com.zeewain.base.data.http;

import android.content.Context;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.DiskCacheManager;
import com.zeewain.base.utils.NetworkUtil;

import java.io.IOException;
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
                || reqUrl.contains(BaseConstants.ApiPath.USER_PLAY_RECORD_LIST)){

            RequestBody requestBody = request.body();
            if("POST".equals(request.method()) && requestBody != null){
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                String paramsString = buffer.readString(StandardCharsets.UTF_8);
                cacheKey = request.url().url().toString() + "_" + paramsString;
            }else if("GET".equals(request.method())){
                cacheKey = request.url().url().toString();
            }
        }

        if (!NetworkUtil.isNetworkAvailable(context) && cacheKey != null) {
            String cacheContent = DiskCacheManager.getInstance().get(cacheKey);
            if(cacheContent != null){
                MediaType contentType = null;
                if(request.body()!= null){
                    contentType = request.body().contentType();
                }else{
                    contentType = MediaType.parse("application/json; charset=utf-8");
                }
                Response.Builder builder = new Response.Builder();
                builder.request(request);
                builder.protocol(Protocol.HTTP_1_1);
                builder.message("")
                        .code(200)
                        .body(ResponseBody.create(cacheContent, contentType));
                return builder.build();
            }
        }

        Response response = chain.proceed(request);

        if(cacheKey != null && response.isSuccessful() && response.code() == 200){
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
        }
        return response;
    }
}
