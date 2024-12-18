/*
 * The MIT License
 *
 * Copyright (c) 2013 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package htsjdk.tribble.util;

import htsjdk.io.IOPath;
import htsjdk.samtools.seekablestream.SeekablePathStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

/**
 * @author jrobinso
 */
public class ParsingUtils {

    public static final Map<Object, Color> colorCache = new WeakHashMap<>(100);

    private static URLHelperFactory urlHelperFactory = RemoteURLHelper::new;

    // HTML 4.1 color table,  + orange and magenta
    private static final Map<String, String> colorSymbols = new HashMap<>();

    static {
        colorSymbols.put("white", "FFFFFF");
        colorSymbols.put("silver", "C0C0C0");
        colorSymbols.put("gray", "808080");
        colorSymbols.put("black", "000000");
        colorSymbols.put("red", "FF0000");
        colorSymbols.put("maroon", "800000");
        colorSymbols.put("yellow", "FFFF00");
        colorSymbols.put("olive", "808000");
        colorSymbols.put("lime", "00FF00");
        colorSymbols.put("green", "008000");
        colorSymbols.put("aqua", "00FFFF");
        colorSymbols.put("teal", "008080");
        colorSymbols.put("blue", "0000FF");
        colorSymbols.put("navy", "000080");
        colorSymbols.put("fuchsia", "FF00FF");
        colorSymbols.put("purple", "800080");
        colorSymbols.put("orange", "FFA500");
        colorSymbols.put("magenta", "FF00FF");
    }

    /**
     * @return an input stream from the given path
     * @throws IOException
     */
    public static InputStream openInputStream(String path)
            throws IOException {
        return openInputStream(path, null);
    }

    /**
     * open an input stream from the given path and wrap the raw byte stream with a wrapper if given
     *
     * the wrapper will only be applied to paths that are
     *   1. not local files
     *   2. not being handled by the legacy http(s)/ftp providers
     *  i.e. any {@link java.nio.file.Path} using a custom FileSystem plugin
     * @param uri a uri like string
     * @param wrapper to wrap the input stream in, may be used to implement caching or prefetching, etc
     * @return An inputStream appropriately created from uri and conditionally wrapped with wrapper (only in certain cases)
     * @throws IOException when stream cannot be opened against uri
     */
    public static InputStream openInputStream(final String uri, final Function<SeekableByteChannel, SeekableByteChannel> wrapper)
            throws IOException {
        final IOPath path = null;
        if(path.hasFileSystemProvider()){
            if(path.isPath()) {
                return path.getScheme().equals("file")
                        ? Files.newInputStream(path.toPath())
                        : new SeekablePathStream(path.toPath(), wrapper);
            } else {
                throw new IOException("FileSystemProvider for path " + path.getRawInputString() + " exits but failed to " +
                        " create path. \n" + path.getToPathFailureReason());
            }
        } else if( SeekableStreamFactory.canBeHandledByLegacyUrlSupport(uri)){
            return getURLHelper(new URL(uri)).openInputStream();
        } else {
            throw new IOException("No FileSystemProvider available to handle path: " + path.getRawInputString());
        }
    }

    public static <T> String join(String separator, Collection<T> objects) {
        if (objects.isEmpty()) {
            return "";
        }
        Iterator<T> iter = objects.iterator();
        final StringBuilder ret = new StringBuilder(iter.next().toString());
        while (iter.hasNext()) {
            ret.append(separator);
            ret.append(iter.next().toString());
        }

        return ret.toString();
    }

    /**
     * a small utility function for sorting a list
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends Comparable> List<T> sortList(Collection<T> list) {
        ArrayList<T> ret = new ArrayList<>();
        ret.addAll(list);
        Collections.sort(ret);
        return ret;
    }

    public static <T extends Comparable<T>, V> String sortedString(Map<T, V> c) {
        List<T> t = new ArrayList<>(c.keySet());
        Collections.sort(t);

        List<String> pairs = new ArrayList<>();
        for (T k : t) {
            pairs.add(k + "=" + c.get(k));
        }

        return "{" + ParsingUtils.join(", ", pairs.toArray(new String[pairs.size()])) + "}";
    }

    /**
     * join an array of strings given a seperator
     *
     * @param separator the string to insert between each array element
     * @param strings   the array of strings
     * @return a string, which is the joining of all array values with the separator
     */
    public static String join(String separator, String[] strings) {
        return join(separator, strings, 0, strings.length);
    }

