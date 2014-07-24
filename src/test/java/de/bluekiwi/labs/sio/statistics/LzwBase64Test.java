package de.bluekiwi.labs.sio.statistics;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class LzwBase64Test {

    @Test
    public void simpleCompressionTest() throws UnsupportedEncodingException, IllegalArgumentException {
        assertEquals("Hello, World!", Helper.decompress(Helper.compress("Hello, World!")));
    }

}
