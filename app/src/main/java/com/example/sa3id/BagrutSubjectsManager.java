package com.example.sa3id;

import java.util.ArrayList;
import java.util.List;

public class BagrutSubjectsManager {
    private static List<BagrutSubject> getAllBagrutSubjects() {
        List<BagrutSubject> BagrutSubjects = new ArrayList<>();

        // mandatory
        BagrutSubjects.add(new BagrutSubject("אזרחות", BagrutSubjectCategory.MANDATORY, true, true));
        BagrutSubjects.add(new BagrutSubject("אנגלית", BagrutSubjectCategory.MANDATORY, true, true));
        BagrutSubjects.add(new BagrutSubject("מתמטיקה", BagrutSubjectCategory.MANDATORY, true, true));
        BagrutSubjects.add(new BagrutSubject("היסטוריה/תע\"י", BagrutSubjectCategory.MANDATORY, true, true));
        BagrutSubjects.add(new BagrutSubject("הבעה עברית", BagrutSubjectCategory.MANDATORY, false, true));
        BagrutSubjects.add(new BagrutSubject("ספרות", BagrutSubjectCategory.MANDATORY, false, true));
        BagrutSubjects.add(new BagrutSubject("תנ\"ך", BagrutSubjectCategory.MANDATORY, false, true));
        BagrutSubjects.add(new BagrutSubject("ערבית", BagrutSubjectCategory.MANDATORY, true, false));
        BagrutSubjects.add(new BagrutSubject("עברית", BagrutSubjectCategory.MANDATORY, true, false));
        BagrutSubjects.add(new BagrutSubject("מורשת ודת", BagrutSubjectCategory.MANDATORY, true, false));


        String[] optionalBagrutSubjects = {
                "אלקטרוניקה",
                "אמנות",
                "ביולוגיה",
                "גיאוגרפיה",
                "חקלאות",
                "חשמל",
                "כימיה",
                "מדעי החברה",
                "מדעי המחשב",
                "מוסיקה",
                "מחשבת ישראל",
                "מכשור ובקרה",
                "מכניקה הנדסית",
                "פיזיקה",
                "פסיכולוגיה",
                "צרפתית",
                "שפה זרה אחרת",
                "תורה שבע\"פ",
                "תלמוד"
        };

        for (String BagrutSubject : optionalBagrutSubjects) {
            BagrutSubjects.add(new BagrutSubject(BagrutSubject, BagrutSubjectCategory.OPTIONAL, true, true));
        }

        return BagrutSubjects;
    }

    public static List<BagrutSubject> getBagrutSubjectsForSector(boolean isArabicSector) {
        List<BagrutSubject> allBagrutSubjects = getAllBagrutSubjects();
        List<BagrutSubject> sectorBagrutSubjects = new ArrayList<>();

        for (BagrutSubject BagrutSubject : allBagrutSubjects) {
            if ((isArabicSector && BagrutSubject.isArabicSector()) ||
                    (!isArabicSector && BagrutSubject.isJewishSector())) {
                sectorBagrutSubjects.add(BagrutSubject);
            }
        }

        return sectorBagrutSubjects;
    }
}