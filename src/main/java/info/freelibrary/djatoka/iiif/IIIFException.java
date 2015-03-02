
package info.freelibrary.djatoka.iiif;

import javax.servlet.http.HttpServletResponse;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Like a regular Exception, except for two things:
 * 1) it has a different type, and
 * 2) in addition to wrapping a message, it also wraps an HTTP response code.
 * Thrown when an error page, with the indication response code and message, needs to be served.
 */
public class IIIFException extends Exception {

    private static final long serialVersionUID = -5457634583786308472L;

    /**
     * an HTTPServletResponse.SC_* code for resulting HTTP error page, with default value
     */
    private int httpCode = HttpServletResponse.SC_BAD_REQUEST;

    /**
     * a simple error message to display on resulting HTTP error page instead of full detailed Exception
     * message, if set.
     */
    private String httpMessage = null;

    /**
     * Creates a new <code>IIIFException</code> with no message and the default error code.
     */
    public IIIFException() {
        super();
    }

    /**
     * Creates a new <code>IIIFException</code> using the supplied exception message
     * and the default error code.
     * 
     * @param aMessage The detailed exception message
     */
    public IIIFException(String aMessage) {
        super(aMessage);
    }

    /**
     * Creates a new <code>IIIFException</code> using the supplied error code and exception message.
     *
     * @param httpCode an HTTPServletResponse.SC_* code to use when serving an error page for this Exception
     * @param aMessage The detailed exception message
     */
    public IIIFException(int httpCode, String aMessage) {
        super(aMessage);
        this.httpCode = httpCode;
    }

    /**
     * Creates a new <code>IIIFException</code> with the supplied error code, exception message,
     * and parent exception.
     * 
     * @param httpCode an HTTPServletResponse.SC_* code to use when serving an error page for this Exception
     * @param aMessage The exception message
     * @param aException The exception that was the cause of this exception
     */
    public IIIFException(int httpCode, String aMessage, Exception aException) {
        super(aMessage, aException);
        this.httpCode = httpCode;
    }

    /**
     * Creates a new <code>IIIFException</code> with the supplied error code, HTTP error message,
     * and exception message.
     *
     * @param httpCode an HTTPServletResponse.SC_* code to use when serving an error page for this Exception
     * @param httpMessage a simple error message to use when serving an error page for this Exception, in place of
     *          the detailed logging/info message in aMessage
     * @param aMessage The exception message
     */
    public IIIFException(int httpCode, String httpMessage, String aMessage) {
        super(aMessage);
        this.httpCode = httpCode;
        this.httpMessage = httpMessage;
    }

    /**
     * Creates a new <code>IIIFException</code> with the supplied exception message and parent exception.
     *
     * @param aMessage The exception message
     * @param aException The exception that was the cause of this exception
     */
    public IIIFException(String aMessage, Exception aException) {
        super(aMessage, aException);
    }

    /**
     * Gets this exception's associated HTTPServletResponse.SC_* error code
     * @return the error code
     */
    public int getHttpCode() {
        return httpCode;
    }

    /**
     * Gets this exception's HTTP error page message
     * @return the error code
     */
    public String getHttpMessage() {
        if (httpMessage != null) {
            return httpMessage;
        }
        return getMessage();
    }

    /**
     * Gets this exception's HTTP error code and HTTP error message as a Pair
     * @return Pair with code as key and message as value. If errorMessage was
     * explicitly set, it will be the "value", otherwise the regular exception
     * message will be the "value".
     */
    public Map.Entry<Integer, String> getPair() {
        return new AbstractMap.SimpleImmutableEntry<Integer, String>(httpCode, getHttpMessage());
    }

}
