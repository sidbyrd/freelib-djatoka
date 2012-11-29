/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oclc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * General purpose static methods for manipulating XML.
 * 
 * @author Jeffrey A. Young
 * @deprecated use info.openurl.oom.util.XMLHelper instead
 */
public class XMLHelper {
	
//	private static HashMap builderMap = new HashMap();
	private static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	private static TransformerFactory tFactory = TransformerFactory.newInstance();
//	private static HashMap transformerMap = new HashMap();
	static {
		dbFactory.setNamespaceAware(true);
	}
	
	private static Element xmlnsEl;
	static {
		try {
			DOMImplementation impl =
				getThreadedDocumentBuilder().getDOMImplementation();
			Document xmlnsDoc = impl.createDocument("uri:foo", "foo:xmlnsDoc",
					null);
			xmlnsEl = xmlnsDoc.getDocumentElement();
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:foo", "uri:foo");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:ctx", "info:ofi/fmt:xml:xsd:ctx");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai", "http://www.openarchives.org/OAI/2.0/");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:dc", "http://purl.org/dc/elements/1.1/");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:srw", "http://www.loc.gov/zing/srw/");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:mets", "http://www.loc.gov/METS/");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:xlink", "http://www.w3.org/TR/xlink");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:config", "info:sid/localhost:CollectionSimpleSchemas:config");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:purl", "info:sid/localhost:CollectionSimpleSchemas:purl");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:reg", "http://info-uri.info/registry");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:wr", "http://errol.oclc.org/oai:xmlregistry.oclc.org:errol/WikiRepository");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:pearsIdx", "http://www.oclc.org/pears/");
			xmlnsEl.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xmlns:oomRef", "info:collections/oomImpls/oomRef");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a thread-safe Transformer without an assigned transform. This is useful
	 * for transforming a DOM Document into XML text.
	 * @param omitXmlDeclaration 
	 * @return an "identity" Transformer assigned to the current thread
	 * @throws TransformerConfigurationException
	 */
	public static Transformer getThreadedIdentityTransformer(
			boolean omitXmlDeclaration)
	throws TransformerConfigurationException {
		return getThreadedTransformer(omitXmlDeclaration,
//                transformerMap,
                null,
				(String) null);
	}
	
	/**
	 * Get a thread-safe Transformer without an assigned transform. This is useful
	 * for transforming a DOM Document into XML text.
	 * @param omitXmlDeclaration
	 * @param standalone
	 * @return an Identity Transformer
	 * @throws TransformerConfigurationException
	 */
	public static Transformer getThreadedIdentityTransformer(
			boolean omitXmlDeclaration, boolean standalone)
	throws TransformerConfigurationException {
		return getThreadedTransformer(omitXmlDeclaration, standalone,
//				transformerMap,
                null,
                (String) null);
	}
	
	private static Transformer getThreadedTransformer(
			boolean omitXmlDeclaration,
			boolean standalone, Map threadMap, String xslURL)
	throws TransformerConfigurationException {
		Thread currentThread = Thread.currentThread();
		Transformer transformer = null;
		if (threadMap != null) {
			transformer = (Transformer) threadMap.get(currentThread);
		}
		if (transformer == null) {
			if (xslURL == null) {
				transformer = tFactory.newTransformer(); // "never null"
			} else {
				transformer = tFactory.newTransformer(new StreamSource(xslURL));
			}
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			if (threadMap != null) {
				threadMap.put(currentThread, transformer);
			}
		}
		transformer.setOutputProperty(OutputKeys.STANDALONE,
				standalone?"yes":"no");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
				omitXmlDeclaration?"yes":"no");
		return transformer;
	}
	
	/**
	 * Get a thread-safe Transformer.
	 * @param omitXmlDeclaration
	 * @param threadMap
	 * @param xslURL
	 * @return a thread-safe Transformer
	 * @throws TransformerConfigurationException
	 */
	public static Transformer getThreadedTransformer(boolean omitXmlDeclaration,
			Map threadMap, String xslURL)
	throws TransformerConfigurationException {
		return getThreadedTransformer(omitXmlDeclaration, true, threadMap,
				xslURL);
	}
	
