package com.zee.launcher.home.gesture.utils;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

/**
 *
 * @author zlc
 *
 */
public class MediaManager {
    private static MediaPlayer mMediaPlayer;  //播放录音文件
    private static boolean isPause = false;
    static {
        if(mMediaPlayer==null){
            mMediaPlayer=new MediaPlayer();
            mMediaPlayer.setOnErrorListener( new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        }
    }
    /**
     * 播放音频
     * @param filePath
     * @param onCompletionListenter
     */
    public static void playSound(Context context,String filePath, MediaPlayer.OnCompletionListener onCompletionListenter){
        if(mMediaPlayer==null){
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener( new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        }else{
            mMediaPlayer.reset();
        }
        try {
//详见“MediaPlayer”调用过程图
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListenter);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.e("wang", "onPrepared: MediaPlayer");
                }
            });
            mMediaPlayer.start();
        } catch (Exception e) {
// TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("语音error==",e.getMessage());
        }
    }



    /**
     * 播放音频
     * @param afd
     * @param onCompletionListenter
     */
    public static void playSoundAssets(AssetFileDescriptor afd, MediaPlayer.OnCompletionListener onCompletionListenter){
        if(mMediaPlayer==null){
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener( new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        }else{
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListenter);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
// TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("语音error==",e.getMessage());
        }
    }


    /**
     * 暂停
     */
    public synchronized static void pause(){
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            isPause=true;
        }
    }
    //停止
    public synchronized static void stop(){
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            isPause=false;
        }
    }
    /**
     * resume继续
     */
    public synchronized static void resume(){
        if(mMediaPlayer!=null && isPause){
            mMediaPlayer.start();
            isPause=false;
        }
    }
    public static boolean isPause(){
        return isPause;
    }
    public static void setPause(boolean isPause) {
        MediaManager.isPause = isPause;
    }
    /**
     * release释放资源
     */
    public static void release(){
        if(mMediaPlayer!=null){
            isPause = false;
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    public synchronized static void reset(){
        if(mMediaPlayer!=null) {
            mMediaPlayer.reset();
            isPause = false;
        }
    }
    /**
     * 判断是否在播放视频
     * @return
     */
    public synchronized static boolean isPlaying(){
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }
}