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

/**
 * A fatal error occurred within the OpenURL infrastructure.
 * @see OpenURLResponse for non-fatal response conditions
 * 
 * @author Jeffrey A. Young
 */
public class OpenURLException extends Exception {
	/**
	 * Initial version
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Indicates a fatal condition
	 * 
	 * @param message provides a clue to the conditions of the problem
	 * @param e the original exception
	 */
	public OpenURLException(String message, Exception e) {
		super(message, e);
	}
    
    /**
     * Indicates a fatal condition
     * 
     * @param message provides a clue to the conditions of the problem
     */
    public OpenURLException(String message) {
        super(message);
    }
}
