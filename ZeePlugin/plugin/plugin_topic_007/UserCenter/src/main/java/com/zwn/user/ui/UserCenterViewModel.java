package com.zwn.user.ui;



import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.db.CareSettings;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.model.FavoritesItem;
import com.zwn.user.data.protocol.request.DelFavoritesReq;
import com.zwn.user.data.protocol.request.DelHistoryReq;
import com.zwn.user.data.protocol.request.FavoritePagedReq;
import com.zwn.user.data.protocol.response.DelFavoritesResp;
import com.zwn.user.data.protocol.response.FavoritePagedResp;
import com.zwn.user.data.protocol.response.FavoritesResp;
import com.zwn.user.data.protocol.response.HistoryResp;
import com.zwn.user.utils.AndroidHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class UserCenterViewModel extends BaseViewModel {
    private final UserRepository mUserRepository;

    public MutableLiveData<String> pToast = new MutableLiveData<>();
    public MutableLiveData<LoadState> pFavoritesReqState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<String>> mldDelFavoriteReqState = new MutableLiveData<>();
    public MutableLiveData<LoadState> pClearFavoritesReqState = new MutableLiveData<>();
    public MutableLiveData<LoadState> pHistoryReqState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<String>> mldDelHistoryReqState = new MutableLiveData<>();
    public MutableLiveData<LoadState> pClearHistoryReqState = new MutableLiveData<>();
    public MutableLiveData<String> mldDeletePackageName = new MutableLiveData<>();
    public MutableLiveData<String> mldDelPkgWithSlavePkg = new MutableLiveData<>();
    public MutableLiveData<String> mldDelPkgSlavePackage = new MutableLiveData<>();

    public List<FavoritesItem> pFavoriteList = new ArrayList<>();
    public List<DownloadInfo> pDownloadInfoList = new ArrayList<>();
    public HistoryResp historyResp;

    public UserCenterViewModel(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    public void reqUserFavorites() {
        pFavoriteList.clear();
        mUserRepository.getUserFavorites(new FavoritePagedReq(1, 50))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<FavoritePagedResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<FavoritePagedResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            for (FavoritesResp favoritesResp: resp.data.records) {
                                FavoritesItem item = new FavoritesItem(
                                        favoritesResp.objUrl,
                                        favoritesResp.objName,
                                        favoritesResp.objDesc == null
                                                ? "" : favoritesResp.objDesc,
                                        favoritesResp.favoriteId,
                                        favoritesResp.objId,
                                        AndroidHelper.str2Date(favoritesResp.favoriteTime));
                                pFavoriteList.add(item);
                            }
                            pFavoritesReqState.setValue(LoadState.Success);
                        } else {
                            pToast.setValue(resp.message);
                            pFavoritesReqState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        pToast.setValue("数据获取失败，请检查网络状态");
                        pFavoritesReqState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqDelFavorite(final String skuId) {
        List<String> idList = new ArrayList<>();
        idList.add(skuId);
        DelFavoritesReq delFavoritesReq = new DelFavoritesReq(idList);
        mUserRepository.delFavorites(delFavoritesReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DelFavoritesResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DelFavoritesResp> resp) {
                        if (resp.code == 0) {
                            for(int i=0; i<pFavoriteList.size(); i++){
                                if(skuId.equals(pFavoriteList.get(i).objId)){
                                    pFavoriteList.remove(i);
                                    break;
                                }
                            }

                            mldDelFavoriteReqState.setValue(new DataLoadState<>(LoadState.Success, skuId));
                        } else {
                            pToast.setValue(resp.message);
                            mldDelFavoriteReqState.setValue(new DataLoadState<>(LoadState.Failed));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pToast.setValue("删除失败，请检查网络状态");
                        mldDelFavoriteReqState.setValue(new DataLoadState<>(LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqClearFavorites() {
        List<String> idList = new ArrayList<>();
        for (FavoritesItem item: pFavoriteList) {
            idList.add(item.objId);
        }
        mUserRepository.delFavorites(new DelFavoritesReq(idList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DelFavoritesResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DelFavoritesResp> resp) {
                        if (resp.code == 0) {
                            pClearFavoritesReqState.setValue(LoadState.Success);
                        } else {
                            pToast.setValue(resp.message);
                            pClearFavoritesReqState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        pToast.setValue("清除失败，请检查网络状态");
                        pClearFavoritesReqState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUserHistory() {
        mUserRepository.getUserHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<HistoryResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<HistoryResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            historyResp = resp.data;
                            pHistoryReqState.setValue(LoadState.Success);
                        } else {
                            pToast.setValue(resp.message);
                            pHistoryReqState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pToast.setValue("数据获取失败，请检查网络状态");
                        pHistoryReqState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void removeHistoryResp(String skuId){
        for(int i=0; i<historyResp.earlier.size(); i++){
            HistoryResp.Record record = historyResp.earlier.get(i);
            if(skuId.equals(record.skuId)){
                historyResp.earlier.remove(i);
                return;
            }
        }

        for(int i=0; i<historyResp.thisMonth.size(); i++){
            HistoryResp.Record record = historyResp.thisMonth.get(i);
            if(skuId.equals(record.skuId)){
                historyResp.thisMonth.remove(i);
                return;
            }
        }

        for(int i=0; i<historyResp.inAWeek.size(); i++){
            HistoryResp.Record record = historyResp.inAWeek.get(i);
            if(skuId.equals(record.skuId)){
                historyResp.inAWeek.remove(i);
                return;
            }
        }

        for(int i=0; i<historyResp.today.size(); i++){
            HistoryResp.Record record = historyResp.today.get(i);
            if(skuId.equals(record.skuId)){
                historyResp.today.remove(i);
                return;
            }
        }
    }

    public void reqDelHistory(final String skuId) {
        List<String> skuIds = new ArrayList<>();
        skuIds.add(skuId);
        DelHistoryReq delHistoryReq = new DelHistoryReq(skuIds);
        mUserRepository.delUserHistory(delHistoryReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == 0) {
                            removeHistoryResp(skuId);
                            mldDelHistoryReqState.setValue(new DataLoadState<>(LoadState.Success, skuId));
                        } else {
                            pToast.setValue(resp.message);
                            mldDelHistoryReqState.setValue(new DataLoadState<>(LoadState.Failed));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pToast.setValue("删除失败，请检查网络状态");
                        mldDelHistoryReqState.setValue(new DataLoadState<>(LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqClearHistory() {
        mUserRepository.clearUserHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == 0) {
                            historyResp = null;
                            pClearHistoryReqState.setValue(LoadState.Success);
                        } else {
                            pToast.setValue(resp.message);
                            pClearHistoryReqState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        pToast.setValue("清除失败，请检查网络状态");
                        pClearHistoryReqState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getDownloadList() {
        pDownloadInfoList.clear();
        List<DownloadInfo> allDownloadInfo = CareController.instance.getAllDownloadInfo(
                "type=" + BaseConstants.DownloadFileType.PLUGIN_APP + " and extraId<>''");
        if (!CommonUtils.isUserLogin()) {
            return;
        }
        if ((allDownloadInfo != null) && (allDownloadInfo.size() > 0)) {
            for (int i = 0; i < allDownloadInfo.size(); i++) {
                if (allDownloadInfo.get(i).status == DownloadInfo.STATUS_SUCCESS) {
                    pDownloadInfoList.add(allDownloadInfo.get(i));
                }
            }
        }
    }

    public void delAllDownloadPluginApp() {
        List<DownloadInfo> allDownloadInfo = CareController.instance.getAllDownloadInfo("type=" + BaseConstants.DownloadFileType.PLUGIN_APP );
        for (DownloadInfo downloadInfo: allDownloadInfo) {
            int result = CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
            if(result > 0){
                mldDeletePackageName.setValue(downloadInfo.mainClassPath);
            }
        }
    }

    public int delDownload(Context context, String skuId) {
        DownloadInfo downloadInfo = null;
        for(int i=0; i<pDownloadInfoList.size(); i++){
            if(skuId.equals(pDownloadInfoList.get(i).extraId)){
                downloadInfo = pDownloadInfoList.remove(i);
                break;
            }
        }

        if(downloadInfo != null){
            if (downloadInfo.describe != null && !downloadInfo.describe.isEmpty()) {
                String masterPackageName = context.getPackageName();
                String newDesc = downloadInfo.describe.replace(masterPackageName + ";", "").replace(downloadInfo.mainClassPath + ";", "");
                if (!TextUtils.isEmpty(newDesc.trim())){// one more master;
                    clearExtraIdAndUpdateDesc(context, downloadInfo.fileId, newDesc);
                    mldDelPkgSlavePackage.setValue(downloadInfo.mainClassPath);
                    return 1;
                }
            }
            File file = new File(downloadInfo.filePath);
            if (file.exists()) {
                file.delete();
            }
            int result = CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
            if(result > 0){
                mldDelPkgWithSlavePkg.setValue(downloadInfo.mainClassPath);
            }
            return result;
        }
        return 1;
    }

    public int clearExtraIdAndUpdateDesc(Context context, String fileId, String newDesc){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CareSettings.DownloadInfo.EXTRA_ID, "");
        contentValues.put(CareSettings.DownloadInfo.DESC, newDesc);
        return context.getContentResolver().update(CareSettings.DownloadInfo.CONTENT_URI, contentValues, "fileId='" + fileId + "'", null);
    }

}