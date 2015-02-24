
package info.freelibrary.djatoka.view;

import gov.lanl.adore.djatoka.openurl.DjatokaImageMigrator;
import gov.lanl.adore.djatoka.openurl.IReferentMigrator;
import gov.lanl.adore.djatoka.openurl.IReferentResolver;
import gov.lanl.adore.djatoka.openurl.ResolverException;
import gov.lanl.adore.djatoka.util.ImageRecord;
import info.freelibrary.djatoka.Constants;
import info.freelibrary.djatoka.util.URLEncode;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import info.freelibrary.util.StringUtils;
import info.openurl.oom.entities.Referent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierResolver implements IReferentResolver, Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierResolver.class);

    private final IReferentMigrator myMigrator = new DjatokaImageMigrator();

    private Map<String, ImageRecord> myRemoteImages;

    private final List<Pattern> myIngestIdValidations = new CopyOnWriteArrayList<Pattern>();

    private final List<String> myIngestImageHosts = new CopyOnWriteArrayList<String>();

    private File myJP2Dir;

    /**
     * Extracts and decodes the identifier from the request
     *
     * @param aRequest the request ID
     * @return the portion of aRequest that matches from the validation regexes in the
     *         djatoka.known.ingest.sources property in djatoka-properties.xml, or else
     *         null if there were no matches.
     */
    public String extractID(String aRequest) {
        // turn double-encoded slashes into single-encoded slashes
        aRequest = URLEncode.pathSafetyDecode(aRequest);
        // matching happens at decoded level
        aRequest = URLEncode.decode(aRequest);

        // make sure id matches an allowed pattern
	    for (Pattern pattern : myIngestIdValidations) {
	        final Matcher matcher = pattern.matcher(aRequest);

	        if (matcher.matches() && matcher.groupCount() > 0) {
				// Matched pattern may strip out non-id stuff here.
                // ids are used at the single-encoded level.
				final String id = URLEncode.encode(matcher.group(1));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Match found for {}, id={}", aRequest, id);
                }
                return id;
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No Match in {} for {}", pattern.toString(), aRequest);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("No Matches found for {}. Giving up.", aRequest);
        }
        return null;
    }

    /**
     * Gets the image for the requested id
     *
     * @param id single-encoded identifier, already extracted from request
     * @return An image record, or null if couldn't be found in cache or from configured hosts
     */
    private ImageRecord getImageRecordForId(final String id) {
        // if we already have the id cached, easy.
        ImageRecord image = getCachedImage(id);
        if (image != null) {
            return image;
        }

        // make id into a URL
        for (String urlPattern : myIngestImageHosts) {
            // URL substitution happens at decoded level.
            final String url = StringUtils.format(urlPattern, URLEncode.decode(id));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying to resolve using URL pattern: {}", url);
            }

            // fetch it. If it works, we're done.
            image = getRemoteImage(id, url);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    /**
     * Gets the image record for the requested image.
     * 
     * @param aRequest An image request (path safe encoded identifier)
     * @return An image record
     */
    @Override
    public ImageRecord getImageRecord(final String aRequest) throws ResolverException {
        final String id = extractID(aRequest);
        if (id != null) {
            return getImageRecordForId(id);
        }

	    return null;
    }

    /**
     * Gets an image record for the supplied referent.
     * 
     * @param aReferent A referent for the desired image
     * @return An image record
     */
    @Override
    public ImageRecord getImageRecord(final Referent aReferent) throws ResolverException {
        final String request = ((URI) aReferent.getDescriptors()[0]).toASCIIString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Got Referent descriptor: {}", request);
        }

        return getImageRecord(request);
    }

    /**
     * Gets the referent migrator for this resolver.
     */
    @Override
    public IReferentMigrator getReferentMigrator() {
        return myMigrator;
    }

    /**
     * Gets the HTTP status of the referent ID request.
     * 
     * @param aReferentID The ID of a requested referent
     * @return An HTTP status code
     */
    @Override
    public int getStatus(final String aReferentID) {
        final String id = extractID(aReferentID);

        if (getImageRecordForId(id) != null) {
            return HttpServletResponse.SC_OK;
        } else if (myMigrator.getProcessingList().contains(id)) {
            return HttpServletResponse.SC_ACCEPTED;
        } else {
            return HttpServletResponse.SC_NOT_FOUND;
        }
    }

    /**
     * Sets the properties for this identifier resolver.
     * 
     * @param aProps A supplied properties configuration
     */
    @Override
    public void setProperties(final Properties aProps) throws ResolverException {
        final String idValidations = aProps.getProperty(INGEST_VALIDATIONS);
        final String imageHosts = aProps.getProperty(INGEST_HOSTS);

        myJP2Dir = new File(aProps.getProperty(JP2_DATA_DIR));
        myMigrator.setPairtreeRoot(myJP2Dir);
        myRemoteImages = new ConcurrentHashMap<String, ImageRecord>();

        for (String validation : idValidations.split("\\s+")) {
            myIngestIdValidations.add(Pattern.compile(validation)); // pre-compile regular expressions
        }
        myIngestImageHosts.addAll(Arrays.asList(imageHosts.split("\\s+")));
    }

    /**
     * Returns image from JP2 cache
     * @param id single-encoded identifier
     * @return image, or null if wasn't in cache
     */
    private ImageRecord getCachedImage(final String id) {
        ImageRecord image = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking in Pairtree file system for: {}", id);
        }

        try {
            final PairtreeRoot pairtree = new PairtreeRoot(myJP2Dir);
            final PairtreeObject dir = pairtree.getObject(id);
            final String filename = PairtreeUtils.encodeID(id);
            final File file = new File(dir, filename);

            if (file.exists()) {
                image = new ImageRecord();
                image.setIdentifier(id);
                image.setImageFile(file.getAbsolutePath());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("JP2 found in Pairtree cache: {}", file.getAbsolutePath());
                }
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to find a JP2 in Pairtree cache: {}", pairtree.getAbsolutePath());
            }
        } catch (final IOException details) {
            LOGGER.error("Failed to load file from cache", details);
        }

        if (LOGGER.isDebugEnabled() && image != null) {
            LOGGER.debug("** Returning JP2 image from getCachedImage() **");
        }

        return image;
    }

    private ImageRecord getRemoteImage(final String id, final String aURL) {
        ImageRecord image = null;

        try {
            final URI uri = new URI(aURL);
            File imageFile;

            // Check to see if it's already in the processing queue
            if (myMigrator.getProcessingList().contains(id)) {
                Thread.sleep(1000);
                int index = 0;

                while (myMigrator.getProcessingList().contains(id) && index < (5 * 60)) {
                    Thread.sleep(1000);
                    index++;
                }

                if (myRemoteImages.containsKey(id)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Retrieving {} from remote images cache", id);
                    }

                    return myRemoteImages.get(id);
                }
            }

            imageFile = myMigrator.convert(id, uri);
            image = new ImageRecord(id, imageFile.getAbsolutePath());

            if (imageFile.length() > 0) {
                myRemoteImages.put(id, image);
            } else {
                throw new ResolverException("An error occurred processing file: " + uri.toURL());
            }
        } catch (final Exception details) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Unable to access {} ({})", id, details.getMessage());
            }

            return null;
        }

        if (LOGGER.isDebugEnabled() && image != null) {
            LOGGER.debug("** Returning JP2 image from getRemoteImage() **");
        }

        return image;
    }
}
