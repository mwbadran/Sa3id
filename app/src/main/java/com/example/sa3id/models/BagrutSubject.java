package com.example.sa3id.models;

import com.example.sa3id.BagrutSubjectCategory;

public class BagrutSubject {
    private String name;
    private BagrutSubjectCategory category;
    private boolean isArabicSector;
    private boolean isJewishSector;

    public BagrutSubject(String name, BagrutSubjectCategory category, boolean isArabicSector, boolean isJewishSector) {
        this.name = name;
        this.category = category;
        this.isArabicSector = isArabicSector;
        this.isJewishSector = isJewishSector;
    }

    public String getName() { return name; }
    public BagrutSubjectCategory getCategory() { return category; }
    public boolean isArabicSector() { return isArabicSector; }
    public boolean isJewishSector() { return isJewishSector; }
}

