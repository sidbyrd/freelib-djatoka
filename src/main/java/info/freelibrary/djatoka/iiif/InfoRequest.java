
package info.freelibrary.djatoka.iiif;

import gov.lanl.adore.djatoka.openurl.ReferentManager;
import info.freelibrary.djatoka.view.IdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * An IIIF request for information about an image.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class InfoRequest implements IIIFRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoRequest.class);

    private String myIdentifier;

    private String myExtension;

    private String myServicePrefix;

    /**
     * Returns a <code>InfoRequest</code> for the supplied {@link URL}.
     * 
     * @param aURL A {@link URL} representing the <code>InfoRequest</code>
     */
    public InfoRequest(URL aURL) throws IIIFException {
        this(aURL, null);
    }

    /**
     * Returns a <code>InfoRequest</code> for the supplied {@link URL}.
     * 
     * @param aURL A {@link URL} representing the <code>InfoRequest</code>
     * @param aServicePrefix A pre-configured prefix to use in parsing the request
     */
    public InfoRequest(URL aURL, String aServicePrefix) throws IIIFException {
        myServicePrefix = Builder.checkServicePrefix(aServicePrefix);
        parseExtension(aURL.getPath());
        parseIdentifier(aURL.getPath());
    }

    /**
     * Gets the extension for the request.
     * 
     * @return The extension for the request
     */
    public String getExtension() {
        return myExtension;
    }

    /**
     * Returns true if the request has an extension; else, false
     * 
     * @return True if the request has an extension; else, false
     */
    public boolean hasExtension() {
        return myExtension != null;
    }

    /**
     * Returns the IIIF service prefix.
     * 
     * @return The IIIF service prefix
     */
    public String getServicePrefix() {
        return myServicePrefix;
    }

    /**
     * Returns true if there is an IIIF service prefix; else, false.
     * 
     * @return True if there is an IIIF service prefix; else, false
     */
    public boolean hasServicePrefix() {
        return myServicePrefix != null;
    }

    /**
     * Gets the identifier for the request.
     * 
     * @return The identifier for the request
     */
    public String getIdentifier() {
        return myIdentifier;
    }

    private void parseExtension(String aPath) {
        if (aPath.endsWith(".xml")) {
            myExtension = "xml";
        } else if (aPath.endsWith(".json")) {
            myExtension = "json";
        } else {
            throw new RuntimeException("");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Extension parsed: {}", myExtension);
        }
    }

    private void parseIdentifier(String aPath) throws IIIFException {
        int endIndex = aPath.lastIndexOf("/info."); // A literal from the spec
        String servicePrefixPath = "/"; // First slash for default contextPath
        int startIndex = 1; // To skip the first slash in default contextPaths

        if (myServicePrefix != null) {
            servicePrefixPath += myServicePrefix;
            startIndex = aPath.indexOf(servicePrefixPath);
        }

        if (endIndex == -1) {
            throw new RuntimeException("Improper syntax: " + aPath);
        }

        if (startIndex != -1) {
            if (myServicePrefix != null) {
                startIndex += (servicePrefixPath.length() + 1);
            }

            myIdentifier = aPath.substring(startIndex, endIndex);
            myIdentifier = ((IdentifierResolver) ReferentManager.getResolver()).extractID(myIdentifier);
            if (myIdentifier==null) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Request path '{}' doesn't contain a valid identifier", aPath);
                }

                throw new IIIFException("Request doesn't contain a valid identifier: " + aPath);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Identifier parsed: {}", myIdentifier);
        }
    }
}
