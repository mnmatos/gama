package com.digitallib;

import com.digitallib.manager.RepositoryManager;

public class main {

    public static void main(String[] args) {
        removeEntries(new String[]{"", ""});
    }

    static void removeEntries(String[] entries){
        for (String entry : entries){
            RepositoryManager.removeEntry(entry);
        }
    }


}
