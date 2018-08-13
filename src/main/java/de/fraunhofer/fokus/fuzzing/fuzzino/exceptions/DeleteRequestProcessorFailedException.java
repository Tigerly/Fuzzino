//   Copyright 2012-2013 Fraunhofer FOKUS
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
package de.fraunhofer.fokus.fuzzing.fuzzino.exceptions;

/**
 * This exception indicates that a request processor could not be deleted when trying to
 * perform a close request command. 
 * 
 * @author Martin Schneider (martin.schneider@fokus.fraunhofer.de)
 *
 */
public class DeleteRequestProcessorFailedException extends Exception {
	
	private static final long serialVersionUID = -6516365540093449516L;

	public DeleteRequestProcessorFailedException() {
		super();
	}
	
	public DeleteRequestProcessorFailedException(String message) {
		super(message);
	}

}