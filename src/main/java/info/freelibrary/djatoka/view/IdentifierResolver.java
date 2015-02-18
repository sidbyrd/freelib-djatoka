
package info.freelibrary.djatoka.view;

import gov.lanl.adore.djatoka.openurl.DjatokaImageMigrator;
import gov.lanl.adore.djatoka.openurl.IReferentMigrator;
import gov.lanl.adore.djatoka.openurl.IReferentResolver;
import gov.lanl.adore.djatoka.openurl.ResolverException;
import gov.lanl.adore.djatoka.util.ImageRecord;
import info.freelibrary.djatoka.Constants;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
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

    private final List<String> myIngestSources = new CopyOnWriteArrayList<String>();

    private final List<String> myIngestGuesses = new CopyOnWriteArrayList<String>();

    private File myJP2Dir;

    /**
     * Gets the image record for the requested image.
     * 
     * @param aRequest An image request
     * @return An image record
     */
    @Override
    public ImageRecord getImageRecord(final String aRequest) throws ResolverException {
        String decodedRequest = decode(aRequest);

        // make sure id matches an allowed pattern
	    for (String ingestSource : myIngestSources) {
	        final Pattern pattern = Pattern.compile(ingestSource);
	        final Matcher matcher = pattern.matcher(decodedRequest);

	        if (matcher.matches() && matcher.groupCount() > 0) {
				// matched pattern may strip out non-id stuff here
				decodedRequest = matcher.group(1);

				// if we already have the id, easy.
				ImageRecord image = getCachedImage(decodedRequest);
		        if (image != null) {
			        return image;
		        }

				// make id into a URL
				for (String urlPattern : myIngestGuesses) {
					final String url = StringUtils.format(urlPattern, decodedRequest);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Trying to resolve using URL pattern: {}", url);
					}

					// fetch it. If it works, we're done.
					image = getRemoteImage(decodedRequest, url);
					if (image != null) {
						return image;
					}
				}
	        } else if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("No Match in {} for {}", pattern.toString(), decodedRequest);
	        }
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
        final String id = ((URI) aReferent.getDescriptors()[0]).toASCIIString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Translating Referent descriptor into String ID: {}", decode(id));
        }

        return getImageRecord(id);
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
        try {
            if (getImageRecord(aReferentID) != null) {
                return HttpServletResponse.SC_OK;
            } else if (myMigrator.getProcessingList().contains(aReferentID)) {
                return HttpServletResponse.SC_ACCEPTED;
            } else {
                return HttpServletResponse.SC_NOT_FOUND;
            }
        } catch (final ResolverException details) {
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Sets the properties for this identifier resolver.
     * 
     * @param aProps A supplied properties configuration
     */
    @Override
    public void setProperties(final Properties aProps) throws ResolverException {
        final String sources = aProps.getProperty("djatoka.known.ingest.sources");
        final String guesses = aProps.getProperty("djatoka.known.ingest.guesses");

        myJP2Dir = new File(aProps.getProperty(JP2_DATA_DIR));
        myMigrator.setPairtreeRoot(myJP2Dir);
        myRemoteImages = new ConcurrentHashMap<String, ImageRecord>();

        myIngestSources.addAll(Arrays.asList(sources.split("\\s+")));
        myIngestGuesses.addAll(Arrays.asList(guesses.split("\\s+")));
    }

    private ImageRecord getCachedImage(final String aReferentID) {
        ImageRecord image = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking in Pairtree file system for: {}", aReferentID);
        }

        try {
            final PairtreeRoot pairtree = new PairtreeRoot(myJP2Dir);
            final PairtreeObject dir = pairtree.getObject(aReferentID);
            final String filename = PairtreeUtils.encodeID(aReferentID);
            final File file = new File(dir, filename);

            if (file.exists()) {
                image = new ImageRecord();
                image.setIdentifier(aReferentID);
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

    private ImageRecord getRemoteImage(final String aReferent, final String aURL) {
        ImageRecord image = null;

        try {
            final URI uri = new URI(aURL);
            File imageFile;

            // Check to see if it's already in the processing queue
            if (myMigrator.getProcessingList().contains(aReferent)) {
                Thread.sleep(1000);
                int index = 0;

                while (myMigrator.getProcessingList().contains(aReferent) && index < (5 * 60)) {
                    Thread.sleep(1000);
                    index++;
                }

                if (myRemoteImages.containsKey(aReferent)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Retrieving {} from remote images cache", aReferent);
                    }

                    return myRemoteImages.get(aReferent);
                }
            }

            imageFile = myMigrator.convert(aReferent, uri);
            image = new ImageRecord(aReferent, imageFile.getAbsolutePath());

            if (imageFile.length() > 0) {
                myRemoteImages.put(aReferent, image);
            } else {
                throw new ResolverException("An error occurred processing file: " + uri.toURL());
            }
        } catch (final Exception details) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Unable to access {} ({})", aReferent, details.getMessage());
            }

            return null;
        }

        if (LOGGER.isDebugEnabled() && image != null) {
            LOGGER.debug("** Returning JP2 image from getRemoteImage() **");
        }

        return image;
    }

    private String decode(final String aRequest) {
        try {
            final String request = URLDecoder.decode(aRequest, "UTF-8");
            return URLDecoder.decode(request, "UTF-8");
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException("JVM doesn't support UTF-8!!", details);
        }
    }
}
