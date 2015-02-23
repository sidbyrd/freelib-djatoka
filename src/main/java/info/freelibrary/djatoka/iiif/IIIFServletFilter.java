
package info.freelibrary.djatoka.iiif;

import gov.lanl.adore.djatoka.openurl.IReferentResolver;
import gov.lanl.adore.djatoka.openurl.ReferentManager;
import gov.lanl.adore.djatoka.openurl.ResolverException;
import gov.lanl.adore.djatoka.util.IOUtils;
import info.freelibrary.djatoka.view.IdentifierResolver;
import info.freelibrary.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.tools.jstat.Identifier;

/**
 * A {@link javax.servlet.Filter} that parsing incoming IIIF requests for FreeLib-Djatoka.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class IIIFServletFilter implements Filter, Constants {

    private static final String CONTENT_TYPE_KEY = "IIIF_CONTENT_TYPE";

    private static final Logger LOGGER = LoggerFactory.getLogger(IIIFServletFilter.class);

    private static String servicePrefix = null;

    /**
     * Destroys the {@link javax.servlet.Filter}.
     */
    public void destroy() {
        servicePrefix = null;
    }

    /**
     * Performs the check for IIIF requests and parsing of the request's contents if it's found.
     * 
     * @param aRequest The servlet request that might contain an IIIF request
     * @param aResponse The servlet response that might contain an IIIF response
     */
    public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aFilterChain)
            throws IOException, ServletException {

        if (aRequest instanceof HttpServletRequest) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking for an IIIF request");
            }

            HttpServletRequest request = (HttpServletRequest) aRequest;
            URL url = new URL(request.getRequestURL().toString());
            IIIFRequest iiif;

            try {
                iiif = IIIFRequest.Builder.getRequest(url, servicePrefix);

                if (iiif.hasExtension()) {
                    String extension = iiif.getExtension();

                    // in rough order of expected frequency
                    if (extension.equals("jpg")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, JPG_CONTENT_TYPE);
                        aResponse.setContentType(JPG_CONTENT_TYPE);
                    } else if (extension.equals("json")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, JSON_CONTENT_TYPE);
                        aResponse.setContentType(JSON_CONTENT_TYPE);
                    } else if (extension.equals("jp2")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, JP2_CONTENT_TYPE);
                        aResponse.setContentType(JP2_CONTENT_TYPE);
                    } else if (extension.equals("xml")) {
                        aResponse.setCharacterEncoding(DEFAULT_CHARSET);
                        aResponse.setContentType(XML_CONTENT_TYPE);
                        aRequest.setAttribute(CONTENT_TYPE_KEY, XML_CONTENT_TYPE);
                    } else if (extension.equals("gif")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, GIF_CONTENT_TYPE);
                        aResponse.setContentType(GIF_CONTENT_TYPE);
                    } else if (extension.equals("pdf")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, PDF_CONTENT_TYPE);
                        aResponse.setContentType(PDF_CONTENT_TYPE);
                    } else if (extension.equals("tif")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, TIF_CONTENT_TYPE);
                        aResponse.setContentType(TIF_CONTENT_TYPE);
                    } else if (extension.equals("png")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, PNG_CONTENT_TYPE);
                        aResponse.setContentType(PNG_CONTENT_TYPE);
                    } else {
                        throw new RuntimeException("Unexpected extension found: " + extension);
                    }
                } else {
                    String accept = request.getHeader("Accept");
                    String[] values = accept.split(";")[0].split(",");
                    String contentType = getPreferredContentType(values);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Evaluated content type: {}", contentType);
                    }

                    aRequest.setAttribute(CONTENT_TYPE_KEY, contentType);
                    aResponse.setContentType(contentType);
                }

                aRequest.setAttribute(IIIFRequest.KEY, iiif);
                aFilterChain.doFilter(aRequest, aResponse);
            } catch (IIIFException details) {
                HttpServletResponse response = (HttpServletResponse) aResponse;

                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(details.getMessage(), details);
                }

                response.sendError(400, details.getMessage());
            }
        } else {
            aFilterChain.doFilter(aRequest, aResponse);
        }
    }

    /**
     * Initializes the <code>IIIFServletFilter</code> with the supplied {@link javax.servlet.FilterConfig}.
     * 
     * @param aFilterConfig A configuration for the servlet filter
     * @throws ServletException If there is trouble initializing the filter
     */
    public void init(FilterConfig aFilterConfig) throws ServletException {
        ServletContext context = aFilterConfig.getServletContext();
        Arrays.sort(CONTENT_TYPES); // so we can binary search across them

        String iiifPath = aFilterConfig.getInitParameter("prefix");
        if (iiifPath != null) {
            // needs to start with "/" to match the way the lookup below works (unless it's "").
            if (iiifPath.length()>0 && !iiifPath.startsWith("/")) {
                iiifPath = "/"+iiifPath;
            }
        } else  {
            // get the (first) URL that the servlet named "iiifViewer" (in web.xml) is served at, relative to contextPath
            try {
                iiifPath = context.getServletRegistration("iiifViewer").getMappings().iterator().next();
            } catch (UnsupportedOperationException e) {
                LOGGER.error("Unable to get servlet registration for 'iiifViewer': {}", e.getMessage());
            } catch (NoSuchElementException e) {
                LOGGER.error("No registrations found for servlet 'iiifViewer': {}", e.getMessage());
            }
            if (iiifPath == null) {
                // didn't work? Shouldn't fail here, so just fake something.
                iiifPath = "/iiif";
            } else if (iiifPath.endsWith("/*")) {
                iiifPath = iiifPath.substring(0, iiifPath.length()-2);
            }

        }

        // construct servicePrefix including context path.
        // reminder: contextPath is either "/foo" or "" if deployed at server root.
        servicePrefix = context.getContextPath()+iiifPath;
        if (servicePrefix.length()==0) {
            // "" is a legal value, but we represent it as null
            servicePrefix = null;
        } else {
            // if non-empty, will always start with one "/", but for IIIF "Prefix" purposes, remove that.
            servicePrefix = servicePrefix.substring(1);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Filter init using servicePrefix: {}", servicePrefix);
        }

        // make sure the identifier resolver is initialized
        try {
            if (!ReferentManager.isInit()) {
                final InputStream is = getClass().getResourceAsStream("/" + info.freelibrary.djatoka.Constants.PROPERTIES_FILE);
                Properties props = new Properties();
                props.loadFromXML(is);
                ReferentManager.init((IReferentResolver) new IdentifierResolver(), props);
            }
        } catch (IOException e) {
            LOGGER.error("Couldn't open props file from classpath: {}", e.getMessage());
        } catch (ResolverException e) {
            // actually, IdentifierResolver never throws this.
        }
    }

    private String getPreferredContentType(String[] aTypeArray) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Requested content types: {}", StringUtils.toString(aTypeArray, ' '));
        }

        for (int index = 0; index < aTypeArray.length; index++) {
            int found = Arrays.binarySearch(CONTENT_TYPES, aTypeArray[index]);

            if (found >= 0) {
                return CONTENT_TYPES[found];
            }
        }

        return DEFAULT_CONTENT_TYPE;
    }
}
