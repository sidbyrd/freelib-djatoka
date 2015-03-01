
package info.freelibrary.djatoka.iiif;

import info.freelibrary.util.StringUtils;

/**
 * A representation of the region aspect of an IIIF request.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class Region {

    private boolean myRegionIsFullSize;

    private boolean myRegionUsesPercents;

    private int myX;

    private int myY;

    private int myHeight;

    private int myWidth;

    /**
     * Constructs a representation of an IIIF request's region.
     * 
     * @param aRegion An IIIF string representation of a region
     * @throws IIIFException If there is something wrong with the string representation of a IIIF region
     */
    public Region(String aRegion) throws IIIFException {
        if (aRegion.equalsIgnoreCase("full")) {
            myRegionIsFullSize = true;
        } else {
            String[] parts;
            String region;

            if (aRegion.startsWith("pct:")) {
                myRegionUsesPercents = true;
                region = aRegion.substring(4);
            } else {
                region = aRegion;
            }

            parts = region.split(",");

            if (parts.length != 4) {
                throw new IIIFException(StringUtils.format("Incorrect number of region coords: {} (expected 4)",
                        Integer.toString(parts.length)));
            }

            try {
                myX = Integer.parseInt(parts[0]);

                if (myX < 0) {
                    throw new IIIFException("Region's X parameter isn't a positive number: " + myX);
                }
            } catch (NumberFormatException details) {
                throw new IIIFException(StringUtils.format("Region's X parameter ({}) isn't an integer", parts[0]));
            }

            try {
                myY = Integer.parseInt(parts[1]);

                if (myY < 0) {
                    throw new IIIFException("Region's Y parameter isn't a positive number: " + myY);
                }
            } catch (NumberFormatException details) {
                throw new IIIFException(StringUtils.format("Region's Y parameter ({}) isn't an integer", parts[1]));
            }

            try {
                myWidth = Integer.parseInt(parts[2]);

                if (myWidth <= 0) {
                    throw new IIIFException("Region's width parameter isn't greater than 0: " + myWidth);
                }

                if (myRegionUsesPercents && myWidth > 100) {
                    throw new IIIFException("Region's width percent can't be more than 100%");
                }
            } catch (NumberFormatException details) {
                throw new IIIFException(StringUtils
                        .format("Region's width parameter ({}) isn't an integer", parts[2]));
            }

            try {
                myHeight = Integer.parseInt(parts[3]);

                if (myHeight <= 0) {
                    throw new IIIFException("Region's height parameter isn't greater than 0: " + myHeight);
                }

                if (myRegionUsesPercents && myHeight > 100) {
                    throw new IIIFException("Region's height percent can't be more than 100%");
                }
            } catch (NumberFormatException details) {
                throw new IIIFException(StringUtils.format("Region's height parameter ({}) isn't an integer",
                        parts[3]));
            }
        }
    }

    /**
     * Informs this Region of the full dimensions of the image it is for, and
     * computes explicit size and width if it didn't already have it.
     * If it is full coverage, turns itself into a "full" region.
     * If it used percents, they are converted to direct dimensions.
     * @param imageWidth width of image this Region is for
     * @param imageHeight height of image this Region is for
     */
    public void normalizeForImageDims(int imageWidth, int imageHeight) {
        if (myRegionIsFullSize) {
            myX=0;
            myY=0;
            myWidth = imageWidth;
            myHeight = imageHeight;
        } else if (myRegionUsesPercents) {
            myX = (int)(imageWidth * (myX*0.01));
            myY = (int)(imageHeight * (myY*0.01));
            myWidth = (int)(imageWidth * (myWidth*0.01));
            myHeight = (int)(imageHeight * (myHeight*0.01));
        }
        myRegionUsesPercents = false;

        if (myX==0 && myY==0 && myWidth==imageWidth && myHeight==imageHeight) {
            myRegionIsFullSize = true;
        }
    }

    /**
     * Returns true if region is full size; else, false
     * 
     * @return True if region is full size; else, false
     */
    public boolean isFullSize() {
        return myRegionIsFullSize;
    }

    /**
     * Returns true if region uses percents; else, false
     * 
     * @return True if region uses percents; else, false
     */
    public boolean usesPercents() {
        return myRegionUsesPercents;
    }

    /**
     * Returns the region's horizontal left.
     * 
     * @return The region's horizontal left
     */
    public int getHorizontalLeft() {
        return myX;
    }

    /**
     * Returns the region's horizontal right.
     * 
     * @return The region's horizontal right
     */
    public int getX() {
        return myX;
    }

    /**
     * Returns the region's vertical top.
     * 
     * @return The region's vertical top
     */
    public int getVerticalTop() {
        return myY;
    }

    /**
     * Returns the region's vertical bottom.
     * 
     * @return The region's vertical bottom
     */
    public int getY() {
        return myY;
    }

    /**
     * Returns the width of the region.
     * 
     * @return The width of the region
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Returns the height of the region.
     * 
     * @return The height of the region
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Returns a string representation of the IIIF region.
     * 
     * @return A string representation of the IIIF region
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (isFullSize()) {
            builder.append("full");
        } else if (usesPercents()) {
            builder.append("pct:").append(myX).append(',').append(myY);
            builder.append(',').append(myWidth).append(',').append(myHeight);
        } else {
            builder.append(myX).append(',').append(myY).append(',');
            builder.append(myWidth).append(',').append(myHeight);
        }

        return builder.toString();
    }

    /**
     * The way Djatoka wants it.
     * Uses region coords and region dims it it will go with a scale
     * Uses region coords and scale dims if it will go with a level.
     * @param level the level this region goes with (or < 1 for no level)
     * @param scale the scale this region will go with.
     *          May be null if level < 1 or scale is 100% anyways.
     * @return string to go in a svc.region= part of an OpenURL
     */
    public String toDjatokaString(int level, Size scale) {
        StringBuilder builder = new StringBuilder();

        if (myRegionIsFullSize && level < 1) {
            // using region-based request, but region is "full". Done.
            builder.append("");
            return builder.toString();
        }

        // coords
        if (myRegionIsFullSize) {
            // level and regionScale requires coords
            builder.append("0,0,");
        } else {
            builder.append(myY).append(',').append(myX).append(',');
        }

        // dimensions
        if (myRegionUsesPercents) {
            builder.append("pct:"); // will not work correctly if level>0 and haven't called normalizeForImageDims()
        }
        if (scale==null || scale.isFullSize() || level < 1) {
            // if no scale info provided: scale is effectively 100%, so use region dims; it's the same.
            // if using region-based request (level <1) : send region dims
            builder.append(myHeight).append(',').append(myWidth);
        } else {
            // using level-based request: send scale; that's just how Djatoka does it.
            // scale must be explicit in both dims, so call normalizeForImageDims() first.
            builder.append(scale.getHeight()).append(',').append(scale.getWidth());
            // useless bonus info: if level >0 and fullSize, level must be exactly 1, but still need scale dims, not region dims.
        }

        return builder.toString();
    }

}
