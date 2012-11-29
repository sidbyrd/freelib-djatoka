package info.freelibrary.djatoka.view;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.RegexFileFilter;

import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

public class StatsCompilation implements Constants {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(StatsCompilation.class);

    private String myJP2sSize;
    private String myJP2sCount;
    private String myTIFsSize;
    private String myTIFsCount;

    public StatsCompilation(File aTIFDir, File aJP2Dir) {
	RegexFileFilter jp2Pattern = new RegexFileFilter(JP2_FILE_PATTERN);
	RegexFileFilter tifPattern = new RegexFileFilter(TIFF_FILE_PATTERN);
	long jp2CountLong = 0;
	long tifCountLong = 0;

	if (!aJP2Dir.exists() && !aJP2Dir.mkdirs() && LOGGER.isWarnEnabled()) {
	    LOGGER.warn("Couldn't create requested JP2 directory: {}", aJP2Dir);
	}

	try {
	    File[] jp2Files = FileUtils.listFiles(aJP2Dir, jp2Pattern, true);
	    File[] tifFiles = FileUtils.listFiles(aTIFDir, tifPattern, true);

	    // These two just count the size of the files, not directories
	    for (File file : jp2Files) {
		jp2CountLong += file.length();
	    }

	    for (File file : tifFiles) {
		tifCountLong += file.length();
	    }

	    myJP2sSize = FileUtils.sizeFromBytes(jp2CountLong, true);
	    myTIFsSize = FileUtils.sizeFromBytes(tifCountLong, true);
	    myJP2sCount = Integer.toString(jp2Files.length);
	    myTIFsCount = Integer.toString(tifFiles.length);

	    if (LOGGER.isDebugEnabled()) {
		String[] tifStats = new String[] { myTIFsCount, myTIFsSize };
		String[] jp2Stats = new String[] { myJP2sCount, myJP2sSize };

		LOGGER.debug("TIF file count (size): {} ({})", tifStats);
		LOGGER.debug("JP2 file count (size): {} ({})", jp2Stats);
	    }
	}
	catch (Exception details) {
	    if (LOGGER.isErrorEnabled()) {
		LOGGER.error(details.getMessage(), details);
	    }
	}
    }

    public void save(File aStatsFile) {
	try {
	    FileOutputStream outStream = new FileOutputStream(aStatsFile);
	    Serializer serializer = new Serializer(outStream);
	    Element root = new Element("djatoka");
	    Element jp2Elem = new Element("jp2s");
	    Element tifElem = new Element("tifs");

	    jp2Elem.addAttribute(new Attribute(JP2_SIZE_ATTR, myJP2sSize));
	    jp2Elem.addAttribute(new Attribute(JP2_COUNT_ATTR, myJP2sCount));
	    tifElem.addAttribute(new Attribute(TIF_SIZE_ATTR, myTIFsSize));
	    tifElem.addAttribute(new Attribute(TIF_COUNT_ATTR, myTIFsCount));

	    root.appendChild(jp2Elem);
	    root.appendChild(tifElem);
	    serializer.write(new Document(root));
	}
	catch (Exception details) {
	    if (LOGGER.isErrorEnabled()) {
		LOGGER.error(details.getMessage(), details);
	    }
	}
    }

    public String getJP2sSize() {
	return myJP2sSize;
    }

    public String getJP2sCount() {
	return myJP2sCount;
    }

    public String getTIFsSize() {
	return myTIFsSize;
    }

    public String getTIFsCount() {
	return myTIFsCount;
    }

}
