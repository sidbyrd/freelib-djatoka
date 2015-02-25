/*
 * Copyright (c) 2008 Los Alamos National Security, LLC.
 * 
 * Los Alamos National Laboratory Research Library Digital Library Research &
 * Prototyping Team
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package gov.lanl.adore.djatoka.openurl;

import gov.lanl.adore.djatoka.DjatokaDecodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.DjatokaExtractProcessor;
import gov.lanl.adore.djatoka.io.FormatConstants;
import gov.lanl.adore.djatoka.kdu.KduExtractExe;
import gov.lanl.adore.djatoka.plugin.ITransformPlugIn;
import gov.lanl.adore.djatoka.util.IOUtils;
import gov.lanl.adore.djatoka.util.ImageRecord;
import gov.lanl.util.HttpDate;
import info.freelibrary.djatoka.util.CacheUtils;
import info.freelibrary.djatoka.view.IdentifierResolver;
import info.openurl.oom.*;
import info.openurl.oom.config.ClassConfig;
import info.openurl.oom.config.OpenURLConfig;
import info.openurl.oom.entities.Referent;
import info.openurl.oom.entities.ReferringEntity;
import info.openurl.oom.entities.Requester;
import info.openurl.oom.entities.ServiceType;
import org.oclc.oomRef.descriptors.ByValueMetadataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.*;

/**
 * The OpenURLJP2KService OpenURL Service
 * 
 * @author Ryan Chute
 */
public class OpenURLJP2KService implements Service, FormatConstants {

    private static Logger LOGGER = LoggerFactory.getLogger(OpenURLJP2KService.class);

    private static final String DEFAULT_IMPL_CLASS = IdentifierResolver.class.getCanonicalName();

    private static final String PROPS_REQUESTER = "requester";

    private static final String PROPS_REFERRING_ENTITY = "referringEntity";

    private static final String PROPS_KEY_IMPL_CLASS = "OpenURLJP2KService.referentResolverImpl";

    private static final String PROPS_KEY_CACHE_ENABLED = "OpenURLJP2KService.cacheEnabled";

    private static final String PROPS_KEY_CACHE_TMPDIR = "OpenURLJP2KService.cacheTmpDir";

    private static final String SCALE_CACHE_EXCEPTIONS = "OpenURLJP2KService.scaleCacheExceptions";

    private static final String PROPS_KEY_TRANSFORM = "OpenURLJP2KService.transformPlugin";

    private static final String PROPS_KEY_CACHE_SIZE = "OpenURLJP2KService.cacheSize";

    private static final String PROP_KEY_CACHE_MAX_PIXELS = "OpenURLJP2KService.cacheImageMaxPixels";

    private static final String SVC_ID = "info:lanl-repo/svc/getRegion";

    private static final String DEFAULT_CACHE_SIZE = "1000";

    private static final int DEFAULT_CACHE_MAXPIXELS = 100000;

    private static String implClass = null;

    private static Properties props = new Properties();

    private static boolean init = false;

    private static boolean cacheTiles = true;

    private static boolean transformCheck = false;

    private static ITransformPlugIn transform;

    private static String cacheDir = null;

    private static TileCacheManager<String, String> tileCache;

    private static DjatokaExtractProcessor extractor;

    private static int maxPixels = DEFAULT_CACHE_MAXPIXELS;

    private static Set<Double> scaleCacheExceptions;

