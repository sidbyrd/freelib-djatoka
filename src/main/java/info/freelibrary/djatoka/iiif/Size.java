
package info.freelibrary.djatoka.iiif;

/**
 * A representation of the size aspect of an IIIF request.
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class Size {

    private boolean mySizeIsFull;

    private boolean mySizeIsPercent;

    private boolean myAspectRatioIsPreserved;

    private int myPercent = -1;

    private int myHeight = -1;

    private int myWidth = -1;

    /**
     * Constructs a new IIIF request size.
     * 
     * @param aSize The string value of a request size
     * @throws IIIFException If there is trouble constructing the request size
     */
    public Size(final String aSize) throws IIIFException {
        if (aSize.equalsIgnoreCase("full")) {
            mySizeIsFull = true;
            myAspectRatioIsPreserved = true;
        } else if (aSize.startsWith("pct:")) {
            mySizeIsPercent = true;
            myAspectRatioIsPreserved = true;

            try {
                final int size = Integer.parseInt(aSize.substring(4));

                if (size < 0 || size > 100) {
                    throw new IIIFException("Size percent isn't in the range of 0 to 100: " + size);
                }

                myPercent = size;
            } catch (final NumberFormatException details) {
                throw new IIIFException("Size percent isn't an integer: " + aSize.substring(4));
            }
        } else if (aSize.contains(",")) {
            if (aSize.length() == 1) {
                throw new IIIFException("Scaled size lacks a value");
            }

            if (aSize.startsWith(",")) {
                try {
                    myHeight = Integer.parseInt(aSize.substring(1));
                    if (myHeight < 0) {
                        throw new IIIFException("Height may not be negative: " + myHeight);
                    }
                    myAspectRatioIsPreserved = true;
                } catch (final NumberFormatException details) {
                    throw new IIIFException("Size's scaled height is not an integer: " + aSize.substring(1));
                }
            } else if (aSize.endsWith(",")) {
                final int end = aSize.length() - 1;

                try {
                    myWidth = Integer.parseInt(aSize.substring(0, end));
                    if (myWidth < 0) {
                        throw new IIIFException("Width may not be negative: " + myWidth);
                    }
                    myAspectRatioIsPreserved = true;
                } catch (final NumberFormatException details) {
                    throw new IIIFException("Size's scaled width is not an integer: " + aSize.substring(0, end));
                }
            } else {
                String size = aSize;
                String[] parts;

                if (aSize.startsWith("!")) {
                    size = aSize.substring(1);
                    myAspectRatioIsPreserved = true;
                }

                parts = size.split(",");

                if (parts.length != 2) {
                    throw new IIIFException("Size shouldn't have more than 2 parts");
                }

                try {
                    myWidth = Integer.parseInt(parts[0]);
                    if (myWidth < 0) {
                        throw new IIIFException("Width may not be negative: " + myWidth);
                    }
                } catch (final NumberFormatException details) {
                    throw new IIIFException("Size's width isn't an integer");
                }

                try {
                    myHeight = Integer.parseInt(parts[1]);
                    if (myHeight < 0) {
                        throw new IIIFException("Height may not be negative: " + myHeight);
                    }
                } catch (final NumberFormatException details) {
                    throw new IIIFException("Size's height isn't a integer");
                }
            }
        } else {
            throw new IIIFException("Size parameter isn't formatted correctly: " + aSize);
        }
    }

    /**
     * Informs the Size of the dimensions of the Region is is for, and computes
     * explicit size and width if it didn't already have it.
     * If it used percents, they are converted to direct dimensions.
     * If either dimension was unspecified (-1) before, it remains unchanged.
     * @param regionWidth width of the Region this Size is for
     * @param regionHeight height of the Region this Size is for
     */
    public void normalizeForRegionDims(int regionWidth, int regionHeight) {
        if (mySizeIsFull) {
            mySizeIsFull = false; // means don't toString() as "full", not whether it has full coverage.
            if (hasWidth()) {
                myWidth = regionWidth;
            }
            if (hasHeight()) {
                myHeight = regionHeight;
            }
        } else if (mySizeIsPercent) {
            if (hasWidth()) {
                myWidth = (int)(regionWidth * myWidth*0.01);
            }
            if (hasHeight()) {
                myHeight = (int)(regionHeight * myHeight*0.01);
            }
        }
        mySizeIsPercent = false;
    }

    /**
     * Explicitly sets both width and height with direct (non-percent) values.
     * This means that isAspectRatioPreserved() becomes false.
     * Note: Explicit values for both width and height are required with Djatoka level-based requests.
     * @param width new width for this Size
     * @param height new height for this Size
     */
    public void setExplicit(int width, int height) {
        myWidth = width;
        myHeight = height;
        myAspectRatioIsPreserved = false; // semantically false, but this actually just means "is w or h -1 ?"
        mySizeIsFull = false; // means don't toString() as "full", not whether it has full coverage.
        mySizeIsPercent = false;
    }

    /**
     * Returns true if the request is for a full-size image; else, false.
     * 
     * @return True if the request is for a full-size image; else, false
     */
    public boolean isFullSize() {
        return mySizeIsFull;
    }

    /**
     * Returns true if the request's size is expressed as a percent; else, false.
     * 
     * @return True if the request's size is expressed as a percent; else, false
     */
    public boolean isPercent() {
        return mySizeIsPercent;
    }

    /**
     * Returns true if the aspect ratio is maintained; else, false.
     * 
     * @return True if the aspect ratio is maintained; else, false
     */
    public boolean maintainsAspectRatio() {
        return myAspectRatioIsPreserved;
    }

    /**
     * Gets the percent value of the request's size.
     * 
     * @return The percent value of the request's size
     */
    public int getPercent() {
        return myPercent;
    }

    /**
     * Returns true if the request's size has a width; else, false.
     * 
     * @return True if the request's size has a width
     */
    public boolean hasWidth() {
        return myWidth != -1;
    }

    /**
     * Returns the width of the request's size.
     * 
     * @return The width of the request's size
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Returns true if the request's size has a height; else, false.
     * 
     * @return True if the request's size has a height; else, false
     */
    public boolean hasHeight() {
        return myHeight != -1;
    }

    /**
     * Returns the height of the request's size.
     * 
     * @return The height of the request's size
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Returns the string representation of the request's size.
     * 
     * @return The string representation of the request's size
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        if (maintainsAspectRatio()) {
            if (isFullSize()) {
                builder.append("full");
            } else if (isPercent()) {
                builder.append(Float.toString((float) myPercent / 100));
            } else {
                if (hasHeight() && hasWidth()) {
                    builder.append('!').append(myWidth).append(',');
                    builder.append(myHeight);
                } else if (hasHeight()) {
                    builder.append("0,").append(myHeight); // I rather suspect this was a hack just for filename purposes
                } else {
                    builder.append(myWidth).append(",0");
                }
            }
        } else {
            builder.append(myWidth).append(',').append(myHeight);
        }

        return builder.toString();
    }

    /**
     * Returns as string the was a Djatoka OpenURL request likes it
     * Basically, it's the same but a default value is -1, not 0.
     * @return
     */
    public String toDjatokaString() {
        final StringBuilder builder = new StringBuilder();

        if (isFullSize()) {
            builder.append("1.0"); // single scaling factor instead of w & h
        } else if (isPercent()) {
            builder.append(Float.toString((float) myPercent / 100)); // single scaling factor instead of w & h
        } else {
            builder.append(myWidth).append(',').append(myHeight); // if either is "-1", send it unaltered.
        }

        return builder.toString();
    }
}
