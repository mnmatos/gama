package com.digitallib.utils;

import org.apache.logging.log4j.core.util.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest {

    @Test
    void getAcronimo() {
        testAcronimo("Esse não é um exemplo e se for: ímpeto", "ENESFI");
        testAcronimo("À Poetisa Alcina Dantas", "APAD");
    }

    private static void testAcronimo(String text, String expected) {
        String acr = TextUtils.getAcronimo(text, "");
        Assertions.assertEquals(expected, acr);
    }
}