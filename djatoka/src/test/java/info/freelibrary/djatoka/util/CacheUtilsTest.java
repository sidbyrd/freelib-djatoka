package info.freelibrary.djatoka.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUtilsTest {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(CacheUtilsTest.class);

    @Test
    public void testGetFileName() {
	String scale = Integer.toString(CacheUtils.getScale(10));
	assertEquals("image_1024_0-0-1023-1023.jpg",
		CacheUtils.getFileName(null, scale, "0,0,1023,1023"));

	// TODO: should this class throw an exception if level AND scale/region
	// are passed to it?
    }

    @Test
    public void testGetMaxLevel() {
	assertEquals(12, CacheUtils.getMaxLevel(2338, 1684));
    }

    @Test
    public void testGetScale() {
	assertEquals(256, (int) CacheUtils.getScale(8));
    }

    @Test
    public void testGetCachingQueries() {
	Object[] actual = CacheUtils.getCachingQueries(2338, 1684).toArray();
	String[] expected = new String[] { "/all/1", "/all/2", "/all/4",
		"/all/8", "/all/16", "/all/32", "/all/64", "/all/128",
		"/all/256", "/0,0,2047,2047/512", "/2048,0,2048,2047/512",
		"/0,0,1023,1023/1024", "/1024,0,1024,1023/1024",
		"/2048,0,1024,1023/1024", "/0,0,511,511/2048",
		"/512,0,512,511/2048", "/1024,0,512,511/2048",
		"/1536,0,512,511/2048", "/2048,0,512,511/2048",
		"/0,0,255,255/4096", "/256,0,256,255/4096",
		"/512,0,256,255/4096", "/768,0,256,255/4096",
		"/1024,0,256,255/4096", "/1280,0,256,255/4096",
		"/1536,0,256,255/4096", "/1792,0,256,255/4096",
		"/2048,0,256,255/4096", "/2304,0,256,255/4096" };

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("actual  : " + Arrays.toString(actual));
	    LOGGER.debug("expected: " + Arrays.toString(expected));
	}

	assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRegion() {
	assertEquals("0,0,1023,1023",
		CacheUtils.getRegion(10, 1684, 2338, 0, 0));
    }

}
