
package info.freelibrary.djatoka.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CacheUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtils.class);

    /**
     * Return a file name for the cached file based on its characteristics.
     * 
     * @param aLevel A level to be cached
     * @param aRegion A region to be cached
     * @param aScale A scale to be cached
     * @param aRotation A rotation to be cached
     * @return The file name for the cached file
     */
    public static final String getFileName(final String aLevel, final String aRegion, final String aScale,
            final float aRotation) {
        final StringBuilder cfName = new StringBuilder("image_");
        final String region = isEmpty(aRegion) ? "full" : aRegion.replace(',', '-');
        final String scale = isEmpty(aScale) ? "full" : aScale.replace(',', '-');

        final boolean useOldNames = false;
        if (useOldNames && !isEmpty(aLevel) && !aLevel.equals("-1") && region.equals("full")) {
            // backwards compatibility name with level but no region or scale
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking cache for level-oriented tile");
            }

            cfName.append(aLevel);
        } else if (useOldNames && isEmpty(aLevel)){
            // backwards compatibility name with region and scale but no level
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking cache for scale-oriented tile");
            }

            cfName.append(scale).append('_').append(region);
        } else {
            // not using old names, or need a name with all three components anyways
            // level_region_scale_rotation--same order as everything else around here.
            cfName.append(aLevel).append('_').append(region).append('_').append(scale);
        }

        if (!useOldNames || aRotation != 0.0f) {
            cfName.append('_').append((int) aRotation); // djatoka expects int
        }

        return cfName.append(".jpg").toString();
    }

    /**
     * Gets the max level for the supplied height and width.
     * 
     * @param aHeight A height of the image
     * @param aWidth A width of the image
     * @return The maximum level using the supplied height and width
     */
    public static final int getMaxLevel(final int aHeight, final int aWidth) {
        return (int) Math.ceil(Math.log(Math.max(aHeight, aWidth)) / Math.log(2));
    }

    /**
     * Returns the scale for the supplied level.
     * 
     * @param aLevel A supplied image level
     * @return The scale for the supplied level
     */
    public static final int getScale(final int aLevel) {
        return (int) Math.pow(2, aLevel);
    }

    /**
     * Get a list of tile queries based on the supplied height and width.
     * 
     * @param aHeight A supplied image height
     * @param aWidth A supplied image width
     * @return The list of tile queries based on the supplied height and width
     */
    public static final List<String> getCachingQueries(final int aHeight, final int aWidth) {
        final int maxLevel = getMaxLevel(aHeight, aWidth);
        final List<String> list = new ArrayList<String>();

        for (int level = 0; level <= maxLevel; level++) {
            final String scale = Integer.toString(getScale(level));

            if (level <= 8) {
                list.add("/all/" + scale);
                continue; // We don't need to get regions for these
            }

            final int tileSize = getTileSize(level, maxLevel);
            int x = 0;

            /* x is left point and y is top point */
            for (int xSize = 0, y = 0; xSize <= aWidth; xSize += tileSize) {
                if (x * tileSize > (aWidth + tileSize)) {
                    break;
                }

                for (int ySize = 0; ySize <= aHeight; ySize += tileSize) {
                    final String region = getRegion(level, aWidth, aHeight, x, y++);

                    if (y * tileSize > (aHeight + tileSize)) {
                        break;
                    }

                    list.add("/" + region + "/" + scale);
                }

                x += 1;
            }
        }

        return list;
    }

    /**
     * Gets a string representation of the region for the supplied characteristics.
     * 
     * @param aLevel A image level
     * @param aWidth An image width
     * @param aHeight An image height
     * @param aX An X coordinate
     * @param aY A Y coordinate
     */
    public static final String getRegion(final int aLevel, final int aWidth, final int aHeight, final int aX,
            final int aY) {
        // All the other code uses width, height (rather than height, width); I
        // should probably change to match their use pattern/order for
        // consistency

        final int tileSize = getTileSize(aLevel, getMaxLevel(aHeight, aWidth));
        int startX, startY, tileSizeX, tileSizeY;

        /* startX is left point and startY is top point */
        if (aLevel > 8) {
            if (aX == 0) {
                tileSizeX = tileSize - 1;
            } else {
                tileSizeX = tileSize;
            }

            if (aY == 0) {
                tileSizeY = tileSize - 1;
            } else {
                tileSizeY = tileSize;
            }

            startX = aX * tileSize;
            startY = aY * tileSize;

            return startY + "," + startX + "," + tileSizeY + "," + tileSizeX;
        }

        return "full";
    }

    private static boolean isEmpty(final String aString) {
        return aString == null || aString.equals("");
    }

    private static int getTileSize(final int aLevel, final int aMaxLevel) {
        return (int) (Math.pow(2, aMaxLevel) / Math.pow(2, aLevel)) * 256;
    }
}
