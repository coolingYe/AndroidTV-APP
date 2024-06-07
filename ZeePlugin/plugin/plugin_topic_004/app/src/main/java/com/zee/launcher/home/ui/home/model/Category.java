package com.zee.launcher.home.ui.home.model;

public class Category {

    public Category(String categoryName, String categoryFullName, String categoryImage, int categoryIndex) {
        this.categoryName = categoryName;
        this.categoryFullName = categoryFullName;
        this.categoryImage = categoryImage;
        this.categoryIndex = categoryIndex;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryFullName() {
        return categoryFullName;
    }

    public void setCategoryFullName(String categoryFullName) {
        this.categoryFullName = categoryFullName;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public int getCategoryIndex() {
        return categoryIndex;
    }

    public void setCategoryIndex(int categoryIndex) {
        this.categoryIndex = categoryIndex;
    }

    String categoryName;
    String categoryFullName;
    String categoryImage;
    int categoryIndex;
}
