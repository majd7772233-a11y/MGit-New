package me.sheimi.android.utils;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CodeGuesserTest {

    @org.junit.Test
    public void testGuessCodeType() throws Exception {
        assertEquals("expect to recognise java files", "source.java", CodeGuesser.guessCodeType("test.java"));
        assertEquals("expect to recognise typescript files", "source.ts", CodeGuesser.guessCodeType("test.ts"));
        assertEquals("expect to recognise json files", "source.json", CodeGuesser.guessCodeType("test.json"));
    }
}
