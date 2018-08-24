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
package de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.operators.string;

import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.ComputableFuzzingHeuristic;
import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.operators.SimpleFuzzingOperator;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.RequestFactory;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.StringSpecification;

public abstract class SimpleStringOperator extends SimpleFuzzingOperator<String> {
	
	private static final long serialVersionUID = 1669284663278581286L;
	protected StringSpecification stringSpec;
	
	public SimpleStringOperator(String validValue, StringSpecification stringSpec, long seed) {
		super(validValue, seed);
		if (stringSpec == null) {
			this.stringSpec = RequestFactory.INSTANCE.createStringSpecification();
		}
		else {
			this.stringSpec = stringSpec;
		}
	}
	
	public SimpleStringOperator(String validValue, StringSpecification stringSpec, long seed, ComputableFuzzingHeuristic<?> owner) {
		super(validValue, seed, owner);
		if (stringSpec == null) {
			this.stringSpec = RequestFactory.INSTANCE.createStringSpecification();
		}
		else {
			this.stringSpec = stringSpec;
		}
	}

}
