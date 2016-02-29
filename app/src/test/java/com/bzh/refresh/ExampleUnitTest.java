package com.bzh.refresh;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    public static final int MODE_NONE = 0x1;
    public static final int MODE_SETUP_1 = MODE_NONE << 1;
    public static final int MODE_SETUP_2 = MODE_SETUP_1 << 1;
    public static final int MODE_SETUP_3 = MODE_SETUP_2 << 1;
    public static final int MODE_SETUP_4 = MODE_SETUP_3 << 1;

    @Test
    public void addition_isCorrect() throws Exception {
        System.out.println(MODE_NONE);
        System.out.println(MODE_SETUP_1);
        System.out.println(MODE_SETUP_2);
        System.out.println(MODE_SETUP_3);
        System.out.println(MODE_SETUP_4);
    }
}