    /**
     * Construct an info:lanl-repo/svc/getRegion web service class. Initializes Referent Resolver instance using
     * OpenURLJP2KService.referentResolverImpl property.
     * 
     * @param openURLConfig OOM Properties forwarded from OpenURLServlet
     * @param classConfig Implementation Properties forwarded from OpenURLServlet
     * @throws ResolverException
     */
    public OpenURLJP2KService(final OpenURLConfig openURLConfig, final ClassConfig classConfig)
            throws ResolverException {
        try {
            if (!init) {
                props = IOUtils.loadConfigByCP(classConfig.getArg("props"));
                if (!ReferentManager.isInit()) {
                    implClass = props.getProperty(PROPS_KEY_IMPL_CLASS, DEFAULT_IMPL_CLASS);
                    ReferentManager.init((IReferentResolver) Class.forName(implClass).newInstance(), props);
                }
                cacheDir = props.getProperty(PROPS_KEY_CACHE_TMPDIR);
                if (props.getProperty(PROPS_KEY_CACHE_ENABLED) != null) {
                    cacheTiles = Boolean.parseBoolean(props.getProperty(PROPS_KEY_CACHE_ENABLED));
                }
                if (cacheTiles) {
                    final int cacheSize =
                            Integer.parseInt(props.getProperty(PROPS_KEY_CACHE_SIZE, DEFAULT_CACHE_SIZE));
                    tileCache = new TileCacheManager<String, String>(cacheSize);
                }
                if (props.getProperty(PROPS_KEY_TRANSFORM) != null) {
                    transformCheck = true;
                    final String transClass = props.getProperty(PROPS_KEY_TRANSFORM);
                    transform = (ITransformPlugIn) Class.forName(transClass).newInstance();
                    transform.setup(props);
                }
                if (props.getProperty(PROP_KEY_CACHE_MAX_PIXELS) != null) {
                    maxPixels = Integer.parseInt(props.getProperty(PROP_KEY_CACHE_MAX_PIXELS));
                }
                if (props.getProperty(SCALE_CACHE_EXCEPTIONS) != null) {
                    scaleCacheExceptions = new HashSet<Double>();

                    for (final String exception : props.getProperty(SCALE_CACHE_EXCEPTIONS).split("\\s+")) {
                        try {
                            scaleCacheExceptions.add(new Double(exception));

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Scale cache exception added: {}", exception);
                            }
                        } catch (final NumberFormatException details) {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("Configured scale cache exception isn't a valid double: {}", exception);
                            }
                        }
                    }
                } else {
                    scaleCacheExceptions = new HashSet<Double>();
                }
                extractor = new DjatokaExtractProcessor(new KduExtractExe());
                init = true;
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ResolverException("Error attempting to open props file from classpath, disabling " + SVC_ID +
                    " : " + e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ResolverException("Unable to inititalize implementation: " + props.getProperty(implClass) +
                    " - " + e.getMessage());
        }
    }

    /**
     * Returns the OpenURL service identifier for this implementation of info.openurl.oom.Service
     */
    @Override
    public URI getServiceID() throws URISyntaxException {
        return new URI(SVC_ID);
    }

    /**
     * Removes a tile from the tile cache.
     * 
     * @param aCacheID The ID of the tile in the cache
     * @return True if the tile was successfully removed; else, false
     */
    public static boolean removeFromTileCache(final String aCacheID) {
        return tileCache.remove(aCacheID) != null;
    }

