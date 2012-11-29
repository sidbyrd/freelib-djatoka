/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package gov.lanl.adore.djatoka.openurl;

import info.openurl.oom.OpenURLRequest;
import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.OpenURLResponse;
import info.openurl.oom.Transport;
import info.openurl.oom.config.OpenURLConfig;
import gov.lanl.util.AccessManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenURL Servlet - Added referrer and requester to Context Object
 * 
 * @author Jeffrey A. Young
 * @author Ryan Chute
 */
public class OpenURLServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
	    .getLogger(OpenURLServlet.class);
    /**
     * Initial version
     */
    private static final long serialVersionUID = 1L;
    private OpenURLConfig openURLConfig;
    private OpenURLRequestProcessor processor;
    private Transport[] transports;
    private AccessManager am;

    public void init(ServletConfig config) throws ServletException {
	super.init(config);

	try {
	    // load the configuration file from the classpath
	    openURLConfig = new org.oclc.oomRef.config.OpenURLConfig(config);

	    // Construct the configured transports
	    transports = openURLConfig.getTransports();

	    // Construct a processor
	    processor = openURLConfig.getProcessor();

	    ClassLoader cl = OpenURLServlet.class.getClassLoader();
	    java.net.URL url = cl.getResource("access.txt");
	    if (url != null) am = new AccessManager(url.getFile());

	    // A clever hack that allows 'java.library.path' to be set after the
	 	// application has already started (not supposed to be possible!)
	 	// http://blog.cedarsoft.com/2010/11/setting-java-library-path-programmatically/
//	    try {
//	    	String jlp = System.getProperty("java.library.path");
//	    	Field fieldSysPath = cl.getClass().getDeclaredField("sys_paths");
//	    	fieldSysPath.setAccessible(true);
//			fieldSysPath.set(null, null);
//		}
//		catch (Exception details) {
//			if (LOGGER.isWarnEnabled()) {
//				LOGGER.warn("Failed to set java.library.path through hack");
//			}
//		}

	}
	catch (Exception e) {
	    e.printStackTrace();
	    throw new ServletException(e.getMessage(), e);
	}
    }

    /**
     * Extends HttpServlet Request to build OpenURL Request and Context Objects.
     * The req.getHeader("referer") is used to add an OpenURL ReferringEntities
     * and req.getRemoteAddr() is used to add a Requester.
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException {
	try {
	    // Try each Transport until someone takes responsibility
	    OpenURLRequest openURLRequest = null;

	    for (int i = 0; openURLRequest == null && i < transports.length; ++i) {
		openURLRequest = transports[i].toOpenURLRequest(processor, req);
	    }

	    if (openURLRequest == null) {
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
			"Invalid Request");
		return;
	    }

	    // 2009-05-06: rchute Add AccessManager Support
	    if (am != null) {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("OpenURLServlet using AccessManager");
		}

		try {
		    String url = ((java.net.URI) openURLRequest
			    .getContextObjects()[0].getReferent()
			    .getDescriptors()[0]).toASCIIString();

		    if (url.startsWith("http") || url.startsWith("ftp")) {
			if (!am.checkAccess(new URL(url).getHost())) {
			    int status = HttpServletResponse.SC_FORBIDDEN;
			    resp.sendError(status);
			    return;
			}
		    }
		}
		catch (Exception e) {
		    LOGGER.error(e.getMessage(), e);
		}
	    }

	    // rchute: Add referrer for possible extension processing
	    if (req.getHeader("referer") != null) {
		openURLRequest.getContextObjects()[0].getReferringEntities()[0]
			.addDescriptor(req.getHeader("referer"));
	    }

	    // rchute: Add requester for possible extension processing
	    openURLRequest.getContextObjects()[0].getRequesters()[0]
		    .addDescriptor(req.getRemoteAddr());

	    // Process the ContextObjects
	    OpenURLResponse result = processor.resolve(openURLRequest);

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("OpenURLRequestProcessor resolving to a result");
	    }

	    // See if anyone handled the request
	    int status;

	    if (result == null) {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("OpenURLRequestProcessor didn't find result");
		}

		status = HttpServletResponse.SC_NOT_FOUND;
	    }
	    else {
		status = result.getStatus();
		Cookie[] cookies = result.getCookies();

		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("OpenURLRequestProcessor status: {}", status);
		}

		if (cookies != null) {
		    for (int i = 0; i < cookies.length; ++i) {
			resp.addCookie(cookies[i]);
		    }
		}

		Map sessionMap = result.getSessionMap();

		if (sessionMap != null) {
		    HttpSession session = req.getSession(true);
		    Iterator iter = sessionMap.entrySet().iterator();

		    while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			session.setAttribute(key, value);
		    }
		}

		Map headerMap = result.getHeaderMap();

		if (headerMap != null) {
		    Iterator iter = headerMap.entrySet().iterator();

		    while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();

			resp.setHeader((String) entry.getKey(),
				(String) entry.getValue());
		    }
		}
	    }

	    // Allow the processor to generate a variety of response types
	    switch (status) {
	    case HttpServletResponse.SC_MOVED_TEMPORARILY:
		resp.sendRedirect(resp.encodeRedirectURL(result
			.getRedirectURL()));
		break;
	    case HttpServletResponse.SC_SEE_OTHER:
	    case HttpServletResponse.SC_MOVED_PERMANENTLY:
		resp.setStatus(status);
		resp.setHeader("Location", result.getRedirectURL());

		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Redirect URL: {}", result.getRedirectURL());
		}

		break;
	    case HttpServletResponse.SC_NOT_FOUND:
		String id = req.getParameter("rft_id");
		resp.sendError(status, id != null ? id + " not found" : "");

		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("RFT_ID ({}) not found", id);
		}

		break;
	    default:
		OutputStream out = resp.getOutputStream();

		resp.setStatus(status);
		resp.setContentType(result.getContentType());

		InputStream is = result.getInputStream();
		byte[] bytes = new byte[1024];
		int len;

		while ((len = is.read(bytes)) != -1) {
		    out.write(bytes, 0, len);
		}

		out.close();
		break;
	    }
	}
	catch (SocketException e) {
	    LOGGER.error(e.getMessage(), e);
	}
	catch (Throwable e) {
	    LOGGER.debug(e.getMessage(), e);

	    // throw new ServletException(e.getMessage(), e);
	    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException {
	doGet(req, resp);
    }
}
