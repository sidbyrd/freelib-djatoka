package info.freelibrary.djatoka.ingest;

import gov.lanl.adore.djatoka.DjatokaCompress;
import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;

import info.freelibrary.util.DirFileFilter;
import info.freelibrary.util.FileExtFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestThread extends Thread {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(IngestThread.class);

    private static final String JP2_EXT = ".jp2";

    private static final String MAX_SIZE = "djatoka.ingest.file.maxSize";

    private static boolean isFinished;
    private static boolean isWaiting;
    private static int myCount;

    private DjatokaEncodeParam myParams;
    private ICompress myCompression;
    private String[] myExts;
    private long myMaxSize;
    private File mySource;
    private File myDest;

    public IngestThread(File aSource, File aDest, String[] aExts,
	    Properties aCfg) throws Exception {
	super();

	mySource = aSource;
	myDest = aDest;
	myExts = aExts;

	myParams = new DjatokaEncodeParam(aCfg);
	myCompression = new KduCompressExe();

	// Convert maximum file size to bytes
	myMaxSize = Long.parseLong(aCfg.getProperty(MAX_SIZE, "200")) * 1048576;
    }

    @Override
    public void run() {
	super.run();

	isWaiting = true;

	try {
	    convert(mySource, myDest);
	}
	catch (Exception details) {
	    LOGGER.error(details.getMessage(), details);
	}

	isFinished = true;

	while (isWaiting) {
	    try {
		Thread.sleep(1000);
	    }
	    catch (InterruptedException details) {
		if (LOGGER.isWarnEnabled()) {
		    LOGGER.warn(details.getMessage(), details);
		}
	    }
	}
    }

    public int getCount() {
	return myCount;
    }

    public void cleanUp() {
	isWaiting = false;
    }

    public boolean isFinished() {
	return isFinished;
    }

    private void convert(File aSource, File aDest) throws IOException,
	    Exception {
	File[] files = aSource.listFiles(new FileExtFileFilter(myExts));
	File[] dirs = aSource.listFiles(new DirFileFilter());

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Found {} directories in {}", dirs.length, aSource);
	}

	for (File nextSource : dirs) {
	    String fileName = nextSource.getName();

	    if (!fileName.startsWith(".")) {
		File dest = new File(aDest, nextSource.getName());

		if (dest.exists() && (!dest.canWrite() || !dest.isDirectory())) {
		    throw new IOException(
			    "Problem with destination directory: "
				    + dest.getAbsolutePath());
		}
		else {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Descending into new directory: {}", dest);
		    }

		    if ((!dest.exists() && dest.mkdirs())
			    || (dest.exists() && dest.isDirectory())) {
			convert(nextSource, dest);
		    }
		    else {
			throw new IOException(
				"Failed to create a new directory: "
					+ dest.getAbsolutePath());
		    }
		}
	    }
	}

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Found {} files in {}", files.length, aSource);
	}

	for (File nextSource : files) {
	    String fileName = stripExt(nextSource.getName()) + JP2_EXT;
	    String sourceFileName = nextSource.getAbsolutePath();
	    File nextDest = new File(aDest, fileName);

	    if (nextSource.length() > myMaxSize) {
		if (LOGGER.isErrorEnabled()) {
		    LOGGER.error("Source file too large: {} ({} MB)",
			    sourceFileName, nextSource.length() / 1048576);
		}

		continue;
	    }

	    if (!aDest.exists() && !aDest.mkdirs()) {
		throw new IOException("Unable to create new directory: "
			+ nextDest.getAbsolutePath());
	    }

	    if (!fileName.startsWith(".") && !nextDest.exists()) {
		String destFileName = nextDest.getAbsolutePath();

		if (LOGGER.isInfoEnabled()) {
		    LOGGER.info("Compressing {} to {} ({})",
			    new String[] { sourceFileName, destFileName,
				    Integer.toString(myCount + 1) });
		}

		try {
		    DjatokaCompress.compress(myCompression, sourceFileName,
			    destFileName, myParams);

		    myCount += 1;

		    if (LOGGER.isDebugEnabled()) {
			String fileSize = Long.toString(new File(destFileName)
				.length());
			LOGGER.debug("{} written to disk: {}", new String[] {
				destFileName, fileSize });
		    }
		}
		catch (DjatokaException details) {
		    LOGGER.error("Compression of {} (from {}) failed: {}",
			    new String[] { destFileName, sourceFileName,
				    details.getMessage() });

		    if (!new File(destFileName).delete()) {
			LOGGER.error("Could not delete broken file: {}",
				destFileName);
		    }
		}
		catch (NullPointerException details) {
		    LOGGER.error("File ({}) seems to be an empty source image",
			    sourceFileName);
		}
	    }
	}
    }

    private String stripExt(String aFileName) {
	int index;

	if ((index = aFileName.lastIndexOf('.')) != -1) {
	    return aFileName.substring(0, index);
	}

	return aFileName;
    }
}
