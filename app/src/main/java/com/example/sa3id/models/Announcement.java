package com.example.sa3id.models;

public class Announcement {
    private String title;
    private String description;
    private int imageResource;
    //Bitmap image;


    public Announcement(String title, String description, int imageResource) {
        this.title = title;
        this.description = description;
        //this.image = image;
        this.imageResource = imageResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
