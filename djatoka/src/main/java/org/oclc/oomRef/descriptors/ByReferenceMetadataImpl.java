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

import info.openurl.oom.descriptors.ByReferenceMetadata;

import java.net.URI;
import java.net.URL;

/**
 * A <em>Descriptor</em> that details properties of an <em>Entity</em>
 * by the combination of: (1) a URI reference to a <em>Metadata
 * Format</em> and (2) the network location of a particular instance
 * of metadata about the <em>Entity</em>, the metadata being
 * expressed according to the indicated <em>Metadata Format</em>.
 * 
 * @author Jeffrey A. Young
 */
public class ByReferenceMetadataImpl implements ByReferenceMetadata {
	private URI ref_fmt;
	private URL ref;
	
	/**
	 * Constructs a By-Reference Metadata descriptor
	 * 
	 * @param ref_fmt A URI reference to a <em>Metadata Format</em>.
	 * @param ref The network location of a particular instance of
	 * metadata about the <em>Entity</em>, the metadata being
	 * expressed according to the indicated <em>Metadata Format</em>.
	 */
	public ByReferenceMetadataImpl(URI ref_fmt, URL ref) {
		this.ref_fmt = ref_fmt;
		this.ref = ref;
	}
	
	public URI getRefFmt() {
		return ref_fmt;
	}

	public URL getRef() {
		return ref;
	}
}
