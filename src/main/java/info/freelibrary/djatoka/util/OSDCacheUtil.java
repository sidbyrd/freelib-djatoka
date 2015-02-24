
package info.freelibrary.djatoka.util;

import info.freelibrary.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class OSDCacheUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OSDCacheUtil.class);

    private static final String REGION = "{},{},{},{}";

    private static final String LABEL = "0/native.jpg";

    /**
     * Utility used to generate regions for derivatives images prior to the point where they are requested by a user.
     */
    public OSDCacheUtil() {

    }

    /**
     * Return a list of derivatives to be generated.
     * 
     * @return
     */
    public String[] getPaths(final String aService, final String aID, final int aTileSize, final int aWidth,
            final int aHeight) {
        final ArrayList<String> list = new ArrayList<String>();
        final int longDim = Math.max(aWidth, aHeight);
        final String id;

        try {
            // TODO replace with path safe encode
            id = URLEncoder.encode(aID, "UTF-8"); // Encode for IIIF Web service API
        } catch (final UnsupportedEncodingException details) {
            throw new RuntimeException(details); // All JVMs required to support UTF-8
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generating OSD paths [ID: {}; Tile Size: {}; Width: {}; Height: {} ]", aID, aTileSize,
                    aWidth, aHeight);
        }

        for (int multiplier = 1; multiplier * aTileSize < longDim; multiplier *= 2) {
            final int tileSize = multiplier * aTileSize;

            int x = 0, y = 0, xTileSize, yTileSize;
            String region, path, size;

            for (x = 0; x < aWidth + tileSize; x += tileSize) {
                xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                yTileSize = tileSize < aHeight ? tileSize : aHeight;

                if (xTileSize > 0 && yTileSize > 0) {
                    region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                    size = getSize(multiplier, xTileSize, yTileSize);
                    path = StringUtils.toString('/', aService, id, region, size, LABEL);

                    if (list.add(path)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("OSD tile path added: {}", path);
                        }
                    } else if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Path {} couldn't be added to tile cache", path);
                    }
                }

                for (y = tileSize; y < aHeight + tileSize; y += tileSize) {
                    xTileSize = x + tileSize < aWidth ? tileSize : aWidth - x;
                    yTileSize = y + tileSize < aHeight ? tileSize : aHeight - y;

                    if (xTileSize > 0 && yTileSize > 0) {
                        region = StringUtils.format(REGION, x, y, xTileSize, yTileSize);
                        size = getSize(multiplier, xTileSize, yTileSize);
                        path = StringUtils.toString('/', aService, id, region, size, LABEL);

                        if (list.add(path)) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("OSD tile path added: {}", path);
                            }
                        } else {
                            LOGGER.warn("Path {} couldn't be added to tile cache", path);
                        }
                    }
                }

                y = 0;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'{}' OSD tile path regions created", list.size());
        }

        return list.toArray(new String[list.size()]);
    }

    private String getSize(final double aMultiplier, final int aXTileSize, final int aYTileSize) {
        return (int) Math.ceil(aXTileSize / aMultiplier) + "," + (int) Math.ceil(aYTileSize / aMultiplier);
    }
}
