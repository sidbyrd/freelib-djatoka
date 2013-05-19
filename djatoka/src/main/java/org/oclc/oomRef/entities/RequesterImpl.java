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
package org.oclc.oomRef.entities;

import info.openurl.oom.entities.Requester;

/**
 * The resource that requests services pertaining to the
 * Referent; an <em>Entity</em> of the <em>ContextObject</em>.
 * 
 * @author Jeffrey A. Young
 */
public class RequesterImpl extends EntityImpl implements Requester {

	/**
	 * Construct a Requester.
	 * 
	 * @param descriptors Descriptor(s) for the Requester.
	 */
	public RequesterImpl(Object descriptors) {
		super(descriptors);
	}
}