    /**
     * Returns the OpenURLResponse consisting of an image bitstream to be rendered on the client. Having obtained a
     * result, this method is then responsible for transforming it into an OpenURLResponse that acts as a proxy for
     * HttpServletResponse.
     */
    @Override
    public OpenURLResponse resolve(final ServiceType serviceType, final ContextObject contextObject,
            final OpenURLRequest openURLRequest, final OpenURLRequestProcessor processor) {
        String djatokaCacheFile = null;
        String responseFormat = null;
        String format = "image/jpeg";
        int status = HttpServletResponse.SC_OK;
        final HashMap<String, String> kev = setServiceValues(contextObject);
        final DjatokaDecodeParam params = new DjatokaDecodeParam();
        String id = null;

        if (kev.containsKey("region") && !kev.get("region").isEmpty()) {
            params.setRegion(kev.get("region"));
        }
        if (kev.containsKey("format")) {
            format = kev.get("format");
            if (!format.startsWith("image")) {
                // ignoring invalid format identifier
                format = "image/jpeg";
            }
        }
        if (kev.containsKey("level")) {
            params.setLevel(Integer.parseInt(kev.get("level")));
        }
        if (kev.containsKey("rotate")) {
            params.setRotationDegree(Integer.parseInt(kev.get("rotate")));
        }
        if (kev.containsKey("scale")) {
            final String[] v = kev.get("scale").split(",");
            if (v.length == 1) {
                if (v[0].contains(".")) {
                    params.setScalingFactor(Double.parseDouble(v[0]));
                } else {
                    final int[] dims = new int[] { -1, Integer.parseInt(v[0]) };
                    params.setScalingDimensions(dims);
                }
            } else if (v.length == 2) {
                final int[] dims = new int[] { Integer.parseInt(v[0]), Integer.parseInt(v[1]) };
                params.setScalingDimensions(dims);
            }
        }
        if (kev.containsKey("clayer") && kev.get("clayer") != null) {
            final int clayer = Integer.parseInt(kev.get("clayer"));
            if (clayer > 0) {
                params.setCompositingLayer(clayer);
            }
        }
        responseFormat = format;

        byte[] bytes = null;

        if (params.getRegion() != null && params.getRegion().contains("-")) {
            try {
                bytes = ("Negative Region Arguments are not supported.").getBytes("UTF-8");
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            responseFormat = "text/plain";
            status = HttpServletResponse.SC_NOT_FOUND;
        } else {
            final String region = params.getRegion();

            if (LOGGER.isDebugEnabled() && region != null) {
                LOGGER.debug("Service has a valid region request: {}", region);
            }

            try {
                final Referent referent = contextObject.getReferent();
                final ImageRecord r = ReferentManager.getImageRecord(referent);

                if (r != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Retrieving ImageRecord for: {}", r.getIdentifier());
                    }

                    if (transformCheck && transform != null) {
                        final HashMap<String, String> instProps = new HashMap<String, String>();
                        if (r.getInstProps() != null) {
                            instProps.putAll(r.getInstProps());
                        }
                        int l = contextObject.getRequesters().length;
                        final Requester[] req = contextObject.getRequesters();
                        if (l > 0 && req[0].getDescriptors().length > 0) {
                            final String rs = req[0].getDescriptors()[0].toString();
                            instProps.put(PROPS_REQUESTER, rs);
                        }
                        l = contextObject.getReferringEntities().length;
                        ReferringEntity[] rea;
                        rea = contextObject.getReferringEntities();
                        if (l > 0 && rea[0].getDescriptors().length > 0) {
                            instProps.put(PROPS_REFERRING_ENTITY, contextObject.getReferringEntities()[0]
                                    .getDescriptors()[0].toString());
                        }
                        if (instProps.size() > 0) {
                            transform.setInstanceProps(instProps);
                        }
                        params.setTransform(transform);
                    }
                    if (!cacheTiles || !isCacheable(params)) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Not using the OpenURL layer cache");
                        }

                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        extractor.extractImage(r.getImageFile(), baos, params, format);
                        bytes = baos.toByteArray();
                        baos.close();
                    } else {
                        final String ext = getExtension(format);
                        final String hash = getTileHash(r, params);
                        String file = tileCache.get(hash + ext);
                        File f;

                        id = r.getIdentifier();

                        if (file == null || !(f = new File(file)).exists() && f.length() > 0) {
                            if (cacheDir != null) {
                                final File cacheDirFile = new File(cacheDir);

                                // If our cache dir doesn't exist, create it
                                if (!cacheDirFile.exists()) {
                                    if (!cacheDirFile.mkdirs() && LOGGER.isWarnEnabled()) {
                                        LOGGER.warn("Dirs not created: {}", cacheDirFile);
                                    }
                                }

                                f = File.createTempFile("cache" + hash.hashCode() + "-", "." + ext, cacheDirFile);
                            } else {
                                f = File.createTempFile("cache" + hash.hashCode() + "-", "." + ext);
                            }

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Temp file created: {}", f);
                            }

                            f.deleteOnExit();
                            file = f.getAbsolutePath();
                            djatokaCacheFile = file;

                            extractor.extractImage(r.getImageFile(), file, params, format);

                            if (tileCache.get(hash + ext) == null) {
                                tileCache.put(hash + ext, file);
                                bytes = IOUtils.getBytesFromFile(f);

                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("makingTile: " + file + " " + bytes.length + " params: " + params);
                                }
                            } else {
                                // Handles simultaneous request on separate
                                // thread, ignores cache.
                                bytes = IOUtils.getBytesFromFile(f);

                                if (!f.delete() && LOGGER.isWarnEnabled()) {
                                    LOGGER.warn("File not deleted: {}", f);
                                }

                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("tempTile: " + file + " " + bytes.length + " params: " + params);
                                }
                            }
                        } else {
                            bytes = IOUtils.getBytesFromFile(new File(file));

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("tileCache: {} {}", file, bytes.length);
                            }

