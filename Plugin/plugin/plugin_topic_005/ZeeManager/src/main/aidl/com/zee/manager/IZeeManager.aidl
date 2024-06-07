
package com.zee.manager;
import com.zee.manager.IZeeCallback;

interface IZeeManager {

    void deletePackage(String packageName);

    void addZeeCallback(in IZeeCallback callback);

    void removeZeeCallback(in IZeeCallback callback);

    void removeAllRecentTasks(String excludePackageName);

    void removeRecentTask(String packageName);
}