package com.digitallib.utils;

import java.util.Arrays;
import java.util.HashSet;

public class TextUtils {
    public static String getAcronimo(String titulo, String defaultValue) {
        if(titulo == null || titulo.isEmpty()) return  defaultValue;

        else {
            HashSet<String> excluido = new HashSet<>(Arrays.asList("uma", "um", " uns", "umas","me", "te","do", "dos", "no", "nos","da", "das", "na", "nas", "de", "o", "a", "e", "as", "os", "nas", "nos", "com", "que", "qual", "quais", "é"));
            StringBuilder initials = new StringBuilder();
            titulo = titulo.replaceAll("[^A-Za-z0-9áàâãéèêíïóôõöúçñÁÀÂÃÉÈÍÏÓÔÕÖÚÇÑ ]","")
                    .replaceAll(" +", " ");
            for (String s : titulo.split(" ")) {
                if(!excluido.contains(s.toLowerCase())) {

                    s = s.replaceAll("[áàâÁÀÂÃ]", "a")
                            .replaceAll("[éèêÉÈ]", "e")
                            .replaceAll("[íïÍÏ]", "i")
                            .replaceAll("[óôõöÓÔÕÖ]", "o")
                            .replaceAll("[úÚ]", "o")
                            .replaceAll("[çÇ]", "c")
                            .replaceAll("[ñÑ]", "n");

                    if (isNumeric(s)){
                        initials.append(s);
                    }
                    else initials.append(new StringBuilder().append(s.charAt(0)).toString().toUpperCase());
                }
            }
            return initials.toString();
        }
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
