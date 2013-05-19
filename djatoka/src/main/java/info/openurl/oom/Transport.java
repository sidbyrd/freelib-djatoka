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
package info.openurl.oom;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

/**
 * Transports are responsible for parsing information contained
 * in a HttpServletRequest and representing it in the OpenURL model.
 * Each implementation of this interface should be able to deal with
 * a particular pattern of HTTP requests.
 * <p />
 * Analysis: I didn't fully understand or appreciate Transports for
 * the longest time. I now understand that they represent the class
 * responsible for taking an HTTP request and transforming it into
 * OpenURL terms. This means it is possible to create Transports
 * capable of interpreting any web service request in existence.
 * <p />
 * Hallelujah! I don't have to worry about url_ver=Z39.88-2004
 * and rft_id's anymore!
 * 
 * @author Jeffrey A. Young
 */
public interface Transport {
	/**
	 * @return a Transport identifier from the OpenURL Registry
	 * @throws URISyntaxException 
	 */
	public URI getTransportID() throws URISyntaxException;
	
	/**
	 * Transforms an HttpServletRequest into an equivalent
	 * OpenURLRequest representation.
	 * 
	 * @param processor - this is required because we want
	 * to preserve the option of replacing one OOM implementation
	 * with another. 
	 * @param req the HTTP request as it was received from the client
	 * @return the entire request represented in the OpenURL model
	 * @throws OpenURLException 
	 */
	public OpenURLRequest toOpenURLRequest(
			OpenURLRequestProcessor processor,
			HttpServletRequest req)
	throws OpenURLException;
}