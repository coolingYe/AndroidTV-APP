// IExternalServices.aidl
package com.zee.unity;

// Declare any non-default types here with import statements
import com.zee.unity.IExternalCallback;

interface IExternalServices {

    void addExternalCallback(String uid, in IExternalCallback callback);

    void removeExternalCallback(String uid, in IExternalCallback callback);

    boolean prepareStartPluginApp(String uid, String fileId);

    int startPluginApp(String uid, String fileId);

    void reqStartDownloadPluginApp(String uid, String fileId);

}