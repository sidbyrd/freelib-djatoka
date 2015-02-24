
package info.freelibrary.djatoka.ingest;

import gov.lanl.adore.djatoka.util.IOUtils;
import info.freelibrary.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Allows ingest jobs to be triggered from a Web interface. It can operate in "unattended" or "attended" (the default)
 * mode. If it runs attended, the ingestion process doesn't finish until the user reloads the page and sees
 * "Finished # ingested." When run in unattended mode, the job finishes when it's done, BUT there is still the final
 * notice available through the web interface; so, when you come after a job that has been run in "unattended" mode,
 * you'll see the notice about "Finished # ingested." You can ignore it and start a new job of your own with a new page
 * reload. Your job has been started when you see, "Ingesting... reload to see progress." A side-effect of this is that
 * when you want to run in unattended mode, you need to make sure the response you get starts with "Ingesting" rather
 * than "Finished" (which doesn't start a new job but notifies you the old one has completed).
 * 
 * @author <a href="mailto:ksclarke@gmail.com>Kevin S. Clarke</a>
 */
public class IngestServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestServlet.class);

    /**
     * IngestServlet's <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 8661545409056868772L;

    @Override
    protected void doGet(final HttpServletRequest aRequest, final HttpServletResponse aResponse) throws IOException,
            ServletException {
        final String runUnattended = aRequest.getParameter("unattended");
        final ServletContext servletContext = getServletContext();

        try {
            final PrintWriter toBrowser = aResponse.getWriter();

            toBrowser.write(ingestFileSystem(runUnattended, servletContext));
            toBrowser.close();
        } catch (final IOException details) {
            aResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, details.getMessage());
        }
    }

    @Override
    public void init(final ServletConfig aServletConfig) throws ServletException {
        super.init(aServletConfig);

        final ServletContext servletContext = aServletConfig.getServletContext();

        try {
            ingestFileSystem("unattended", servletContext);
        } catch (final IOException details) {
            throw new ServletException(details);
        } finally { // clean up our unattended ingest
            servletContext.removeAttribute("ingest");
        }
    }

    private String ingestFileSystem(final String aUnattendedRun, final ServletContext aServletContext)
            throws IOException {
        final String dir = aServletContext.getRealPath("/WEB-INF/classes") + "/";
        final String propertiesFile = dir + info.freelibrary.djatoka.Constants.PROPERTIES_FILE;
        final boolean unattended = aUnattendedRun != null;
        final ServletContext context = getServletContext();
        IngestThread thread = (IngestThread) context.getAttribute("ingest");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Loading properties file: {}", propertiesFile);
        }

        try {
            final Properties p = IOUtils.loadConfigByPath(propertiesFile);
            final String dataDir = p.getProperty("djatoka.ingest.data.dir");
            final String jp2Dir = p.getProperty("djatoka.ingest.jp2.dir");
            final String extString = p.getProperty("djatoka.ingest.data.exts");
            final File src = new File(dataDir);
            final File dest = new File(jp2Dir);
            final String[] exts = extString.split(","); // TODO: support ; etc?

            if (thread != null) {
                final int count = thread.getCount();
                final StringBuilder data = new StringBuilder(" (");

                data.append(dest.getUsableSpace() / 1024 / 1024);
                data.append(" MB available on the disk)");

                if (thread.isFinished()) {
                    context.removeAttribute("ingest");
                    thread.cleanUp();

                    return StringUtils.format("Finished: {} ingested{}", Integer.toString(count), data.toString());
                }

                return "Ingesting... at number " + count + data;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Looking for '{}' files in {}", StringUtils.toString(exts, ' '), src.getAbsolutePath());
            }

            if (src.exists()) {
                if (!src.isDirectory() || !src.canRead()) {
                    throw new IOException(StringUtils.format("{} cannot be read or is not a dir", src
                            .getAbsolutePath()));
                } else {
                    if (unattended) {
                        thread = new IngestThread(src, dest, exts, p, true);
                    } else {
                        thread = new IngestThread(src, dest, exts, p, false);
                    }

                    thread.start();
                    context.setAttribute("ingest", thread);

                    if (!unattended) {
                        return "Ingesting... reload to see progress";
                    }

                    return ""; // no-one to see it anyway
                }
            } else {
                throw new FileNotFoundException(StringUtils.format("Supplied source directory didn't exist: {}", src
                        .getAbsolutePath()));
            }
        } catch (final Exception details) {
            throw new IOException(details.getMessage(), details);
        }
    }
}
