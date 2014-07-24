package de.bluekiwi.labs.sio.statistics;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import com.carrotsearch.hppc.IntArrayList;

public class Helper {
    
    private static final Base64 b64 = new Base64(true);

    /**
     * LZW-compresses and Base64-encodes a String.
     * 
     * @param s The string to be compressed and encoded.
     * @return The LZW-compressed and Base64-encoded string.
     */
    public static String compress(String s) {
        IntArrayList lzwEncodedList = LZW.compress(s);
        StringBuilder lzwEncodedStringBuilder = new StringBuilder();
        
        for (int i=0; i<lzwEncodedList.size(); ++i) {
            lzwEncodedStringBuilder.append("" + (char)lzwEncodedList.get(i));
        }
        
        String lzwEncodedString = lzwEncodedStringBuilder.toString();
        
        return b64.encodeToString(lzwEncodedString.getBytes());
    }
    
    /**
     * Decodes and decompresses a base64-encoded string that has been compressed using LZW.
     * 
     * @param base64String
     * @return The decoded and decompressed string.
     * @throws UnsupportedEncodingException
     */
    public static String decompress(String base64String) throws UnsupportedEncodingException, IllegalArgumentException {
        byte[] lzwEncodedByteArr = b64.decode(base64String);
    
        String lzwEncodedString = new String(lzwEncodedByteArr, "UTF-8");
        IntArrayList lzwArrList = new IntArrayList();
    
        for (int i = 0; i < lzwEncodedString.length(); ++i) {
            lzwArrList.add((int) lzwEncodedString.charAt(i));
        }
    
        String result = LZW.decompress(lzwArrList);
    
        return result;
    }

}
