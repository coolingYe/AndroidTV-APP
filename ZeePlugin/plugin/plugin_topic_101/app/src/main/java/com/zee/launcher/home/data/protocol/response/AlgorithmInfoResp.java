package com.zee.launcher.home.data.protocol.response;

import java.util.List;

public class AlgorithmInfoResp {
    /**
     * 安装包md5值
     */
    public String packageMd5;

    /**
     * 安装包大小
     */
    public long packageSize;

    /**
     * 安装包类型（1-zip 2-exe 3-apk 4-unit 5-rar 6-7z）
     */
    public int packageType;

    /**
     * 安装包链接
     */
    public String packageUrl;

    /**
     * 对应软件ID
     */
    public long softwareId;

    /**
     * 最新版本号
     */
    public String softwareVersion;

    /**
     * 版本ID
     */
    public String versionId;

    /**
     * 关联模型版本列表
     */
    public List<ModelInfoResp> relevancyModelVersions;

}
