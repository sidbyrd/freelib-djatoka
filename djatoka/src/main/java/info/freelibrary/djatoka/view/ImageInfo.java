package info.freelibrary.djatoka.view;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

import info.freelibrary.djatoka.IIIFInterface;

public class ImageInfo implements IIIFInterface {

    private Document myInfoDoc;

    public ImageInfo(String aID, int aHeight, int aWidth) {
	Element id = new Element("identifier", IIIF_NS);
	Element height = new Element("height", IIIF_NS);
	Element width = new Element("width", IIIF_NS);
	Element root = new Element("info", IIIF_NS);

	width.appendChild(Integer.toString(aWidth));
	height.appendChild(Integer.toString(aHeight));
	id.appendChild(aID);

	myInfoDoc = new Document(root);
	root.appendChild(id);
	root.appendChild(width);
	root.appendChild(height);
    }

    public String getIdentifier() {
	return getValue("identifier");
    }

    public int getHeight() {
	return Integer.parseInt(getValue("height"));
    }

    public int getWidth() {
	return Integer.parseInt(getValue("width"));
    }

    public void addFormat(String aFormat) {
	Element root = myInfoDoc.getRootElement();
	Elements elements = root.getChildElements("formats", IIIF_NS);
	Element format = new Element("format", IIIF_NS);
	Element formats;

	if (elements.size() > 0) {
	    formats = elements.get(0);
	}
	else {
	    formats = new Element("formats", IIIF_NS);
	    root.appendChild(formats);
	}

	format.appendChild(aFormat);
	formats.appendChild(format);
    }

    public List<String> getFormats() {
	return getValues("format");
    }

    public String toXML() {
	return myInfoDoc.toXML();
    }

    public String toString() {
	return myInfoDoc.toXML();
    }

    public String toJSON() {
	throw new UnsupportedOperationException("toJSON() not yet implemented");
    }

    public void toStream(OutputStream aOutStream) throws IOException {
	new Serializer(aOutStream).write(myInfoDoc);
    }

    private List<String> getValues(String aName) {
	ArrayList<String> list = new ArrayList<String>();
	Element root = myInfoDoc.getRootElement();
	Elements elements = root.getChildElements();

	for (int eIndex = 0; eIndex < elements.size(); eIndex++) {
	    Element element = elements.get(eIndex);
	    Elements children = element.getChildElements(aName, IIIF_NS);

	    if (children.size() > 0) {
		for (int cIndex = 0; cIndex < children.size(); cIndex++) {
		    list.add(children.get(cIndex).getValue());
		}

		break;
	    }
	}

	return list;
    }

    private String getValue(String aName) {
	Element root = myInfoDoc.getRootElement();
	Elements elements = root.getChildElements(aName, IIIF_NS);

	if (elements.size() > 0) {
	    return elements.get(0).getValue();
	}

	return null;
    }
}