    /**
     * join a set of strings, using the separator provided, from index start to index stop
     *
     * @param separator the separator to use
     * @param strings   the list of strings
     * @param start     the start position (index in the list)0
     * @param end       the end position (index in the list)
     * @return a joined string, or "" if end - start == 0
     */
    public static String join(String separator, String[] strings, int start, int end) {
        if ((end - start) == 0) {
            return "";
        }
        StringBuilder ret = new StringBuilder(strings[start]);
        for (int i = start + 1; i < end; ++i) {
            ret.append(separator);
            ret.append(strings[i]);
        }
        return ret.toString();
    }


    /**
     * Split the string into tokens separated by the given delimiter. This looks
     * suspiciously like what String.split should do. It is here because
     * String.split has particularly poor performance for this use case in some
     * versions of the Java SE API because of use of java.util.regex APIs
     * (see bug report at http://bugs.java.com/view_bug.do?bug_id=6840246 for
     * information).
     *
     * @param input the string to split
     * @param delim the character that delimits tokens
     * @return a list of the tokens
     */
    public static List<String> split(String input, char delim) {
        if (input.isEmpty()) return Arrays.asList("");
        final ArrayList<String> output = new ArrayList<>(1 + input.length() / 2);
        int from = -1, to;
        for (to = input.indexOf(delim);
             to >= 0;
             from = to, to = input.indexOf(delim, from+1)) {
            output.add(input.substring(from+1, to));
        }
        output.add(input.substring(from+1));
        return output;
    }


    /**
     * Split the string into tokesn separated by the given delimiter.  Profiling has
     * revealed that the standard string.split() method typically takes > 1/2
     * the total time when used for parsing ascii files.
     *
     * @param aString the string to split
     * @param tokens  an array to hold the parsed tokens
     * @param delim   character that delimits tokens
     * @return the number of tokens parsed
     */
    public static int split(String aString, String[] tokens, char delim) {
        return split(aString, tokens, delim, false);
    }

    /**
     * Split the string into tokens separated by the given delimiter.  Profiling has
     * revealed that the standard string.split() method typically takes > 1/2
     * the total time when used for parsing ascii files.
     *
     * @param aString                the string to split
     * @param tokens                 an array to hold the parsed tokens
     * @param delim                  character that delimits tokens
     * @param condenseTrailingTokens if true and there are more tokens than will fit in the tokens array,
     *                               condense all trailing tokens into the last token
     * @return the number of tokens parsed
     */
    public static int split(String aString, String[] tokens, char delim, boolean condenseTrailingTokens) {

        int maxTokens = tokens.length;
        int nTokens = 0;
        int start = 0;
        int end = aString.indexOf(delim);

        if (end == 0) {
            if (aString.length() > 1) {
                start = 1;
                end = aString.indexOf(delim, start);
            } else {
                return 0;
            }
        }

        if (end < 0) {
            tokens[nTokens++] = aString.substring(start);
            return nTokens;
        }

        while ((end > 0) && (nTokens < maxTokens)) {
            tokens[nTokens++] = aString.substring(start, end);
            start = end + 1;
            end = aString.indexOf(delim, start);
        }

        // condense if appropriate
        if (condenseTrailingTokens && nTokens == maxTokens) {
            tokens[nTokens - 1] = tokens[nTokens - 1] + delim + aString.substring(start);
        }
        // Add the trailing string
        else if (nTokens < maxTokens) {
            String trailingString = aString.substring(start);
            tokens[nTokens++] = trailingString;
        }

        return nTokens;
    }


    // trim a string for the given character (i.e. not just whitespace)

    public static String trim(String str, char ch) {
        char[] array = str.toCharArray();
        int start = 0;
        while (start < array.length && array[start] == ch)
            start++;

        int end = array.length - 1;
        while (end > start && array[end] == ch)
            end--;

        return str.substring(start, end + 1);
    }


