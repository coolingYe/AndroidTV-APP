package com.zee.setting.data.protocol.response;

import java.io.Serializable;
import java.util.List;

public class UpgradeResp implements Serializable {
    private String versionId;
    private String softwareId;
    private String softwareVersion;
    private String feature;
    private String packageUrl;
    private int packageType;
    private String packageFileId;
    private String packageMd5;
    private String packageSize;
    private int versionStatus;
    private boolean forcible;
    private List<AlgorithmInfoResp> relevancyAlgorithmVersions;
    private String updateTime;
    private String createTime;

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(String softwareId) {
        this.softwareId = softwareId;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getPackageUrl() {
        return packageUrl;
    }

    public void setPackageUrl(String packageUrl) {
        this.packageUrl = packageUrl;
    }

    public int getPackageType() {
        return packageType;
    }

    public void setPackageType(int packageType) {
        this.packageType = packageType;
    }

    public String getPackageFileId() {
        return packageFileId;
    }

    public void setPackageFileId(String packageFileId) {
        this.packageFileId = packageFileId;
    }

    public String getPackageMd5() {
        return packageMd5;
    }

    public void setPackageMd5(String packageMd5) {
        this.packageMd5 = packageMd5;
    }

    public String getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
    }

    public int getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(int versionStatus) {
        this.versionStatus = versionStatus;
    }

    public boolean isForcible() {
        return forcible;
    }

    public void setForcible(boolean forcible) {
        this.forcible = forcible;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<AlgorithmInfoResp> getRelevancyAlgorithmVersions() {
        return relevancyAlgorithmVersions;
    }

    public void setRelevancyAlgorithmVersions(List<AlgorithmInfoResp> relevancyAlgorithmVersions) {
        this.relevancyAlgorithmVersions = relevancyAlgorithmVersions;
    }


    @Override
    public String toString() {
        return "UpgradeResp{" +
                "versionId='" + versionId + '\'' +
                ", softwareId='" + softwareId + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", feature='" + feature + '\'' +
                ", packageUrl='" + packageUrl + '\'' +
                ", packageType=" + packageType +
                ", packageFileId='" + packageFileId + '\'' +
                ", packageMd5='" + packageMd5 + '\'' +
                ", packageSize='" + packageSize + '\'' +
                ", versionStatus=" + versionStatus +
                ", forcible=" + forcible +
                ", relevancyAlgorithmVersions=" + relevancyAlgorithmVersions +
                ", updateTime='" + updateTime + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
