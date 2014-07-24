package de.bluekiwi.labs.sio.statistics;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;

/**
 * found at: http://rosettacode.org/wiki/LZW_compression
 */
public class LZW {
    public static IntArrayList compress(String uncompressed) {
        return compress(uncompressed, new ObjectIntOpenHashMap<String>());
    }
    
    /**
     * Compress a string to a list of output symbols.
     * 
     * @param uncompressed The string to be compressed.
     * @param dictionary A re-usable buffer for supporting the GC. Will be emptied every time the method is called.
     */
    public static IntArrayList compress(String uncompressed, ObjectIntOpenHashMap<String> dictionary) {
        // Build the dictionary.
        dictionary.clear();
        int dictSize = 256;
        for (int i = 0; i < 256; i++)
            dictionary.put("" + (char)i, i);
 
        String w = "";
        IntArrayList result = new IntArrayList();
        for (char c : uncompressed.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc))
                w = wc;
            else {
                result.add(dictionary.get(w));
                // Add wc to the dictionary.
                dictionary.put(wc, dictSize++);
                w = "" + c;
            }
        }
 
        // Output the code for w.
        if (!w.equals(""))
            result.add(dictionary.get(w));
        return result;
    }
 
    public static String decompress(IntArrayList compressed) {
        return decompress(compressed, new IntObjectOpenHashMap<String>());
    }
    
    /**
     * Decompress a list of output ks to a string.
     *
     * @param compressed The compressed data.
     * @param dictionary A re-usable buffer for supporting the GC. Will be emptied every time the method is called.
     */
    public static String decompress(IntArrayList compressed, IntObjectOpenHashMap<String> dictionary) throws IllegalArgumentException {
        // Build the dictionary.
        dictionary.clear();
        int dictSize = 256;
        for (int i = 0; i < 256; i++)
            dictionary.put(i, "" + (char)i);
 
        StringBuilder resultBuilder = new StringBuilder();
        
        String w = "" + (char)(int)compressed.remove(0);
        resultBuilder.append(w);
        
        for (IntCursor k : compressed) {
            String entry;
            if (dictionary.containsKey(k.value))
                entry = dictionary.get(k.value);
            else if (k.value == dictSize)
                entry = w + w.charAt(0);
            else
                throw new IllegalArgumentException("Bad compressed k: " + k);
 
            resultBuilder.append(entry);
 
            // Add w+entry[0] to the dictionary.
            dictionary.put(dictSize++, w + entry.charAt(0));
 
            w = entry;
        }
        
        return resultBuilder.toString();
    }
 
    public static void main(String[] args) {
        IntArrayList compressed = compress("TOBEORNOTTOBEORTOBEORNOT");
        System.out.println(compressed);
        String decompressed = decompress(compressed);
        System.out.println(decompressed);
    }
}
