
package info.freelibrary.djatoka.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import info.freelibrary.djatoka.iiif.Constants;
import info.freelibrary.djatoka.util.URLEncode;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageInfo {

    private final String myId;
    private final int myHeight;
    private final int myWidth;
    private final int myLevels;
    private final List<String> myFormats;

    private Document infoDoc = null;
    private String json = null;

    /**
     * Creates an image info object which may be queried for its constructor-supplied properties, or
     * output as XML or JSON.
     * 
     * @param aID An image ID
     * @param aHeight The height of the image represented by the supplied ID
     * @param aWidth The width of the image represented by the supplied ID
     * @param aLevels The max Djatoka "level" the image can be requested at
     */
    public ImageInfo(final String aID, final int aHeight, final int aWidth, final int aLevels) {
        myId = aID;
        myHeight = aHeight;
        myWidth = aWidth;
        myLevels = aLevels;
        myFormats = new ArrayList<String>(1); // in practice, we only use 1.
    }

    /**
     * Gets the image's identifier.
     * 
     * @return The image's identifier
     */
    public String getIdentifier() {
        return myId;
    }

    /**
     * Gets the image's height.
     * 
     * @return The height of the image
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Gets the image's width.
     * 
     * @return The width of the image
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Gets the image's max levels
     *
     * @return the max levels
     */
    public int getLevels() {
        return myLevels;
    }

    /**
     * Adds the supplied format to the list of handled formats.
     * 
     * @param aFormat A format to add to the supported list
     */
    public void addFormat(final String aFormat) {
        if (!aFormat.contains(aFormat)) {
            myFormats.add(aFormat);
        }
        infoDoc = null; // invalidate if already generated
        json = null;
    }

    /**
     * Gets the list of supported formats.
     * 
     * @return The list of supported formats
     */
    public List<String> getFormats() {
        return Collections.unmodifiableList(myFormats);
    }

    /**
     * Makes an XML Document containing all the data we have for this image.
     * If it has already been made (and nothing has changed since then), returns existing Document.
     * @return generated Document
     */
    private Document infoDoc() {
        if (infoDoc == null) {
            final Element id = new Element("identifier", Constants.IIIF_NS);
            final Element height = new Element("height", Constants.IIIF_NS);
            final Element width = new Element("width", Constants.IIIF_NS);
            final Element root = new Element("info", Constants.IIIF_NS);
            final Element formats = new Element("myFormats", Constants.IIIF_NS);

            width.appendChild(Integer.toString(myWidth));
            height.appendChild(Integer.toString(myHeight));
            id.appendChild(myId);

            infoDoc = new Document(root);
            root.appendChild(id);
            root.appendChild(width);
            root.appendChild(height);
            root.appendChild(formats);

            for (String aFormat : myFormats) {
                final Element format = new Element("format", Constants.IIIF_NS);
                format.appendChild(aFormat);
                formats.appendChild(format);
            }
        }

        return infoDoc;
    }

    /**
     * Gets the XML representation of the image's metadata.
     * 
     * @return The XML representation of the image's metadata
     */
    public String toXML() {
        return infoDoc().toXML();
    }

    /**
     * Gets the string representation of the image's metadata, which is defined to be its XML representation.
     * 
     * @return The string representation of the image's metadata
     */
    @Override
    public String toString() {
        return infoDoc().toXML();
    }

    /**
     * Gets the JSON representation of the image's metadata.
     * 
     * @param aService The IIIF service
     * @param aPrefix The IIIF prefix
     * @return The JSON representation of the image's metadata
     * @throws JsonProcessingException if JSON can't be formed - shouldn't happen.
     */
    public String toJSON(final String aService, final String aPrefix) throws JsonProcessingException {
        if (json == null) {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode rootNode = mapper.createObjectNode();
            final ArrayNode formats, scaleFactors;
            final String id = URLEncode.pathSafetyEncode(getIdentifier());

            rootNode.put("@context", "http://library.stanford.edu/iiif/image-api/1.1/context.json");
            rootNode.put("@id", aService + "/" + aPrefix + "/" + id);
            rootNode.put("width", myWidth);
            rootNode.put("height", myHeight);

            scaleFactors = rootNode.arrayNode();

            // treat levels as exponential (output is 2^^(level-1), up to a max of 256==2^^(9-1), because let's not be crazy.
            int start = 1;
            for (int level = 0; level < myLevels && level <= 9; level++) {
                scaleFactors.add(start);
                start *=2;
            }

            rootNode.put("scale_factors", scaleFactors);
            rootNode.put("tile_width", 256); // TODO: provide other tile size options?
            rootNode.put("tile_height", 256);

            formats = rootNode.arrayNode();

            for (final String format : myFormats) {
                formats.add(format);
            }

            rootNode.put("myFormats", formats);
            rootNode.put("qualities", rootNode.arrayNode().add("native"));
            rootNode.put("profile", Constants.IIIF_URL + "1.1/compliance.html#level1");

            json = mapper.writeValueAsString(rootNode);
        }

        return json;
    }

    /**
     * Serializes the image info the supplied output stream as XML.
     * 
     * @param aOutputStream The output stream to which the image info should be serialized
     * @throws IOException If there is a problem reading or writing the image info
     */
    public void toStreamXML(final OutputStream aOutputStream) throws IOException {
        new Serializer(aOutputStream).write(infoDoc());
    }
}
