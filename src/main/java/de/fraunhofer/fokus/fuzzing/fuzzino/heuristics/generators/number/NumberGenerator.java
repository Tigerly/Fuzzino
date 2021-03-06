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
package de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.generators.number;

import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.ComputableListOfLists;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.IntegerSpecification;

// TODO delete because unused?

public abstract class NumberGenerator<E> extends ComputableListOfLists<E> {

	private static final long serialVersionUID = 3521362186525683748L;

	public abstract boolean isApplicableTo(IntegerSpecification spec);
	
}
