package com.zee.launcher.home.data.protocol.response;

import java.io.Serializable;
import java.util.List;

public class PublishResp {

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
    private SoftwareInfo softwareInfo;
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

    public SoftwareInfo getSoftwareInfo() {
        return softwareInfo;
    }

    public void setSoftwareInfo(SoftwareInfo softwareInfo) {
        this.softwareInfo = softwareInfo;
    }

    public List<AlgorithmInfoResp> getRelevancyAlgorithmVersions() {
        return relevancyAlgorithmVersions;
    }

    public void setRelevancyAlgorithmVersions(List<AlgorithmInfoResp> relevancyAlgorithmVersions) {
        this.relevancyAlgorithmVersions = relevancyAlgorithmVersions;
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

    public class SoftwareInfo implements Serializable {
        private String softwareId;
        private String softwareCode;
        private String softwareName;
        private int softwareCategory;
        private int softwareOs;
        private String softwareIntroduction;
        private String softwareIcon;
        private int softwareStatus;
        private String compatibilityVersion;
        private String updateTime;
        private String createTime;
        private SoftwareExtendInfo softwareExtendInfo;

        public String getSoftwareId() {
            return softwareId;
        }

        public void setSoftwareId(String softwareId) {
            this.softwareId = softwareId;
        }

        public String getSoftwareCode() {
            return softwareCode;
        }

        public void setSoftwareCode(String softwareCode) {
            this.softwareCode = softwareCode;
        }

        public String getSoftwareName() {
            return softwareName;
        }

        public void setSoftwareName(String softwareName) {
            this.softwareName = softwareName;
        }

        public int getSoftwareCategory() {
            return softwareCategory;
        }

        public void setSoftwareCategory(int softwareCategory) {
            this.softwareCategory = softwareCategory;
        }

        public int getSoftwareOs() {
            return softwareOs;
        }

        public void setSoftwareOs(int softwareOs) {
            this.softwareOs = softwareOs;
        }

        public String getSoftwareIntroduction() {
            return softwareIntroduction;
        }

        public void setSoftwareIntroduction(String softwareIntroduction) {
            this.softwareIntroduction = softwareIntroduction;
        }

        public String getSoftwareIcon() {
            return softwareIcon;
        }

        public void setSoftwareIcon(String softwareIcon) {
            this.softwareIcon = softwareIcon;
        }

        public int getSoftwareStatus() {
            return softwareStatus;
        }

        public void setSoftwareStatus(int softwareStatus) {
            this.softwareStatus = softwareStatus;
        }

        public String getCompatibilityVersion() {
            return compatibilityVersion;
        }

        public void setCompatibilityVersion(String compatibilityVersion) {
            this.compatibilityVersion = compatibilityVersion;
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

        public SoftwareExtendInfo getSoftwareExtendInfo() {
            return softwareExtendInfo;
        }

        public void setSoftwareExtendInfo(SoftwareExtendInfo softwareExtendInfo) {
            this.softwareExtendInfo = softwareExtendInfo;
        }

        public class SoftwareExtendInfo implements Serializable {
            private String mainClassPath;
            private String packageName;

            public String getMainClassPath() {
                return mainClassPath;
            }

            public void setMainClassPath(String mainClassPath) {
                this.mainClassPath = mainClassPath;
            }

            public String getPackageName() {
                return packageName;
            }

            public void setPackageName(String packageName) {
                this.packageName = packageName;
            }
        }


    }


}
