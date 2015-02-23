package info.freelibrary.djatoka.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA. User: sidb Date: 2/23/15 Time: 2:05 PM To change this template use File | Settings | File
 * Templates.
 */
public class URLEncode {

    /**
     * Applies regular URL encoding to a string once.
     * Note: just because a String is "legally" encoded doesn't mean common web servers will actually
     * accept URLs with them. Before using in a URL, call pathSafetyEncode() as well.
     * @param raw String to encode (in UTF-8)
     * @return encodeed String
     */
    public static String encode(final String raw) {
        try {
            return URLEncoder.encode(raw, "UTF-8");
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException("JVM doesn't support UTF-8!!", details);
        }
    }

    /**
     * Applies regular URL decoding to a string once.
     * @param encoded String to decode
     * @return decoded String (in UTF-8)
     */
    public static String decode(final String encoded) {
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException("JVM doesn't support UTF-8!!", details);
        }
    }

    /**
     * Takes a string that is otherwise safe to use in a URL path, and encodes for a second time
     * certain already-legally-encoded characters that HTTP servers often reject anyway. Specifically,
     * changes %2F to %252F, and %5C to %255C, but leaves / and \ alone, since they *are* path-safe
     * in their unencoded form.
     * Another option would be to just URL encode twice, but that's heavier and uglier.
     * @param pathStr existing safe or already en
     * @return a version of the string safe to use in a URL path (assuming it was already legal).
     */
    public static String pathSafetyEncode (String pathStr) {
        pathStr = pathStr.replace("%2F", "%252F");
        pathStr = pathStr.replace("%5C", "%255C");
        return pathStr;
    }

    /**
     * Takes a string that may have had certain characters double-encoded so web servers won't reject
     * them in the path of a URL, and removes the second encoding of those characters.
     * @param pathStr a path-safed string
     * @return a path *legal* string that may nonetheless be disallowed by web servers
     */
    public static String pathSafetyDecode (String pathStr) {
        pathStr = pathStr.replace("%252F", "%2F");
        pathStr = pathStr.replace("%255C", "%5C");
        return pathStr;
    }
}
