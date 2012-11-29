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
package org.oclc.oomRef.descriptors;

import info.openurl.oom.descriptors.ByValueMetadataXml;

import java.net.URI;

import org.w3c.dom.Document;

/**
 * A <em>Descriptor</em> that specifies properties of an <em>Entity</em>
 * by the combination of: (1) a URI reference to a <em>Metadata
 * Format</em> and (2) a particular instance of metadata about the
 * <em>Entity</em>, expressed according to the indicated <em>Metadata
 * Format</em>.
 * 
 * @author Jeffrey A. Young
 */
public class ByValueMetadataXmlImpl implements ByValueMetadataXml {
	private URI val_fmt;
    private Document xmlDoc;
	
	/**
	 * Constructs a By-Value Metadata descriptor
	 * 
	 * @param val_fmt A URI reference to a <em>Metadata Format</em>.
	 * will be extracted according to the specified prefix.
	 * @param xmlDoc the Document representation of the record
	 */
	public ByValueMetadataXmlImpl(URI val_fmt, Document xmlDoc) {
		this.val_fmt = val_fmt;
        this.xmlDoc = xmlDoc;
	}

	public URI getValFmt() {
		return val_fmt;
	}

    public Document getDocument() {
        return xmlDoc;
    }
}
