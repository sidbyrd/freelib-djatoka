
package info.freelibrary.maven;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.freelibrary.djatoka.util.OSDCacheUtil;
import info.freelibrary.util.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Caches tiles for JP2s in FreeLib-Djatoka's Pairtree file system.
 * <p/>
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
@Mojo(name = "cache-tiles")
public class DjatokaTileMojo extends AbstractPairtreeMojo {

    private static final String JETTY_PORT = "jetty.port";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String CACHE_URL = "http://localhost:{}/{}";

    private static final String METADATA_URL =
            "http://localhost:{}/resolve?url_ver=Z39.88-2004&rft_id={}&svc_id=info:lanl-repo/svc/getMetadata";

    private static final String JPEG_CONTENT_TYPE[] = new String[] { "image/jpeg", "image/jpg" };

    private static final Logger LOGGER = LoggerFactory.getLogger(DjatokaIngestMojo.class);

    @Parameter(property = "overwrite", defaultValue = "false")
    private boolean myCacheToBeOverwritten;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String ptfs = myProject.getProperties().getProperty(PAIRTREE_FS);
        final String port = myProject.getProperties().getProperty(JETTY_PORT);
        final OSDCacheUtil tiler = new OSDCacheUtil();

        // Keep track if we started Jetty to load the tiles
        boolean startedJetty = false;

        // Make sure the supplied port number is actually an integer
        try {
            Integer.parseInt(port);
        } catch (final NumberFormatException details) {
            throw new MojoExecutionException(StringUtils.format("Supplied port ({}) must be an integer", port));
        }

        // If server isn't up, start it so we can cache tiles
        if (!serverIsUp(port)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Starting the FreeLib-Djatoka server to cache derivative image tiles");
            }

            try {
                final String message = "FreeLib-Djatoka server must be running to cache tiles";

                if (Runtime.getRuntime().exec(new String[] { "mvn", "jetty:run-forked" }).waitFor() != 0) {
                    throw new MojoFailureException(message);
                }

                if (!serverIsUp(port)) {
                    throw new MojoFailureException(message);
                }

                startedJetty = true;
            } catch (final Exception details) {
                throw new MojoExecutionException(details.getMessage(), details);
            }
        }

        // Sets the Maven loggers' levels (not the levels of loggers used by this plugin)
        MavenUtils.setLogLevels(MavenUtils.ERROR_LOG_LEVEL, MavenUtils.getMavenLoggers());

        try {
            final PairtreeRoot pairtree = new PairtreeRoot(new File(ptfs));
            final RegexFileFilter filter = new RegexFileFilter(".*");
            final String eol = System.getProperty("line.separator");
            final File[] jp2List = FileUtils.listFiles(pairtree, filter, true);

            int processed = 0;

            if (jp2List.length == 0 && LOGGER.isWarnEnabled()) {
                LOGGER.warn("There are no JP2s in the Pairtree structure");
            }

            for (final File file : jp2List) {
                final String id = PairtreeUtils.decodeID(file.getName());
                final URL url = new URL(StringUtils.format(METADATA_URL, port, id));
                final JsonNode json = MAPPER.readTree(url.openStream());

                // Pull out relevant info from our metadata service
                final int width = json.get("width").asInt();
                final int height = json.get("height").asInt();
                final String[] tilePaths = tiler.getPaths("iiif", id, 256, width, height);

                // If cache is to be overwritten, delete what's there so it will be recreated
                if (myCacheToBeOverwritten) {
                    deletePairtreeImageCache(id);
                }

                if (LOGGER.isDebugEnabled()) {
                    final StringBuilder builder = new StringBuilder("Caching tiles for ");

                    builder.append(id).append(eol);

                    // Get URLs OpenSeadragon's IIIF interface will call
                    for (final String path : tilePaths) {
                        processed += cache(port, path);
                        builder.append("  ").append(path).append(eol);
                    }

                    LOGGER.debug(builder.toString());
                } else {
                    for (final String path : tilePaths) {
                        processed += cache(port, path);
                        if (LOGGER.isInfoEnabled() && ++processed % 1000 == 0) {
                            LOGGER.info("{} tiles cached", processed);
                        }
                    }
                }
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("All image tiles were successfully cached");
            }
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        } finally {
            if (startedJetty && serverIsUp(port)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Shutting down the FreeLib-Djatoka after caching image tiles");
                }

                try {
                    if (Runtime.getRuntime().exec(new String[] { "mvn", "jetty:stop" }).waitFor() != 0) {
                        throw new MojoExecutionException("Unable to stop the FreeLib-Djatoka server after tiling");
                    }
                } catch (final IOException details) {
                    throw new MojoExecutionException(details.getMessage(), details);
                } catch (final InterruptedException details) {
                    throw new MojoExecutionException(details.getMessage(), details);
                }
            }
        }
    }

    /**
     * Check that the FreeLib-Djatoka server is up (required for caching our tiles).
     *
     * @param aPort The port at which the server runs
     * @return True if the server responded as expected
     * @throws MojoExecutionException If the server isn't responding as expected
     */
    private boolean serverIsUp(final String aPort) throws MojoExecutionException {
        try {
            final URL url = new URL("http://localhost:" + aPort + "/health");
            final HttpURLConnection http = (HttpURLConnection) url.openConnection();

            if (http.getResponseCode() != 200) {
                throw new MojoExecutionException("FreeLib-Djatoka server is up, but didn't respond as expected");
            }

            http.disconnect();
            return true;
        } catch (final IOException details) {
            return false;
        }
    }

    /**
     * Caches the image using the supplied path and server port.
     *
     * @param aPort The port at which the server runs
     * @param aPath The path of the image to cache
     * @return One if the image was successfully cached
     * @throws MojoExecutionException If there is an error while connecting with the server
     */
    private int cache(final String aPort, final String aPath) throws MojoExecutionException {
        try {
            final URL url = new URL(StringUtils.format(CACHE_URL, aPort, aPath));
            final HttpURLConnection http = (HttpURLConnection) url.openConnection();

            if (http.getResponseCode() != 200 || Arrays.binarySearch(JPEG_CONTENT_TYPE, http.getContentType()) < 0) {
                throw new IOException("Unable to cache tile image: " + aPath);
            }

            return 1;
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }
}
