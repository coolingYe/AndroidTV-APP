package com.zee.launcher.home.data.protocol.response;

import com.zee.launcher.home.data.model.ProductListMo;

import java.io.Serializable;
import java.util.List;

public class ProDetailResp {
    private String skuId;
    private String skuCode;
    private String spuId;
    private String productCode;
    private String categoryId;
    private String productTitle;
    private String slogan;
    private String productPrice;
    private List<String> images;
    private String description;
    private String softwareCode;
    private TutorialInfo tutorial;
    private int putawayStatus;
    private int heat;
    private List<SpecificationsInfo> specifications;
    private ExtendInfo extendInfo;


    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getSpuId() {
        return spuId;
    }

    public void setSpuId(String spuId) {
        this.spuId = spuId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSoftwareCode() {
        return softwareCode;
    }

    public void setSoftwareCode(String softwareCode) {
        this.softwareCode = softwareCode;
    }

    public TutorialInfo getTutorial() {
        return tutorial;
    }

    public void setTutorial(TutorialInfo tutorial) {
        this.tutorial = tutorial;
    }

    public int getPutawayStatus() {
        return putawayStatus;
    }

    public void setPutawayStatus(int putawayStatus) {
        this.putawayStatus = putawayStatus;
    }

    public int getHeat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = heat;
    }

    public List<SpecificationsInfo> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<SpecificationsInfo> specifications) {
        this.specifications = specifications;
    }

    public String getUseImgUrl() {
        if (images != null && images.size() > 0) {
            return images.get(0);
        }
        return "";
    }

    public ExtendInfo getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(ExtendInfo extendInfo) {
        this.extendInfo = extendInfo;
    }


    public class TutorialInfo implements Serializable {
        private String videoUrl;

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }
    }

    public class SpecificationsInfo implements Serializable {
        private String groupId;
        private String groupName;
        //  private String sourceId;
        private int orderNum;

        private List<AttributesInfo> attributes;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

    /*    public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }*/

        public int getOrderNum() {
            return orderNum;
        }

        public void setOrderNum(int orderNum) {
            this.orderNum = orderNum;
        }


        public List<AttributesInfo> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<AttributesInfo> attributes) {
            this.attributes = attributes;
        }

        public class AttributesInfo implements Serializable {
            private String attributeId;
            private String attributeName;
            private int orderNum;
            private boolean isGlobal;
            private String attributePrompt;
            private String attributeValue;


            public String getAttributeId() {
                return attributeId;
            }

            public void setAttributeId(String attributeId) {
                this.attributeId = attributeId;
            }

            public String getAttributeName() {
                return attributeName;
            }

            public void setAttributeName(String attributeName) {
                this.attributeName = attributeName;
            }

            public int getOrderNum() {
                return orderNum;
            }

            public void setOrderNum(int orderNum) {
                this.orderNum = orderNum;
            }

            public boolean isGlobal() {
                return isGlobal;
            }

            public void setGlobal(boolean global) {
                isGlobal = global;
            }

            public String getAttributePrompt() {
                return attributePrompt;
            }

            public void setAttributePrompt(String attributePrompt) {
                this.attributePrompt = attributePrompt;
            }

            public String getAttributeValue() {
                return attributeValue;
            }

            public void setAttributeValue(String attributeValue) {
                this.attributeValue = attributeValue;
            }
        }
    }

    public static class ExtendInfo {
        private List<String> bannerImages;
        private String backgroundLogo;
        private ProductListMo.BackgroundImages backgroundImages;

        public List<String> getBannerImages() {
            return bannerImages;
        }

        public void setBannerImages(List<String> bannerImages) {
            this.bannerImages = bannerImages;
        }

        public String getBackgroundLogo() {
            return backgroundLogo;
        }

        public void setBackgroundLogo(String backgroundLogo) {
            this.backgroundLogo = backgroundLogo;
        }

        public ProductListMo.BackgroundImages getBackgroundImages() {
            return backgroundImages;
        }

        public void setBackgroundImages(ProductListMo.BackgroundImages backgroundImages) {
            this.backgroundImages = backgroundImages;
        }
    }

    public static class BackgroundImages {
        public String getH() {
            return h;
        }

        public void setH(String h) {
            this.h = h;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

        private String h;
        private String v;
    }
}
