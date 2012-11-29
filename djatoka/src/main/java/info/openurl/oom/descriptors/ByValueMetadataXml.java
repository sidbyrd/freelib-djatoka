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
package info.openurl.oom.descriptors;

import java.net.URI;
import org.w3c.dom.Document;

/**
 * This class represents the By-Value Metadata Descriptor described
 * in section 5.2.3 of the 
 * <a href="http://alcme.oclc.org/openurl/docs/pdf/z39_88_2004.pdf">OpenURL 1.0 spec</a>.
 * @author Jeffrey A. Young
 */
public interface ByValueMetadataXml {
	/**
     * Get an identifier for the type of key/value pairs in the fieldMap
     * 
	 * @return a URI indicating the <a href="http://alcme.oclc.org/openurl/servlet/OAIHandler?verb=ListRecords&metadataPrefix=oai_dc&set=Core:Metadata+Formats">metadata format</a>
     * represented in the fieldMap.
	 */
	public URI getValFmt();
    
	/**
     * Get the metadata elements
     * 
	 * @return a Map of key/value metadata elements
	 */
	public Document getDocument();
}
