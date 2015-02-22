
package info.freelibrary.djatoka.iiif;

import info.freelibrary.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link javax.servlet.Filter} that parsing incoming IIIF requests for FreeLib-Djatoka.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class IIIFServletFilter implements Filter, Constants {

    private static final String CONTENT_TYPE_KEY = "IIIF_CONTENT_TYPE";

    private static final Logger LOGGER = LoggerFactory.getLogger(IIIFServletFilter.class);

    private FilterConfig myFilterConfig;

    /**
     * Destroys the {@link javax.servlet.Filter}.
     */
    public void destroy() {
        myFilterConfig = null;
    }

    /**
     * Performs the check for IIIF requests and parsing of the request's contents if it's found.
     * 
     * @param aRequest The servlet request that might contain an IIIF request
     * @param aResponse The servlet response that might contain an IIIF response
     */
    public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aFilterChain)
            throws IOException, ServletException {
        ServletContext context = myFilterConfig.getServletContext();
        String servicePrefix;

        // The service prefix can come from a context path or a filter config
        servicePrefix = myFilterConfig.getInitParameter("prefix");

        if (servicePrefix == null) {
            // Per IIIF spec, this is "The path on the host server to the service" and needs to include
            // both contextPath of the webapp and also the servlet path within that contextPath.
            servicePrefix = context.getContextPath();
        }

        if (aRequest instanceof HttpServletRequest) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking for an IIIF request");
            }

            HttpServletRequest request = (HttpServletRequest) aRequest;
            URL url = new URL(request.getRequestURL().toString());
            IIIFRequest iiif;

            try {
                if (hasServicePrefix(servicePrefix)) {
                    iiif = IIIFRequest.Builder.getRequest(url, servicePrefix);
                } else {
                    iiif = IIIFRequest.Builder.getRequest(url);
                }

                if (iiif.hasExtension()) {
                    String extension = iiif.getExtension();

                    if (extension.equals("xml")) {
                        aResponse.setCharacterEncoding(DEFAULT_CHARSET);
                        aResponse.setContentType(XML_CONTENT_TYPE);
                        aRequest.setAttribute(CONTENT_TYPE_KEY, XML_CONTENT_TYPE);
                    } else if (extension.equals("json")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, JSON_CONTENT_TYPE);
                        aResponse.setContentType(JSON_CONTENT_TYPE);
                    } else if (extension.equals("jpg")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, JPG_CONTENT_TYPE);
                        aResponse.setContentType(JPG_CONTENT_TYPE);
                    } else if (extension.equals("gif")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, GIF_CONTENT_TYPE);
                        aResponse.setContentType(GIF_CONTENT_TYPE);
                    } else if (extension.equals("jp2")) {
                        aRequest.setAttribute(CONTENT_TYPE_KEY, JP2_CONTENT_TYPE);
                        aResponse.setContentType(JP2_CONTENT_TYPE);
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
        Arrays.sort(CONTENT_TYPES); // so we can binary search across them
        myFilterConfig = aFilterConfig;
    }

    private boolean hasServicePrefix(String aContextName) {
        return aContextName != null && !aContextName.equals("/") && !aContextName.equals(""); // variations necessary?
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
