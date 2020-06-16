package com.easyetz.bean;

public class languageEntity {
    private int lid;
    private String language;

    public languageEntity(int id, String language) {
        this.lid = id;
        this.language = language;
    }

    public int getLid() {
        return lid;
    }

    public String getLanguage() {
        return language;
    }
}
