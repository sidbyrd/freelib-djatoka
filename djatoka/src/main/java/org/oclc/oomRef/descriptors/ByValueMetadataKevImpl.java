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

import info.openurl.oom.descriptors.ByValueMetadataKev;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A <em>Descriptor</em> that specifies properties of an <em>Entity</em>
 * by the combination of: (1) a URI reference to a <em>Metadata
 * Format</em> and (2) a particular instance of metadata about the
 * <em>Entity</em>, expressed according to the indicated <em>Metadata
 * Format</em>.
 * 
 * @author Jeffrey A. Young
 */
public class ByValueMetadataKevImpl implements ByValueMetadataKev {
	private URI val_fmt;
	private Map fieldMap = new HashMap();
	
	/**
	 * Constructs a By-Value Metadata descriptor
	 * 
	 * @param val_fmt A URI reference to a <em>Metadata Format</em>.
	 * @param prefix The KEV key prefix to be extracted from the entrySet
	 * @param entrySet A set of all KEV keys from which a subset
	 * will be extracted according to the specified prefix.
	 */
	public ByValueMetadataKevImpl(URI val_fmt, String prefix, Set entrySet) {
		this.val_fmt = val_fmt;
		Iterator iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			String key = (String) entry.getKey();
			if (entry.getKey().toString().startsWith(prefix)) {
				fieldMap.put(key, entry.getValue());
			}
		}
	}

	public URI getValFmt() {
		return val_fmt;
	}
	
	public Map getFieldMap() {
		return fieldMap;
	}
}
