package com.digitallib.utils;

public class ReferenceUtils {


    public static String getExtendedMonth(Integer mes) {
        String[] monthArray = {"jan.", "fev.", "mar.", "abr.", "maio.", "jun.", "jul.", "ago.", "set.", "out.", "nov.", "dez."};
        return monthArray[mes-1];
    }
}
