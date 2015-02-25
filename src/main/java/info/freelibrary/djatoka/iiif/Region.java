
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

    public String toIiifString() {
        StringBuilder builder = new StringBuilder();

        if (isFullSize()) {
            builder.append("");
        } else {
            builder.append(myY).append(',').append(myX).append(',');
            if (usesPercents()) {
                builder.append(Math.ceil(myY*myHeight/100)).append(',').append(Math.ceil(myX*myWidth/100));
            } else {
                builder.append(myHeight).append(',').append(myWidth);
            }
        }

        return builder.toString();
    }
}
