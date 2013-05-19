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
package info.openurl.oom.entities;

/**
 * This interface is an abstraction for the categories of information
 * represented in a web service request. In layman's terms, these
 * categories are synonymous with who, what, where, why, when, etc.
 * 
 * @author Jeffrey A. Young
 */
public interface Entity {
	/**
	 * Get a sequence of descriptors for this entity. Use
	 * the instanceof operator to select desired descriptor
	 * types from the list.
	 * <p />
	 * Analysis: The OpenURL specification defines a set of
	 * abstractions to represent descriptors: Identifier (URI),
	 * By Value Metadata, By Reference Metadata, and PrivateData
	 * (everything else). To be fair, these represent categories
	 * that are useful for the handful of transports defined by
	 * the spec in a language independent way. In contrast, OOM-J
	 * doesn't need to be operationally constrained by language
	 * independence and shouldn't feel obligated to give special
	 * status to the quirky descriptors used by legacy Transports.
     * <p />
	 * Normal Transports can completely ignore this whole mess:
	 * any Java Object is a legitimate descriptor. Unfortunately,
	 * the XML Schemas used to document OpenURL applications
	 * reflect this bias, but it is easy enough to dumb our Java
	 * Object descriptors down when the time comes to register
	 * the application.
     * <p />
	 * As a consequence, the OOM model does NOT provide a Descriptor
	 * interface to encapsulate the four types of descriptors
	 * enumerated in the OpenURL 1.0 spec: Identifier, By
	 * Value Metadata, By Reference Metadata, and PrivateData.
	 * As described below, every Java Object qualifies
	 * as a legitimate descriptor one way or another, so why
	 * create an abstraction for something that is already abstract?
     * <p />
	 * Identifier is <em>not</em> modeled in OOM because it is
	 * functionally equivalent to the URI class and there is
	 * no sense in creating an Identifier abstraction for a single
	 * class of objects.
     * <p />
	 * Interfaces <em>are</em> provided for By Value Metadata
	 * and By Reference Metadata because they are generally
	 * useful and, unlike URI, there is no native Java class
	 * that represents them unambiguously.
     * <p />
	 * PrivateData is <em>not</em> modeled because it is
	 * functionally equivalent to Java's Object class and
	 * there is no good reason to create an abstraction
	 * for something that is already abstract.
	 * 
	 * @return an array of Objects that describe this entity
	 */
	public Object[] getDescriptors();
    
    public void addDescriptor(Object descriptor);
    
	/**
	 * This utility method allows Service classes to select a
	 * subset of descriptors that are instances of the specified
	 * Class.
	 * 
	 * @param c the Class of descriptors
	 * @return an array of Objects that describe this entity that
	 * are instances of the specified Class
     * @deprecated Introspection isn't available in some languages, so
     * this method will have to be removed before the official release. 
	 */
	public Object[] getDescriptors(Class c);
}
