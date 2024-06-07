package com.zwn.launcher.data.protocol.request;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.GlobalBusinessUtils;
import com.zwn.launcher.BuildConfig;

public class UploadLogReq {
    /**
     * |className|记录日志所在类名|false|string|
     */
    public String className = "com.zee.launcher";
    /**
     * |department|部门|false|string|
     */
    public String department = "1001";
    /**
     * |exceptionType|异常类型|false|string|
     */
    public String exceptionType = "ERROR";
    /**
     * |level|日志级别|true|string|
     */
    public String level = "ERROR";
    /**
     * |line|行号|false|integer(int32)|
     */
    public int line = 1;
    /**
     * |logTime|日志时间(格式yyyy-MM-dd HH:mm:ss.SSS)|true|string|
     */
    public String logTime = "";
    /**
     * |loggerName|日志名称|false|string|
     */
    public String loggerName = "logs-android";
    /**
     * |message|自定义信息|false|string|
     */
    public String message = "";
    /**
     * |methodName|记录日志所在方法名|false|string|
     */
    public String methodName = "unknow";
    /**
     * |moduleName|模块名称|true|string|
     */
    public String moduleName = "unknow";
    /**
     * |projectCode|项目代码|true|string|
     */
    public String projectCode = BaseConstants.HOST_APP_SOFTWARE_CODE;
    /**
     * |projectVersion|项目版本|false|string|
     */
    public String projectVersion = BuildConfig.VERSION_NAME;
    /**
     * |stackTrace|堆栈信息|false|string|
     */
    public String stackTrace = "";
    /**
     * |timeZone|时区(默认北京时区：GMT+8)|false|string|
     */
    public String timeZone = GlobalBusinessUtils.getCurrentTimeZone();

    public UploadLogReq(String className, String logTime, String message, String methodName) {
        this.className = className;
        this.logTime = logTime;
        this.message = message;
        this.methodName = methodName;
    }

    public UploadLogReq(String message, String logTime) {
        this.logTime = logTime;
        this.message = message;
        if (timeZone.equals("GMT")) {
            timeZone = "GMT+8";
        }
    }
}