	/**
	 * Get an Element with some handy xmlns attributes defined.
	 * This comes in handy for calling XPathAPI methods.
	 * @return an Element containing various xmlns attributes.
	 */
	public static Element getXmlnsEl() {
		return xmlnsEl;
	}
	
	/**
	 * Grab the Document found at the specified URL.
	 * 
	 * @param ref the URL location of an XML document.
	 * @return a Document loaded from the specified URL.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document parse(String ref)
	throws SAXException, IOException, ParserConfigurationException {
		String protocol = ref.split(":", 2)[0];
		return parse(protocol, new InputSource(ref));
	}
	
	/**
	 * Grab the Document found at the specified URL.
	 * 
	 * @param protocol
	 * @param is
	 * @return a Document loaded from the specified URL.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document parse(String protocol, InputSource is)
	throws SAXException, IOException, ParserConfigurationException {
		if ("http".equals(protocol)) {
			return getThreadedDocumentBuilder().parse(is);
		}
		throw new IOException("Protocol handler not implemented yet: " + protocol);
	}
	
	/**
	 * Grab the Document loaded from the specified InputSource.
	 * 
	 * @param is
	 * @return a Document loaded from the specified InputSource
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document parse(InputSource is)
	throws SAXException, IOException, ParserConfigurationException {
		return getThreadedDocumentBuilder().parse(is);
	}
	
	/**
	 * Grab the Document loaded from the specified InputStream
	 * 
	 * @param is
	 * @return a Document loaded from the specified InputStream.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document parse(InputStream is)
	throws SAXException, IOException, ParserConfigurationException {
		return getThreadedDocumentBuilder().parse(is);
	}
	
	/**
	 * Get a thread-safe DocumentBuilder
	 * @return a namespaceAware DocumentBuilder assigned to the current thread
	 * @throws ParserConfigurationException
	 */
	public static DocumentBuilder getThreadedDocumentBuilder()
	throws ParserConfigurationException {
//		Thread currentThread = Thread.currentThread();
//		DocumentBuilder builder =
//			(DocumentBuilder) builderMap.get(currentThread);
//		if (builder == null) {
//			builder = dbFactory.newDocumentBuilder();
//			builderMap.put(currentThread, builder);
//		}
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
		return builder;
	}
	
	/**
	 * XML-encode a String
	 * @param value the String to be XML-encoded
	 * @return the XML-encoded String
	 */
	public static String encode(String value) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<value.length(); ++i) {
			char c = value.charAt(i);
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Transform a DOM Node into an XML String.
	 * @param node
	 * @return an XML String representation of the specified Node
	 * @throws TransformerException
	 */
	public static String toString(Node node)
	throws TransformerException {
		return toString(node, true);
	}
	
	/**
	 * Transform a DOM Node into an XML String
	 * @param node
	 * @param omitXMLDeclaration
	 * @return an XML String representation of the specified Node
	 * @throws TransformerException
	 */
	public static String toString(Node node, boolean omitXMLDeclaration)
	throws TransformerException {
		StringWriter writer = new StringWriter();
		Transformer transformer =
			getThreadedIdentityTransformer(omitXMLDeclaration);
		Source source = new DOMSource(node);
		Result result = new StreamResult(writer);
		transformer.transform(source, result);
		return writer.toString();
	}
	
	/**
	 * Remove named nodes of the specified nodeType from the
	 * specified node.
	 * @param node the node to be cleaned.
	 * @param nodeType the type of nodes to be removed.
	 * @param name the name of nodes to be removed.
	 */
	public static void removeAll(Node node, short nodeType, String name) {
		if (node.getNodeType() == nodeType &&
				(name == null || node.getNodeName().equals(name))) {
			node.getParentNode().removeChild(node);
		} else {
			// Visit the children
			NodeList list = node.getChildNodes();
			for (int i=0; i<list.getLength(); i++) {
				removeAll(list.item(i), nodeType, name);
			}
		}
	}
}
