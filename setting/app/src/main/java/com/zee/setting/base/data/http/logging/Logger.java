package com.zee.setting.base.data.http.logging;

import okhttp3.internal.platform.Platform;

public interface Logger {
    void log(int level, String tag, String msg);

    Logger DEFAULT = new Logger() {
        @Override
        public void log(int level, String tag, String message) {
            Platform.get().log(message, level, null);
        }
    };
}
