
package info.freelibrary.djatoka;

public interface Constants {

    public static final String PROPERTIES_FILE = "djatoka-properties.xml";

    public static final String DEFAULT_VIEW_FORMAT = "image/jpeg";

    public static final String DEFAULT_VIEW_EXT = "jpg";

    public static final String DEFAULT_VIEW_LEVEL = "3";

    public static final String VIEW_FORMAT_EXT = "djatoka.view.format.ext";

    public static final String JP2_EXT = ".jp2";

    public static final String TIFF_DATA_DIR = "djatoka.ingest.data.dir";

    public static final String JP2_DATA_DIR = "djatoka.ingest.jp2.dir";

    public static final String VIEW_CACHE_DIR = "djatoka.view.cache.dir";

    public static final String INTERNAL_SERVER = "djatoka.server.internal";

    public static final String REQUIRE_OSD_STYLE = "iiif.require.osdstyle";

    public static final String REQUIRE_LEVELS = "iiif.require.levels";

    public static final String REQUIRE_LEVELS_MIN = "iiif.require.levels.min";

    /**
     * key for 1 or more (space separated) regular expressions that match a Referent and return a URL-path-safe encoded identifier
     */
    public static final String INGEST_VALIDATIONS = "djatoka.ingest.id.validations";

    /**
     * key for 1 or more (space separated) format patterns into which to inject a de-url-encoded identifier to fetch a JP2 image
     */
    public static final String INGEST_HOSTS = "djatoka.ingest.id.hosts";

    /**
     * key for 1 or more (comma separated) file extensions that, if found by ingester, may be converted to JP2
     */
    public static final String TIF_EXTS = "djatoka.ingest.data.exts";

    // Would be nicer to tell the regex filter to be case insensitive
    public static final String TIFF_FILE_PATTERN = "^[^\\.].*\\.(tif|tiff|TIF|TIFF|Tiff|Tif)$";

    // TODO: make case insensitivity an option for the FilenameFilter
    public static final String JP2_FILE_PATTERN = "^[^\\.].*\\.(JP2|jp2|Jp2)$";

    public static final String JP2_SIZE_ATTR = "jp2Size";

    public static final String TIF_SIZE_ATTR = "tifSize";

    public static final String JP2_COUNT_ATTR = "jp2Count";

    public static final String TIF_COUNT_ATTR = "tifCount";

    public static final String MAX_SIZE = "djatoka.ingest.file.maxSize";

}
