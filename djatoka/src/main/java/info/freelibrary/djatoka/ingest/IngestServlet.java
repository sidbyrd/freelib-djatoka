package info.freelibrary.djatoka.ingest;

import gov.lanl.adore.djatoka.util.IOUtils;

import info.freelibrary.djatoka.view.IdentifierResolver;
import info.freelibrary.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(IngestServlet.class);

    /**
     * IngestServlet's <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 8661545409056868772L;

    private static final String PROPERTIES_FILE = "djatoka.properties";

    @Override
    protected void doGet(HttpServletRequest aReq, HttpServletResponse aResp)
	    throws ServletException, IOException {
	String dir = getServletContext().getRealPath("/WEB-INF/classes") + "/";
	String propertiesFile = dir + PROPERTIES_FILE;
	ServletContext context = getServletContext();
	IngestThread thread = (IngestThread) context.getAttribute("ingest");

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Loading properties file: {}", propertiesFile);
	}

	try {
	    Properties props = IOUtils.loadConfigByPath(propertiesFile);
	    String dataDir = props.getProperty("djatoka.ingest.data.dir");
	    String jp2Dir = props.getProperty("djatoka.ingest.jp2.dir");
	    String extString = props.getProperty("djatoka.ingest.data.exts");
	    File source = new File(dataDir);
	    File dest = new File(jp2Dir);
	    String[] exts = extString.split(","); // TODO: support ; etc?

	    if (thread != null) {
		PrintWriter toBrowser = aResp.getWriter();
		int count = thread.getCount();
		StringBuilder data = new StringBuilder(" (");

		data.append(dest.getUsableSpace() / 1024 / 1024);
		data.append(" MB available on the disk)");
		
		if (thread.isFinished()) {
		    toBrowser.write("Finished: " + count + " ingested" + data);
		    new IdentifierResolver().loadFileSystemImages(jp2Dir);
		    context.removeAttribute("ingest");
		    thread.cleanUp();
		}
		else {
		    toBrowser.write("Ingesting... at number " + count + data);
		}

		toBrowser.close();
		return;
	    }

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Loading/processing images from {}", dataDir);
	    }

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Looking for '{}' files",
			StringUtils.toString(exts, ' '));
	    }

	    if (source.exists()) {
		if (!source.isDirectory() || !source.canRead()) {
		    String msg = dataDir + " cannot be read or is not a dir";
		    LOGGER.error(msg, new IOException(dataDir));
		    aResp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		else {
		    thread = new IngestThread(source, dest, exts, props);
		    thread.start();
		    context.setAttribute("ingest", thread);
		}
	    }
	    else if (LOGGER.isWarnEnabled()) {
		String msg = "Supplied source directory didn't exist: "
			+ dataDir;
		LOGGER.warn(msg, new FileNotFoundException(dataDir));
		aResp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
	    }
	}
	catch (Exception details) {
	    throw new IOException(details.getMessage(), details);
	}
    }

}
