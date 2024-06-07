package com.zeewain.base.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AsyncPlayer;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public class AsyncPlayerUtils {
    @SuppressLint("StaticFieldLeak")
    public static volatile AsyncPlayerUtils instance;
    private final Context context;
    private static AsyncPlayer asyncPlayer;
    private static AudioAttributes audioAttributes;
    private Uri audioUri;

    private AsyncPlayerUtils(Context context, Uri audioUri){
        this.context = context;
        this.audioUri = audioUri;

        asyncPlayer = new AsyncPlayer("focused");

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
    }

    public static void init(Context context, Uri audioUri){
        if(instance == null){
            synchronized (AsyncPlayerUtils.class){
                if(instance == null){
                    instance = new AsyncPlayerUtils(context, audioUri);
                }
            }
        }
    }

    public void setAudioUri(Uri audioUri){
        //Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" +resId);
        this.audioUri = audioUri;
    }

    public void play(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            asyncPlayer.play(context, audioUri, false, audioAttributes);
        }
    }

    public void stop(){
        asyncPlayer.stop();
    }
}
