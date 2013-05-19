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
package org.oclc.oomRef.config;

import info.openurl.oom.OpenURLRequestProcessor;
import info.openurl.oom.Service;
import info.openurl.oom.Transport;
import info.openurl.oom.util.XMLHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * @author Jeffrey A. Young
 *
 * TODO Describe type
 */
public class OpenURLConfig implements info.openurl.oom.config.OpenURLConfig {
    private ServletConfig servletConfig;
	private static Document oomConfig;
	static {
		try {
			oomConfig = XMLHelper.parse(Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream("oomRef.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param config
	 */
	public OpenURLConfig(ServletConfig config) {
        this.servletConfig = config;
    }
    
    /**
     * @return
     */
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public Transport[] getTransports()
	throws TransformerException, ClassNotFoundException, SecurityException,
	NoSuchMethodException, IllegalArgumentException, InstantiationException,
	IllegalAccessException, InvocationTargetException {
		ArrayList transports = new ArrayList();
		Element xmlnsEl = XMLHelper.getXmlnsEl();
		NodeIterator nodeIter = XPathAPI.selectNodeIterator(oomConfig,
				"/oomRef:config/oomRef:transportMap/oomRef:transport",
				xmlnsEl);
		Node node;
        while ((node = nodeIter.nextNode()) != null) {
            ClassConfig classConfig = new ClassConfig(node);
            String transportClassName = classConfig.getClassName();
            Class transportClass = Class.forName(transportClassName);
            Constructor transportConstructor = null;
            try {
                transportConstructor = transportClass.getConstructor(new Class[] {
                        info.openurl.oom.config.OpenURLConfig.class,
                        info.openurl.oom.config.ClassConfig.class });
            } catch (NoSuchMethodException e) {
                // Uh Oh. Somebody extended OOMRef-J's HowImpl directly. 
                transportConstructor = transportClass.getConstructor(new Class[] {
                        org.oclc.oomRef.config.OpenURLConfig.class,
                        org.oclc.oomRef.config.ClassConfig.class });
            }
            Transport transport =
            	(Transport) transportConstructor.newInstance(new Object[] { this,
            			classConfig });
            transports.add(transport);
        }
        return (Transport[]) transports.toArray(new Transport[transports.size()]);
	}

	public Service getService(URI uri)
	throws TransformerException, ClassNotFoundException, SecurityException,
	NoSuchMethodException, IllegalArgumentException, InstantiationException,
	IllegalAccessException, InvocationTargetException {
		Element xmlnsEl = XMLHelper.getXmlnsEl();
        Node node = XPathAPI.selectSingleNode(oomConfig,
                "/oomRef:config/oomRef:serviceMap/oomRef:service[@ID='" +
                uri.toString() +
                "']",
                xmlnsEl);
        if (node != null) {
            ClassConfig classConfig = new ClassConfig(node);
            String className = XPathAPI.eval(node, "oomRef:className", xmlnsEl).str();
            Class serviceClass = Class.forName(className);
            Constructor serviceConstructor = null;
            try {
                serviceConstructor = serviceClass.getConstructor(
                        new Class[] { info.openurl.oom.config.OpenURLConfig.class,
                                info.openurl.oom.config.ClassConfig.class});
            } catch (NoSuchMethodException e) {
                // Uh Oh. Somebody implemented an OOMRef-J class directly.
                serviceConstructor = serviceClass.getConstructor(
                        new Class[] { org.oclc.oomRef.config.OpenURLConfig.class,
                                org.oclc.oomRef.config.ClassConfig.class});
            }
            return (Service) serviceConstructor.newInstance(
                    new Object[] { this, classConfig });
        }
        
        return null;
	}
	
    public Service getService(String className)
    throws TransformerException, SecurityException, NoSuchMethodException,
    IllegalArgumentException, InstantiationException,
    IllegalAccessException, InvocationTargetException {
        try {
            Node node = XPathAPI.selectSingleNode(oomConfig,
                    "/oomRef:config/oomRef:serviceMap/oomRef:service[oomRef:className='" +
                    className +
                    "']",
                    XMLHelper.getXmlnsEl());
            ClassConfig classConfig = new ClassConfig(node);
        	Class serviceClass = Class.forName(className);
        	Constructor serviceConstructor = null;
            try {
                serviceConstructor = serviceClass.getConstructor(
                        new Class[] { info.openurl.oom.config.OpenURLConfig.class,
                                info.openurl.oom.config.ClassConfig.class});
            } catch (NoSuchMethodException e) {
                // Uh Oh. Somebody extended the ServiceImpl directly.
                serviceConstructor = serviceClass.getConstructor(
                        new Class[] { org.oclc.oomRef.config.OpenURLConfig.class,
                                org.oclc.oomRef.config.ClassConfig.class});
            }
        	return (Service) serviceConstructor.newInstance(
        			new Object[] { this, classConfig });
        } catch (ClassNotFoundException e) {
            // do nothing
        }
        
        return null;
    }

	public OpenURLRequestProcessor getProcessor()
	throws TransformerException, ClassNotFoundException, InstantiationException,
	IllegalAccessException {
        Node node = XPathAPI.selectSingleNode(oomConfig,
                "/oomRef:config/oomRef:processor",
                XMLHelper.getXmlnsEl());
        ClassConfig classConfig = new ClassConfig(node);
        String className = classConfig.getClassName();
        Class c = Class.forName(className);
        return (OpenURLRequestProcessor) c.newInstance();
	}

    public String getArg(String key) throws TransformerException {
        String xpath = new StringBuffer("/oomRef:config/oomRef:args/oomRef:")
        .append(key)
        .toString();
        return XPathAPI.eval(oomConfig, xpath, XMLHelper.getXmlnsEl()).str();
    }

    public String[] getArgs(String key) throws TransformerException {
        ArrayList args = new ArrayList();
        
        String xpath = new StringBuffer("/oomRef:config/oomRef:args/oomRef:")
        .append(key)
        .toString();
        NodeIterator iter =
            XPathAPI.selectNodeIterator(oomConfig, xpath,
                    XMLHelper.getXmlnsEl());
        Node node;
        while ((node = iter.nextNode()) != null) {
            args.add(XPathAPI.eval(node, ".").str());
        }
        return (String[]) args.toArray(new String[args.size()]);
    }

    public Map getArgs() throws TransformerException {
        Map map = new HashMap();

        if (oomConfig != null) {
            NodeIterator iter =
                XPathAPI.selectNodeIterator(oomConfig, "/oomRef:config/oomRef:args/*",
                        XMLHelper.getXmlnsEl());
            Node node;
            while ((node = iter.nextNode()) != null) {
                String key = XPathAPI.eval(node, "name()").str();
                map.put(key, XPathAPI.eval(node, ".").str());
            }
        }
        return map;
    }
}
