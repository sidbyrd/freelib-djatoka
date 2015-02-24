
package info.freelibrary.djatoka.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import info.freelibrary.djatoka.iiif.Constants;
import info.freelibrary.djatoka.util.URLEncode;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageInfo {

    private final Document myInfoDoc;

    private final int myLevel;

    /**
     * Creates an image info object. This was written back when XML was a allowed response... should be rewritten now
     * that it's JSON-only.
     * 
     * @param aID An image ID
     * @param aHeight The height of the image represented by the supplied ID
     * @param aWidth The width of the image represented by the supplied ID
     */
    public ImageInfo(final String aID, final int aHeight, final int aWidth, final int aLevel) {
        final Element id = new Element("identifier", Constants.IIIF_NS);
        final Element height = new Element("height", Constants.IIIF_NS);
        final Element width = new Element("width", Constants.IIIF_NS);
        final Element root = new Element("info", Constants.IIIF_NS);

        width.appendChild(Integer.toString(aWidth));
        height.appendChild(Integer.toString(aHeight));
        id.appendChild(aID);

        myInfoDoc = new Document(root);
        root.appendChild(id);
        root.appendChild(width);
        root.appendChild(height);

        myLevel = aLevel;
    }

    /**
     * Gets the image's identifier.
     * 
     * @return The image's identifier
     */
    public String getIdentifier() {
        return getValue("identifier");
    }

    /**
     * Gets the image's height.
     * 
     * @return The height of the image
     */
    public int getHeight() {
        return Integer.parseInt(getValue("height"));
    }

    /**
     * Gets the image's width.
     * 
     * @return The width of the image
     */
    public int getWidth() {
        return Integer.parseInt(getValue("width"));
    }

    /**
     * Adds the supplied format to the list of handled formats.
     * 
     * @param aFormat A format to add to the supported list
     */
    public void addFormat(final String aFormat) {
        final Element root = myInfoDoc.getRootElement();
        final Elements elements = root.getChildElements("formats", Constants.IIIF_NS);
        final Element format = new Element("format", Constants.IIIF_NS);
        Element formats;

        if (elements.size() > 0) {
            formats = elements.get(0);
        } else {
            formats = new Element("formats", Constants.IIIF_NS);
            root.appendChild(formats);
        }

        format.appendChild(aFormat);
        formats.appendChild(format);
    }

    /**
     * Gets the list of supported formats.
     * 
     * @return The list of supported formats
     */
    public List<String> getFormats() {
        return getValues("format");
    }

    /**
     * Gets the XML representation of the image's metadata.
     * 
     * @return The XML representation of the image's metadata
     */
    public String toXML() {
        return myInfoDoc.toXML();
    }

    /**
     * Gets the string representation of the image's metadata.
     * 
     * @return The string representation of the image's metadata
     */
    @Override
    public String toString() {
        return myInfoDoc.toXML();
    }

    /**
     * Gets the JSON representation of the image's metadata.
     * 
     * @param aService The IIIF service
     * @param aPrefix The IIIF prefix
     * @return The JSON representation of the image's metadata
     */
    public String toJSON(final String aService, final String aPrefix) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.createObjectNode();
        final ArrayNode formats, scaleFactors;
        final String id = URLEncode.pathSafetyEncode(getIdentifier());

        rootNode.put("@context", "http://library.stanford.edu/iiif/image-api/1.1/context.json");
        rootNode.put("@id", aService + "/" + aPrefix + "/" + id);
        rootNode.put("width", getWidth());
        rootNode.put("height", getHeight());

        scaleFactors = rootNode.arrayNode();

        int start = 1;
        for (int index = 0; index < myLevel; index++) {
            //scaleFactors.add(index + 1);
            scaleFactors.add(start);
            start >>= 1;
        }

        rootNode.put("scale_factors", scaleFactors);
        rootNode.put("tile_width", 256); // TODO: provide other tile size options?
        rootNode.put("tile_height", 256);

        formats = rootNode.arrayNode();

        for (final String format : getFormats()) {
            formats.add(format);
        }

        rootNode.put("formats", formats);
        rootNode.put("qualities", rootNode.arrayNode().add("native"));
        rootNode.put("profile", Constants.IIIF_URL + "1.1/compliance.html#level1");

        return mapper.writeValueAsString(rootNode);
    }

    /**
     * Serializes the image info the supplied output stream.
     * 
     * @param aOutputStream The output stream to which the image info should be serialized
     * @throws IOException If there is a problem reading or writing the image info
     */
    public void toStream(final OutputStream aOutputStream) throws IOException {
        new Serializer(aOutputStream).write(myInfoDoc);
    }

    private List<String> getValues(final String aName) {
        final ArrayList<String> list = new ArrayList<String>();
        final Element root = myInfoDoc.getRootElement();
        final Elements elements = root.getChildElements();

        for (int eIndex = 0; eIndex < elements.size(); eIndex++) {
            final Element element = elements.get(eIndex);
            final Elements children = element.getChildElements(aName, Constants.IIIF_NS);

            if (children.size() > 0) {
                for (int cIndex = 0; cIndex < children.size(); cIndex++) {
                    list.add(children.get(cIndex).getValue());
                }

                break;
            }
        }

        return list;
    }

    private String getValue(final String aName) {
        final Element root = myInfoDoc.getRootElement();
        final Elements elements = root.getChildElements(aName, Constants.IIIF_NS);

        if (elements.size() > 0) {
            return elements.get(0).getValue();
        }

        return null;
    }

}
