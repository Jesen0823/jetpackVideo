package com.jesen.cod.jetpackvideo.model;

/*
 *  系统相册获取到的数据
 * */
public class MediaData {
    private String filePath;
    private String mimeType;
    private int width;
    private int height;
    private String resolution;

    public MediaData() {

    }

    public MediaData(String filePath, String mimeType, int width, int height, String resolution) {
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getResolution() {
        return resolution;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}