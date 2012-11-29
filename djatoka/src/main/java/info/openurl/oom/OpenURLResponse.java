/**
 * 
 */
package info.openurl.oom;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.Cookie;

/**
 * Service classes are expected to return an instance of
 * this class. It is basically a holding area for the
 * HttpServletResponse assignments. In theory, this
 * model could be designed to pass the HttpServletResponse
 * directly into the Service classes, but that would
 * make those Services harder to call from within Java. 
 * 
 * @author Jeffrey A. Young
 * @see info.openurl.oom.Service
 */
public class OpenURLResponse {
    private int status;
    private String redirectURL;
    private String contentType;
    private InputStream is;
    private ArrayList cookies = new ArrayList();
    private Map sessionMap;
    private Map headerMap;

    /**
     * Construct an HTTP response proxy.
     * @param status HTTP status code
     * @param redirectURL HTTP redirect URL
     * @param contentType HTTP contentType
     * @param bytes HTTP response message
     * @deprecated
     */
    public OpenURLResponse(int status,
            String redirectURL,
            String contentType,
            byte[] bytes) {
        this(status, redirectURL, contentType, new ByteArrayInputStream(bytes));
    }

    /**
     * Construct an HTTP response proxy.
     * @param status HTTP status code
     * @param redirectURL HTTP redirect URL
     * @param contentType HTTP contentType
     * @param bytes HTTP response message
     * @deprecated
     */
    public OpenURLResponse(int status,
            URL redirectURL,
            String contentType,
            byte[] bytes) {
        this(status, (redirectURL != null) ? redirectURL.toString() : null, contentType, new ByteArrayInputStream(bytes));
    }

    /**
     * Construct an HTTP response proxy.
     * @param status HTTP status code
     * @param redirectURL HTTP redirect URL
     * @param contentType HTTP contentType
     * @param is 
     * @deprecated
     */
    public OpenURLResponse(int status, URL redirectURL, String contentType,
            InputStream is) {
        this(status, (redirectURL != null) ? redirectURL.toString() : null, contentType, is);
    }
    
    /**
     * Constructs a proxy for an HTTP response
     * 
     * @param status
     */
    public OpenURLResponse(int status) {
        this(status, null, (Map) null);
    }
    
    /**
     * Constructs a proxy for an HTTP redirect response
     * 
     * @param status
     * @param redirectURL
     */
    public OpenURLResponse(int status, String redirectURL) {
        this(status, redirectURL, (Map) null);
    }
    
    /**
     * @param status
     * @param redirectURL
     * @param sessionMap
     */
    public OpenURLResponse(int status, String redirectURL, Map sessionMap) {
        this.status = status;
        this.redirectURL = redirectURL;
        this.sessionMap = sessionMap;
    }
    
    /**
     * @param status
     * @param redirectURL
     * @param sessionMap
     * @param headerMap 
     */
    public OpenURLResponse(int status, String redirectURL, Map sessionMap,
            Map headerMap) {
        this.status = status;
        this.redirectURL = redirectURL;
        this.sessionMap = sessionMap;
        this.headerMap = headerMap;
    }
    
    /**
     * Constructs a proxy for an HTTP OutputStream response
     * 
     * @param status
     * @param contentType
     * @param is
     */
    public OpenURLResponse(int status, String contentType, InputStream is) {
        this(status, contentType, is, null);
    }
    
    /**
     * @param status
     * @param contentType
     * @param is
     * @param sessionMap
     */
    public OpenURLResponse(int status,
            String contentType,
            InputStream is,
            Map sessionMap) {
        this.status = status;
        this.contentType = contentType;
        this.is = is;
        this.sessionMap = sessionMap;
    }
    
    /**
     * @param status
     * @param contentType
     * @param bytes 
     * @param sessionMap
     * @param headerMap
     */
    public OpenURLResponse(int status,
            String contentType,
            byte[] bytes,
            Map sessionMap,
            Map headerMap) {
        this.status = status;
        this.contentType = contentType;
        this.is = new ByteArrayInputStream(bytes);
        this.sessionMap = sessionMap;
        this.headerMap = headerMap;
    }

    /**
     * Constructs a proxy for an HTTP OutputStream response
     * 
     * @param status
     * @param contentType
     * @param bytes
     */
    public OpenURLResponse(int status, String contentType, byte[] bytes) {
        this(status, contentType, new ByteArrayInputStream(bytes));
    }
    
    /**
     * @param status
     * @param contentType
     * @param bytes
     * @param sessionMap
     */
    public OpenURLResponse(int status, String contentType, byte[] bytes,
            Map sessionMap) {
        this(status, contentType, new ByteArrayInputStream(bytes), sessionMap);
    }
    
    /**
     * Construct an HTTP response proxy.
     * @param status HTTP status code
     * @param redirectURL HTTP redirect URL
     * @param contentType HTTP contentType
     * @param is 
     * @deprecated
     */
    public OpenURLResponse(int status, String redirectURL, String contentType,
            InputStream is) {
        this.status = status;
        this.redirectURL = redirectURL;
        this.contentType = contentType;
        this.is = is;
    }
    
    /**
	 * Proxy for HttpServletResponse.setStatus()
	 * 
	 * @return HttpServletResponse.SC_* codes
	 */
	public int getStatus() {
     return status;   
    }
	
	/**
	 * Proxy for HttpServletResponse.sendRedirect()
	 * 
	 * @return the target URL
	 */
	public String getRedirectURL() {
	    return redirectURL;    
    }
	
	/**
	 * Proxy for HttpServletResponse.setContentType()
	 * 
	 * @return a String specifying the MIME type of the content
	 */
	public String getContentType() {
	    return contentType;   
    }
	    
    /**
     * Add a cookie to the response
     * @param cookie to be returned to the user
     */
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }
    
    /**
     * Proxy for HttpServletResponse.addCookie()
     * 
     * @return a Cookie[] to be returned to the user
     */
    public Cookie[] getCookies() {
        return (Cookie[]) cookies.toArray((Cookie[]) Array.newInstance(Cookie.class, cookies.size()));
    }
    
    /**
     * @return
     */
    public Map getSessionMap() {
        return sessionMap;
    }

    /**
     * @return
     */
    public Map getHeaderMap() {
        return headerMap;
    }
    
    /**
     * Proxy for HttpServletResponse.write()
     * 
     * @return the InputStream to write()
     */
    public InputStream getInputStream() {
        return is;
    }
}