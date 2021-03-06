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
package de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.generators.string.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import de.fraunhofer.fokus.fuzzing.fuzzino.util.ResourcePath;
import de.fraunhofer.fokus.fuzzing.fuzzino.util.ResourceResolver;

public class StringContainerFileLoader {

	private String filePath;
			
	public StringContainerFileLoader(String fileName) {
		this.filePath = ResourcePath.FUZZING_VALUES + fileName;
	}

	public void loadFileIntoStringContainer(StringContainer stringContainer) {
		try (InputStreamReader streamReader = new InputStreamReader(new ResourceResolver().loadResource(filePath))) {
			BufferedReader reader = new BufferedReader(streamReader);
			
			String line = reader.readLine();
			while(line != null) {
				stringContainer.add(line);
				line = reader.readLine();
			}
		}
		catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
