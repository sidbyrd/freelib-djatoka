
package info.freelibrary.djatoka.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.lanl.adore.djatoka.openurl.OpenURLJP2KService;
import info.freelibrary.djatoka.Constants;
import info.freelibrary.djatoka.iiif.IIIFRequest;
import info.freelibrary.djatoka.iiif.ImageRequest;
import info.freelibrary.djatoka.iiif.Region;
import info.freelibrary.djatoka.util.CacheUtils;
import info.freelibrary.util.*;
import nu.xom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

public class ImageServlet extends HttpServlet implements Constants {

    /**
     * The <code>ImageServlet</code>'s <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = -4142816720756238591L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageServlet.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String METADATA_URL =
            "http://{}:{}/resolve?url_ver=Z39.88-2004&rft_id={}&svc_id=info:lanl-repo/svc/getMetadata";

    private static final String IMAGE_URL = "/resolve?url_ver=Z39.88-2004&rft_id={}"
            + "&svc_id=info:lanl-repo/svc/getRegion" + "&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000"
            + "&svc.format={}&svc.level={}&svc.rotate={}";

    private static final String REGION_URL = "/resolve?url_ver=Z39.88-2004&rft_id={}"
            + "&svc_id=info:lanl-repo/svc/getRegion" + "&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000"
            + "&svc.format={}&svc.region={}&svc.scale={}&svc.rotate={}";

    private static final String XML_TEMPLATE = "/WEB-INF/metadata.xml";

    private static final String CHARSET = "UTF-8";

    private static String myFormatExt;

    private static String myCache;

    @Override
    protected void doGet(final HttpServletRequest aRequest, final HttpServletResponse aResponse)
            throws ServletException, IOException {
        String level = getServletConfig().getInitParameter("level");
        final IIIFRequest iiif = (IIIFRequest) aRequest.getAttribute(IIIFRequest.KEY);
        final String reqURI = aRequest.getRequestURI();
        final String servletPath = aRequest.getServletPath();
        final String path = reqURI.substring(servletPath.length());
        final String id = getID(path);

        if (reqURI.endsWith("/info.xml") || reqURI.endsWith("/info.json")) {
            try {
                final int[] config = getHeightWidthAndLevels(aRequest, aResponse);
                final ImageInfo info = new ImageInfo(id, config[0], config[1], config[2]);
                final ServletOutputStream outStream = aResponse.getOutputStream();

                if (reqURI.endsWith("/info.xml")) {
                    info.toStream(outStream);
                } else {
                    final StringBuilder serviceSb = new StringBuilder();

                    serviceSb.append(aRequest.getScheme()).append("://");
                    serviceSb.append(aRequest.getServerName()).append(":");
                    serviceSb.append(aRequest.getServerPort());

                    final String service = serviceSb.toString();
                    final String prefix = iiif.getServicePrefix();

                    info.addFormat("jpg"); // FIXME: Configurable options

                    outStream.print(info.toJSON(service, prefix));
                }

                outStream.close();
            } catch (final FileNotFoundException details) {
                aResponse.sendError(HttpServletResponse.SC_NOT_FOUND, id + " not found");
            }
        } else if (iiif != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request is handled via the IIIFRequest shim");
            }

            final ImageRequest imageRequest = (ImageRequest) iiif;
            final String size = imageRequest.getSize().toString();
            final Region iiifRegion = imageRequest.getRegion();
            final float rotation = imageRequest.getRotation();
            String region;

            // Djatoka expects a different order from what OpenSeadragon sends
            // so we have to reconstruct rather than use Region's toString().
            if (iiifRegion.isFullSize()) {
                region = "";
            } else {
                final StringBuilder rsb = new StringBuilder();
                rsb.append(iiifRegion.getY()).append(',');
                rsb.append(iiifRegion.getX()).append(',');
                rsb.append(iiifRegion.getHeight()).append(',');
                rsb.append(iiifRegion.getWidth());

                // Now, we have the string order that Djatoka wants
                region = rsb.toString();
            }

            if (myCache != null) {
                checkImageCache(id, level, size, region, rotation, aRequest, aResponse);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Cache isn't configured correctly");
                }

                serveNewImage(id, level, region, size, rotation, aRequest, aResponse);
            }
        } else {
            // We are using the now deprecated FreeLib-Djatoka djtilesource.js
            final String[] regionCoords = getRegion(path);
            final String scale = getScale(path);
            String region;

            if (level == null && scale == null) {
                level = DEFAULT_VIEW_LEVEL;
            }

            if (regionCoords.length == 4) {
                region = StringUtils.toString(regionCoords, ',');
            } else {
                region = "";
            }

            if (LOGGER.isDebugEnabled()) {
                final StringBuilder request = new StringBuilder();

                request.append("id[").append(id).append("] ");

                if (level != null) {
                    request.append("level[").append(level).append("] ");
                }

                if (scale != null) {
                    request.append("scale[").append(scale).append("] ");
                }

                request.append("region[").append(region).append("]");

                LOGGER.debug("Request: " + request.toString());
            }

            if (myCache != null) {
                // Older freelib-djatoka didn't support rotations; use 0.0f
                checkImageCache(id, level, scale, region, 0.0f, aRequest, aResponse);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Cache isn't configured correctly");
                }

                // Older freelib-djatoka didn't support rotations; use 0.0f
                serveNewImage(id, level, region, scale, 0.0f, aRequest, aResponse);
            }
        }
    }

    @Override
    public void init() throws ServletException {
        final InputStream is = getClass().getResourceAsStream("/" + PROPERTIES_FILE);

        if (is != null) {
            try {
                final Properties props = new Properties();
                props.loadFromXML(is);

                if (props.containsKey(VIEW_CACHE_DIR)) {
                    myCache = props.getProperty(VIEW_CACHE_DIR);
                }

                // If we couldn't get cache from config, fall back to tmpdir
                if (myCache == null) {
                    myCache = System.getProperty("java.io.tmpdir");
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Cache directory set to {}", myCache);
                }

                if (props.containsKey(VIEW_FORMAT_EXT)) {
                    myFormatExt = props.getProperty(VIEW_FORMAT_EXT, DEFAULT_VIEW_EXT);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Format extension set to {}", myFormatExt);
                    }
                }
            } catch (final IOException details) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unable to load properties file: {}", details.getMessage());
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    @Override
    protected void doHead(final HttpServletRequest aRequest, final HttpServletResponse aResponse)
            throws ServletException, IOException {
        try {
            final int[] dimensions = getHeightWidthAndLevels(aRequest, aResponse);

            // TODO: add a content length header too
            if (!aResponse.isCommitted()) {
                aResponse.addIntHeader("X-Image-Height", dimensions[0]);
                aResponse.addIntHeader("X-Image-Width", dimensions[1]);

                aResponse.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (final FileNotFoundException details) {
            aResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected long getLastModified(final HttpServletRequest aRequest) {
        // TODO: really implement this using our cached files?
        return super.getLastModified(aRequest);
    }

    private int[] getHeightWidthAndLevels(final HttpServletRequest aRequest, final HttpServletResponse aResponse)
            throws IOException, ServletException {
        final String reqURI = aRequest.getRequestURI();
        final String servletPath = aRequest.getServletPath();
        final String path = reqURI.substring(servletPath.length());
        int width = 0, height = 0, levels = 0;
        final String id = getID(path);

        if (myCache != null) {
            OutputStream outStream = null;
            InputStream inStream = null;

            try {
                final PairtreeRoot cacheDir = new PairtreeRoot(new File(myCache));
                final PairtreeObject cacheObject = cacheDir.getObject(id);
                final ServletContext context = getServletContext();
                final String filename = PairtreeUtils.encodeID(id);
                final File xmlFile = new File(cacheObject, filename + ".xml");

                if (xmlFile.exists() && xmlFile.length() > 0) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Reading XML metadata file: " + xmlFile.getAbsolutePath());
                    }

                    final Document xml = new Builder().build(xmlFile);
                    final Element root = xml.getRootElement();
                    final Element sElement = root.getFirstChildElement("Size");
                    final String wString = sElement.getAttributeValue("Width");
                    final String hString = sElement.getAttributeValue("Height");
                    final Element lElement = root.getFirstChildElement("Levels");

                    width = wString.equals("") ? 0 : Integer.parseInt(wString);
                    height = hString.equals("") ? 0 : Integer.parseInt(hString);

                    if (lElement != null) {
                        try {
                            levels = Integer.parseInt(lElement.getValue());
                        } catch (final NumberFormatException details) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("{} doesn't look like an integer level", lElement.getValue());
                            }

                            levels = 0;
                        }
                    }

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Returning width/height/levels: {}/{}/{}", width, height, levels);
                    }
                } else {
                    inStream = context.getResource(XML_TEMPLATE).openStream();

                    if (xmlFile.exists()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("XML metadata file exists: {}", xmlFile);
                        }

                        if (!xmlFile.delete() && LOGGER.isWarnEnabled()) {
                            LOGGER.warn("File not deleted: {}", xmlFile);
                        }
                    }

                    outStream = new FileOutputStream(xmlFile);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Creating new xml metadata file: " + xmlFile.getAbsolutePath());
                    }

                    final Document xml = new Builder().build(inStream);
                    final Serializer serializer = new Serializer(outStream);
                    final String encodedID = URLEncoder.encode(id, "UTF-8");

                    try {
                        final String host = aRequest.getLocalName();
                        final String port = Integer.toString(aRequest.getLocalPort());
                        final URL url = new URL(StringUtils.format(METADATA_URL, host, port, encodedID));

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Querying image metadata: {}", url);
                        }

                        final JsonNode json = MAPPER.readTree(url.openStream());

                        // Pull out relevant info from our metadata service
                        width = json.get("width").asInt();
                        height = json.get("height").asInt();
                        levels = json.get("levels").asInt();

                        final Element root = xml.getRootElement();
                        final Element sElement = root.getFirstChildElement("Size");
                        final Attribute wAttribute = sElement.getAttribute("Width");
                        final Attribute hAttribute = sElement.getAttribute("Height");
                        final Element lElement = root.getFirstChildElement("Levels");

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Width: {}; Height: {}; Level: {}", width, height, levels);
                        }

                        // Save it in our xml file for easier access next time
                        wAttribute.setValue(Integer.toString(width));
                        hAttribute.setValue(Integer.toString(height));
                        lElement.appendChild(Integer.toString(levels));

                        serializer.write(xml);
                        serializer.flush();
                    } catch (final IIOException details) {
                        if (details.getCause().getClass().getSimpleName().equals("FileNotFoundException")) {
                            throw new FileNotFoundException(id + " not found");
                        } else {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("[{}] " + details.getMessage(), encodedID, details);
                            }

                            throw details;
                        }
                    }
                }
            } catch (final ValidityException details) {
                aResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, details.getMessage());
            } catch (final ParsingException details) {
                aResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, details.getMessage());
            } finally {
                IOUtils.closeQuietly(outStream);
                IOUtils.closeQuietly(inStream);
            }
        } else {
            // TODO: work around rather than throwing an exception
            throw new ServletException("Cache not correctly configured");
        }

        return new int[] { height, width, levels };
    }

    private void checkImageCache(final String aID, final String aLevel, final String aScale, final String aRegion,
            final float aRotation, final HttpServletRequest aRequest, final HttpServletResponse aResponse)
            throws IOException, ServletException {
        final PairtreeRoot cacheDir = new PairtreeRoot(new File(myCache));
        final PairtreeObject cacheObject = cacheDir.getObject(aID);
        final String fileName = CacheUtils.getFileName(aLevel, aScale, aRegion, aRotation);
        final File imageFile = new File(cacheObject, fileName);

        if (imageFile.exists()) {
            final ServletOutputStream outStream = aResponse.getOutputStream();

            aResponse.setHeader("Content-Length", "" + imageFile.length());
            aResponse.setHeader("Cache-Control", "public, max-age=4838400");
            aResponse.setContentType("image/jpg");

            IOUtils.copyStream(imageFile, outStream);
            IOUtils.closeQuietly(outStream);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} served from Pairtree cache", imageFile);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} not found in cache", imageFile);
            }

            serveNewImage(aID, aLevel, aRegion, aScale, aRotation, aRequest, aResponse);
            cacheNewImage(aRequest, aID + "_" + fileName, imageFile);
        }
    }

    private void serveNewImage(final String aID, final String aLevel, final String aRegion, final String aScale,
            final float aRotation, final HttpServletRequest aRequest, final HttpServletResponse aResponse)
            throws IOException, ServletException {
        final String id = URLEncoder.encode(aID, CHARSET);
        RequestDispatcher dispatcher;
        String[] values;
        String url;

        // Cast floats as integers because that's what djatoka expects
        if (aScale == null) {
            values = new String[] { id, DEFAULT_VIEW_FORMAT, aLevel, Integer.toString((int) aRotation) };
            url = StringUtils.format(IMAGE_URL, values);
        } else {
            values =
                    new String[] { id, DEFAULT_VIEW_FORMAT, aRegion, aScale.equals("full") ? "1.0" : aScale,
                        Integer.toString((int) aRotation) };
            url = StringUtils.format(REGION_URL, values);
        }

        // Right now we just let the OpenURL interface do the work
        dispatcher = aRequest.getRequestDispatcher(url);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Image requested: {} - {}", aID, url);
        }

        dispatcher.forward(aRequest, aResponse);
    }

    private void cacheNewImage(final HttpServletRequest aRequest, final String aKey, final File aDestFile) {
        final HttpSession session = aRequest.getSession();
        final String fileName = (String) session.getAttribute(aKey);

        if (fileName != null) {
            final String cacheName = (String) session.getAttribute(fileName);
            final File cachedFile = new File(fileName);

            // This moves the newly created file from the adore-djatoka cache
            // to the freelib-djatoka cache (which is pure-FS/Pairtree-based)
            if (cachedFile.exists() && aDestFile != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Renaming cache file from {} to {}", cachedFile, aDestFile);
                }

                if (!cachedFile.renameTo(aDestFile) && LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to move cache file: {}", cachedFile);
                } else {
                    // This is the temp file cache used by the OpenURL layer
                    if (!OpenURLJP2KService.removeFromTileCache(cacheName) && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Unable to remove OpenURL cache file link: {}", fileName);
                    } else {
                        session.removeAttribute(aKey);
                        session.removeAttribute(fileName);
                    }
                }
            } else if (LOGGER.isWarnEnabled() && !cachedFile.exists()) {
                LOGGER.warn("Session had a cache file ({}), but it didn't exist", cachedFile.getAbsoluteFile());
            } else if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Location for destination cache file was null");
            }
        } else if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Couldn't cache ({} = {}); session lacked new image information", aKey, aDestFile
                    .getAbsolutePath());
        }
    }

    private String getID(String path) {
	    // remove leading "/"
		int startPos = (path.startsWith("/"))? 1 : 0;

	    // remove trailing "/info.*"
	    int endPos = path.lastIndexOf("/info.");
	    if (endPos <= startPos) {
			// or else remove trailing "/*/*/*/native.jpg"
		    int count = 0;
			for (endPos = path.lastIndexOf("/native.jpg"); endPos > startPos && count<3; count++) {
				endPos = path.lastIndexOf("/", endPos-1);
			}
		    if (count != 3) {
			    endPos = -1; // didn't find all three "/" before the final "/native.jpg".
		    }
	    }
	    if (endPos > startPos) {
		    path = path.substring(startPos, endPos);
	    } else {
		    LOGGER.warn("Couldn't understand path "+path);
	    }

		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (final UnsupportedEncodingException details) {
			LOGGER.warn("Couldn't decode path; no UTF-8 support"); // Never happens, all JVMs are required to support UTF-8
		}

        return path;
    }

    private String[] getRegion(final String aPathInfo) {
        String[] coordArray = new String[] {};

        if (aPathInfo.contains("/")) {
            final String[] pathParts = aPathInfo.split("/");

            if (pathParts.length > 2) {
                final String coordsString = aPathInfo.split("/")[2];

                if (!coordsString.equals("all") && !coordsString.equals("full")) {
                    final String[] coords = coordsString.split(",");

                    if (coords.length == 4) {
                        coordArray = coords;
                    } else if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Invalid coordinates ({}) requested in: {}", StringUtils.toString(coords, ','),
                                aPathInfo);
                        // TODO: throw exception?
                    }
                }
            }
        }

        return coordArray;
    }

    private String getScale(final String aPathInfo) {
        String scale = null;

        if (aPathInfo.contains("/")) {
            final String[] pathParts = aPathInfo.split("/");

            if (pathParts.length > 3) {
                scale = aPathInfo.split("/")[3];
            }
        }

        if (scale != null && scale.startsWith("pct:")) {
            scale = scale.substring(4);
            scale = Double.toString(Double.parseDouble(scale) * .01);
        }

        return scale;
    }
}
