package com.example.sa3id;

import android.net.Uri;

public class Material {
    private String title;
    private String type, arabicType;
    private int iconResId;
    private String url;
    private String id;

    public Material(String title, String type, int iconResId) {
        this.title = title;
        this.type = type;
        this.iconResId = iconResId;
    }

    public Material(String name, String type,String arabicType, int icFileDrawable, String url, String id) {
        this.title = name;
        this.type = type;
        this.arabicType = arabicType;
        this.iconResId = icFileDrawable;
        this.url = url;
        this.id = id;
    }

    public String getArabicType() {
        return arabicType;
    }

    public void setArabicType(String arabicType) {
        this.arabicType = arabicType;
    }

    public Material() {}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public int getIconResId() {
        return iconResId;
    }
}