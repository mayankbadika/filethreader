package com.example.filethreader.entity;

public class DBStatus {

    private String fileName;
    private boolean fileStatus;

    public DBStatus(String fileName, boolean fileStatus) {
        this.fileName = fileName;
        this.fileStatus = fileStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(boolean fileStatus) {
        this.fileStatus = fileStatus;
    }
}