    /**
     * Split the string into tokens separated by tab or space(s).  This method
     * was added so support wig and bed files, which apparently accept space delimiters.
     * <p/>
     * Note:  TODO REGEX expressions are not used for speed.  This should be re-evaluated with JDK 1.5 or later
     *
     * @param aString the string to split
     * @param tokens  an array to hold the parsed tokens
     * @return the number of tokens parsed
     */
    public static int splitWhitespace(String aString, String[] tokens) {

        int maxTokens = tokens.length;
        int nTokens = 0;
        int start = 0;
        int tabEnd = aString.indexOf('\t');
        int spaceEnd = aString.indexOf(' ');
        int end = tabEnd < 0 ? spaceEnd : spaceEnd < 0 ? tabEnd : Math.min(spaceEnd, tabEnd);
        while ((end > 0) && (nTokens < maxTokens)) {
            //tokens[nTokens++] = new String(aString.toCharArray(), start, end-start); //  aString.substring(start, end);
            tokens[nTokens++] = aString.substring(start, end);

            start = end + 1;
            // Gobble up any whitespace before next token -- don't gobble tabs, consecutive tabs => empty cell
            while (start < aString.length() && aString.charAt(start) == ' ') {
                start++;
            }

            tabEnd = aString.indexOf('\t', start);
            spaceEnd = aString.indexOf(' ', start);
            end = tabEnd < 0 ? spaceEnd : spaceEnd < 0 ? tabEnd : Math.min(spaceEnd, tabEnd);

        }

        // Add the trailing string
        if (nTokens < maxTokens) {
            String trailingString = aString.substring(start);
            tokens[nTokens++] = trailingString;
        }
        return nTokens;
    }

    public static <T extends Comparable<? super T>> boolean isSorted(Iterable<T> iterable) {
        Iterator<T> iter = iterable.iterator();
        if (!iter.hasNext())
            return true;

        T t = iter.next();
        while (iter.hasNext()) {
            T t2 = iter.next();
            if (t.compareTo(t2) > 0)
                return false;

            t = t2;
        }

        return true;
    }

    /**
     * Convert an rgb string, hex, or symbol to a color.
     *
     * @param string
     * @return
     */
    public static Color parseColor(String string) {
        try {
            Color c = colorCache.get(string);
            if (c == null) {
                if (string.contains(",")) {
                    String[] rgb = string.split(",");
                    int red = Integer.parseInt(rgb[0]);
                    int green = Integer.parseInt(rgb[1]);
                    int blue = Integer.parseInt(rgb[2]);
                    c = new Color(red, green, blue);
                } else if (string.startsWith("#")) {
                    c = hexToColor(string.substring(1));
                } else {
                    String hexString = colorSymbols.get(string.toLowerCase());
                    if (hexString != null) {
                        c = hexToColor(hexString);
                    }
                }

                if (c == null) {
                    c = Color.black;
                }
                colorCache.put(string, c);
            }
            return c;

        } catch (NumberFormatException numberFormatException) {
            //TODO Throw this exception?
            return Color.black;
        }
    }


    private static Color hexToColor(String string) {
        if (string.length() == 6) {
            int red = Integer.parseInt(string.substring(0, 2), 16);
            int green = Integer.parseInt(string.substring(2, 4), 16);
            int blue = Integer.parseInt(string.substring(4, 6), 16);
            return new Color(red, green, blue);
        } else {
            return null;
        }

    }

    public static boolean resourceExists(String resource) throws IOException{
        boolean remoteFile = false;
        if (remoteFile) {
            URL url;
            try {
                url = new URL(resource);
            } catch (MalformedURLException e) {
                // Malformed URLs by definition don't exist
                return false;
            }
            URLHelper helper = getURLHelper(url);
            return helper.exists();
        } else if (IOUtil.hasScheme(resource)) {
            return Files.exists(IOUtil.getPath(resource));
        } else {
            return (new File(resource)).exists();
        }
    }

    /**
     * Return a URLHelper from the current URLHelperFactory
     * @see #setURLHelperFactory(URLHelperFactory) 
     *
     * @param url
     * @return
     */
    public static URLHelper getURLHelper(URL url) {
            return urlHelperFactory.getHelper(url);
    }

    /**
     * Set the factory object for providing URLHelpers.  {@see URLHelperFactory}.
     *
     * @param factory
     */
    public static void setURLHelperFactory(URLHelperFactory factory) {
        if(factory == null) {
            throw new NullPointerException("Null URLHelperFactory");
        }
        urlHelperFactory = factory;
    }

    public static URLHelperFactory getURLHelperFactory() {
        return urlHelperFactory;
    }

    /**
     *
     * Add the {@code indexExtension} to the {@code filepath}, preserving
     * query string elements if present. Intended for use where {@code filepath}
     * is a URL. Will behave correctly on regular file paths (just add the extension
     * to the end)
     * @param filepath
     * @param indexExtension
     * @return
     */
    public static String appendToPath(String filepath, String indexExtension) {
        String tabxIndex = null;
        URL url = null;
        try{
            url = new URL(filepath);
        }catch (MalformedURLException e){
            //pass
        }
        if (url != null) {
            String path = url.getPath();
            String indexPath = path + indexExtension;
            tabxIndex = filepath.replace(path, indexPath);
        } else {
            tabxIndex = filepath + indexExtension;
        }
        return tabxIndex;
    }
}