                            djatokaCacheFile = file;
                        }
                    }
                } else if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unable to retrieve ImageRecord");
                }
            } catch (final ResolverException e) {
                LOGGER.error(e.getMessage(), e);
                bytes = e.getMessage().getBytes();
                responseFormat = "text/plain";
                status = HttpServletResponse.SC_NOT_FOUND;
            } catch (final DjatokaException e) {
                LOGGER.error(e.getMessage(), e);
                bytes = e.getMessage().getBytes();
                responseFormat = "text/plain";
                status = HttpServletResponse.SC_NOT_FOUND;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                bytes = e.getMessage().getBytes();
                responseFormat = "text/plain";
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }

        if (bytes == null || bytes.length == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No bytes found!");
            }

            bytes = "".getBytes();
            responseFormat = "text/plain";
            status = HttpServletResponse.SC_NOT_FOUND;
        }

        final HashMap<String, String> header_map = new HashMap<String, String>();
        header_map.put("Content-Length", bytes.length + "");
        header_map.put("Date", HttpDate.getHttpDate());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting OpenURLResponse...");
        }

        final OpenURLResponse response = new OpenURLResponse(status, responseFormat, bytes, header_map);

        // Record where our cache file was (if we had/created one)
        if (djatokaCacheFile != null) {
            final StringBuilder dimBuilder = new StringBuilder();
            final int[] dims = params.getScalingDimensions();
            final String level = Integer.toString(params.getLevel());
            final String region = params.getRegion();
            final int rotation = params.getRotationDegree();
            final String ext = getExtension(format);
            String scale;
            String hash;

            if (dims != null) {
                if (dims.length == 2) {
                    dimBuilder.append(dims[0]).append(',').append(dims[1]);
                } else {
                    dimBuilder.append(dims[0]);
                }
            }

            scale = dimBuilder.toString();

            try {
                hash = getTileHash(id, params);
            } catch (final Exception details) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(details.getMessage(), details);
                }

                hash = null;
            }

            final String f = CacheUtils.getFileName(level, region, scale, rotation, false);
            id = id + "_" + f;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("OpenURL service: [ {} | {} | {} | {} ] = {}", new Object[] { level, scale, region,
                    rotation, id });
            }

            if (hash != null) {
                final Map sessionMap = response.getSessionMap();
                final String cacheName = hash + ext;

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Setting cache session data [ {} | {} ]", new Object[] { id, djatokaCacheFile });
                }

                sessionMap.put(id, djatokaCacheFile);
                sessionMap.put(djatokaCacheFile, cacheName);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("djatokaCacheFile variable is null");
        }

        return response;
    }

    private boolean isCacheable(final DjatokaDecodeParam params) {
        final double scale = params.getScalingFactor();
        boolean exception;

        if (transformCheck && params.getTransform().isTransformable()) {
            return false;
        }

        exception = scaleCacheExceptions.contains(new Double(scale));

        if (scale != 1.0 && !exception && params.getRegion() == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("NOT CACHING BECAUSE SCALING FACTOR {} != 1.0", params.getScalingFactor());
            }

            return false;
        }

        return true;
    }

    private static final String getTileHash(final ImageRecord r, final DjatokaDecodeParam params) throws Exception {
        return getTileHash(r.getIdentifier(), params);
    }

    private static String getTileHash(final String id, final DjatokaDecodeParam params) throws Exception {
        final int level = params.getLevel();
        final String region = params.getRegion();
        final int rotateDegree = params.getRotationDegree();
        final double scalingFactor = params.getScalingFactor();
        final int[] scalingDims = params.getScalingDimensions();
        String scale = "";
        if (scalingDims != null && scalingDims.length == 1) {
            scale = scalingDims[0] + "";
        }
        if (scalingDims != null && scalingDims.length == 2) {
            scale = scalingDims[0] + "," + scalingDims[1];
        }
        final int clayer = params.getCompositingLayer();
        final String rft_id =
                id + "|" + level + "|" + region + "|" + rotateDegree + "|" + scalingFactor + "|" + scale + "|" +
                        clayer;
        final MessageDigest complete = MessageDigest.getInstance("SHA1");
        return new String(complete.digest(rft_id.getBytes()));
    }

    private static final String getExtension(final String mimetype) {
        if (mimetype.equals(FORMAT_MIMEYPE_JPEG)) {
            return FORMAT_ID_JPG;
        }
        if (mimetype.equals(FORMAT_MIMEYPE_PNG)) {
            return FORMAT_ID_PNG;
        }
        if (mimetype.equals(FORMAT_MIMEYPE_PNM)) {
            return FORMAT_ID_PNM;
        }
        if (mimetype.equals(FORMAT_MIMEYPE_JP2)) {
            return FORMAT_ID_JP2;
        }
        if (mimetype.equals(FORMAT_MIMEYPE_JPX)) {
            return FORMAT_ID_JPX;
        }
        if (mimetype.equals(FORMAT_MIMEYPE_JPM)) {
            return FORMAT_ID_JP2;
        }
        return null;
    }

    private static HashMap<String, String> setServiceValues(final ContextObject co) {
        final HashMap<String, String> map = new HashMap<String, String>();
        final Object[] svcData = co.getServiceTypes()[0].getDescriptors();
        if (svcData != null && svcData.length > 0) {
            for (int i = 0; i < svcData.length; i++) {
                final Object tmp = svcData[i];
                if (tmp.getClass().getSimpleName().equals("ByValueMetadataImpl")) {
                    final ByValueMetadataImpl kev = ((ByValueMetadataImpl) tmp);
                    if (kev.getFieldMap().size() > 0) {
                        if (kev.getFieldMap().containsKey("svc.region") &&
                                ((String[]) kev.getFieldMap().get("svc.region"))[0] != "") {
                            map.put("region", ((String[]) kev.getFieldMap().get("svc.region"))[0]);
                        }
                        if (kev.getFieldMap().containsKey("svc.format") &&
                                ((String[]) kev.getFieldMap().get("svc.format"))[0] != "") {
                            map.put("format", ((String[]) kev.getFieldMap().get("svc.format"))[0]);
                        }
                        if (kev.getFieldMap().containsKey("svc.level") &&
                                ((String[]) kev.getFieldMap().get("svc.level"))[0] != "") {
                            map.put("level", ((String[]) kev.getFieldMap().get("svc.level"))[0]);
                        }
                        if (kev.getFieldMap().containsKey("svc.rotate") &&
                                ((String[]) kev.getFieldMap().get("svc.rotate"))[0] != "") {
                            map.put("rotate", ((String[]) kev.getFieldMap().get("svc.rotate"))[0]);
                        }
                        if (kev.getFieldMap().containsKey("svc.scale") &&
                                ((String[]) kev.getFieldMap().get("svc.scale"))[0] != "") {
                            map.put("scale", ((String[]) kev.getFieldMap().get("svc.scale"))[0]);
                        }
                        if (kev.getFieldMap().containsKey("svc.clayer") &&
                                ((String[]) kev.getFieldMap().get("svc.clayer"))[0] != "") {
                            map.put("clayer", ((String[]) kev.getFieldMap().get("svc.clayer"))[0]);
                        }
                    }
                }
            }
        }
        return map;
    }
}
