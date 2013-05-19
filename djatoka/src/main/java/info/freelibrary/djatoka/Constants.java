package info.freelibrary.djatoka;

public interface Constants {
	
	public static final String PROPERTIES_FILE = "djatoka.properties";

	public static final String DEFAULT_VIEW_FORMAT = "image/jpeg";
	
	public static final String DEFAULT_VIEW_EXT = "jpg";
	
	public static final String DEFAULT_VIEW_LEVEL = "3";
	
	public static final String VIEW_FORMAT_EXT = "djatoka.view.format.ext";
	
	public static final String JP2_EXT = ".jp2";

	public static final String TIFF_DATA_DIR = "djatoka.ingest.data.dir";

	public static final String JP2_DATA_DIR = "djatoka.ingest.jp2.dir";
	
	public static final String VIEW_CACHE_DIR = "djatoka.view.cache.dir";

	public static final String[] TIF_EXTS = new String[] { "tif", "tiff" };
	
	// Would be nicer to tell the regex filter to be case insensitive
	public static final String TIFF_FILE_PATTERN = "^[^\\.].*\\.(tif|tiff|TIF|TIFF)$";
	
	// TODO: make case insensitivity an option for the FilenameFilter
	public static final String JP2_FILE_PATTERN = "^[^\\.].*\\.(JP2|jp2)$";
	
	public static final String JP2_SIZE_ATTR = "jp2Size";

	public static final String TIF_SIZE_ATTR = "tifSize";

	public static final String JP2_COUNT_ATTR = "jp2Count";

	public static final String TIF_COUNT_ATTR = "tifCount";

}
