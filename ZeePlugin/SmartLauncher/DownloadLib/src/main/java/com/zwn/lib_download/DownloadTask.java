package com.zwn.lib_download;

import android.os.AsyncTask;

import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.lib_download.utils.FileHelper;
import com.zwn.lib_download.utils.LogHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;
    public static final int TYPE_NET_LOADING = 4;
    private final DownloadInfo downloadInfo;
    private DownloadListener downloadListener;
    private final OkHttpClient okHttpClient;
    private boolean isCancelled = false;
    private boolean isPaused = false;
    private int lastProgress = 0;
    private File downloadFile = null;
    private final int bufferSize = 5120;
    private int defaultRetryTimes = 3;
    private int failedCode = 0;


    public DownloadTask(DownloadInfo downloadInfo, DownloadListener downloadListener, OkHttpClient okHttpClient){
        this.downloadInfo = downloadInfo;
        this.downloadListener = downloadListener;
        this.okHttpClient = okHttpClient;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        try {
            long downloadFileLength = 0; //记录已下载的文件长度
            String downloadUrl = downloadInfo.url;
            downloadFile = new File(downloadInfo.filePath);
            if(downloadFile.exists()){  //存在，拿到这个文件已经下载的长度，从这里开始下载
                downloadFileLength = downloadFile.length();
            }
            //获得这个下载文件的总长度,使用okhttp
            long contentLength = getContentLength(downloadUrl);
            if(contentLength <= 0){//url地址文件长度为0，下载失败
                failedCode = -1;
                return TYPE_FAILED;
            }else if(contentLength == downloadFileLength){ //下载完成
                return TYPE_SUCCESS;
            }

            if(!FileHelper.isSdcardHasAvailableSize(0.05f)){
                LogHelper.e("DownloadService", "Sdcard Available Size less than ratio 5%");
                failedCode = -2;
                return TYPE_FAILED;
            }

            if(downloadFileLength > 0){
                if(downloadFileLength >= bufferSize)
                    downloadFileLength = downloadFileLength - bufferSize;
                else
                    downloadFileLength = 0;
            }
            asyncRequest(contentLength, downloadFileLength, downloadUrl);
        } catch (IOException e) {
            e.printStackTrace();
            LogHelper.e("DownloadService", "getContentLength() " + e.toString());
            return TYPE_FAILED;
        }
        return TYPE_NET_LOADING;
    }

    private void asyncRequest(long contentLength, final long startIndex,  String downloadUrl){
        //运行到这里，说明既不会这个url地址有问题，也不会说这个已经下载的文件长度已经下载完成了
        Request request = new Request.Builder()
                .url(downloadUrl)
                .addHeader("RANGE","bytes="+startIndex+"-")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onFailed(downloadInfo.fileId, downloadInfo.type, 0);
            }

            @Override
            public void onResponse(Call call, Response response) {
                boolean needRetry = false;
                long loadedSize = startIndex;
                if(response.isSuccessful() && response.body() != null){
                    InputStream is = null;
                    RandomAccessFile savedFile = null;
                    try {
                        is = response.body().byteStream();
                        savedFile = new RandomAccessFile(downloadFile, "rw");
                        savedFile.seek(startIndex);
                        byte[] b = new byte[bufferSize];
                        long total = 0;
                        int len;
                        while ((len = is.read(b)) != -1) {
                            if (isCancelled) {
                                onCancelled(downloadInfo.fileId);
                                return;
                            } else if (isPaused) {
                                onPaused(downloadInfo.fileId);
                                return;
                            } else {
                                total += len;
                                savedFile.write(b, 0, len);

                                //计算已经下载的百分比
                                loadedSize = total + startIndex;
                                int progress = (int) (loadedSize * 100 / contentLength);
                                if(progress > lastProgress) {
                                    onProgress(downloadInfo.fileId, progress, loadedSize, contentLength);
                                    lastProgress = progress;
                                }
                            }
                        }
                        //当运行到这里说明将url文件剩下的长度读写到文件中了
                        response.body().close();
                        onSuccess(downloadInfo.fileId, downloadInfo.type, downloadFile);
                        return;
                    }catch (IOException e){
                        e.printStackTrace();
                        LogHelper.e("DownloadService", "onResponse() " + e.toString());
                        if(FileHelper.getSdcardAvailableSizeByMB() <= 1024){
                            LogHelper.e("DownloadService", "getSdcardAvailableSizeByMB() less than " + FileHelper.getSdcardAvailableSizeByMB() + " M");
                            File file = new File(downloadInfo.filePath);
                            if(file.exists()){
                                file.delete();
                            }
                            onFailed(downloadInfo.fileId, downloadInfo.type, -2);
                            return;
                        }else if(e instanceof SocketTimeoutException){
                            needRetry = true;
                        }

                    }finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (savedFile != null) {
                                savedFile.close();
                            }
                            if (isCancelled && downloadFile != null) {
                                downloadFile.delete();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(needRetry){
                    defaultRetryTimes --;
                    if(defaultRetryTimes >= 0){
                        LogHelper.e("DownloadService", "defaultRetryTimes=" + (3 - defaultRetryTimes));
                        asyncRequest(contentLength, loadedSize, downloadUrl);
                        return;
                    }
                }
                onFailed(downloadInfo.fileId, downloadInfo.type, 0);
            }
        });
    }

    private long getContentLength(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response =  okHttpClient.newCall(request).execute();
        if(response.isSuccessful() && response.body() != null){
            long contentLength =  response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_CANCELED:
                onCancelled(downloadInfo.fileId);
                break;
            case TYPE_FAILED:
                onFailed(downloadInfo.fileId, downloadInfo.type, failedCode);
                break;
            case TYPE_PAUSED:
                onPaused(downloadInfo.fileId);
                break;
            case TYPE_SUCCESS:
                onSuccess(downloadInfo.fileId, downloadInfo.type, downloadFile);
                break;
        }
    }

    public void pausedDownload(){
        isPaused = true;
        downloadListener = null;

    }

    public void cancelDownload(){
        isCancelled = true;
        downloadListener = null;
    }

    private void onProgress(String fileId, int progress, long loadedSize, long fileSize){
        if(downloadListener != null)
            downloadListener.onProgress(fileId, progress, loadedSize, fileSize);
    }

    private void onSuccess(String fileId, int type, File file){
        if(downloadListener != null)
            downloadListener.onSuccess(fileId, type, file);
    }

    private void onFailed(String fileId, int type, int code){
        if(downloadListener != null)
            downloadListener.onFailed(fileId, type, code);
    }

    private void onPaused(String fileId){
        if(downloadListener != null)
            downloadListener.onPaused(fileId);
    }

    private void onCancelled(String fileId){
        if(downloadListener != null)
            downloadListener.onCancelled(fileId);
    }
}
