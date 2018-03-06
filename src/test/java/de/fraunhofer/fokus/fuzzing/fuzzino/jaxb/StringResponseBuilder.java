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
package de.fraunhofer.fokus.fuzzing.fuzzino.jaxb;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.fraunhofer.fokus.fuzzing.fuzzino.FuzzedValue;
import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.FuzzingHeuristic;
import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.generators.string.AllBadStringsGenerator;
import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.generators.string.BadDateGenerator;
import de.fraunhofer.fokus.fuzzing.fuzzino.heuristics.operators.string.DelimiterOperator;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.impl.StringSpecificationImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.FuzzedValuesByGenerators;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.FuzzedValuesByOperators;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.GeneratorSpecificFuzzedValues;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.OperatorSpecificFuzzedValues;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.StringResponse;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.WarningsSection;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.FuzzedValuesByGeneratorsImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.FuzzedValuesByOperatorsImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.GeneratorSpecificFuzzedValuesImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.IllegalGeneratorImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.IllegalOperatorImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.IllegalRequestFormatImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.OperatorSpecificFuzzedValuesImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.StringResponseImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.WarningsSectionImpl;

public class StringResponseBuilder {

	public static StringResponse initStringResponse() {
		StringResponse resp = new StringResponseImpl();
		resp.setGeneratorBasedSection(initGeneratorBaseSection());
		resp.setId("my stringresponse id");
		resp.setMoreValuesAvailable(true);
		resp.setName("string response name");
		resp.setOperatorBasedSection(initOperatorBasedSection());
		resp.setSeed(12345);
		resp.setWarningsSection(initWarningsSection());
		return resp;
	}

	static WarningsSection initWarningsSection() {
		WarningsSection section = new WarningsSectionImpl();
		section.setMoreValues(true);
		section.getIllegalGenerators().add(new IllegalGeneratorImpl("illegal gen name 1","reason 1"));
		section.getIllegalGenerators().add(new IllegalGeneratorImpl("illegal gen name 2","reason 2"));
		section.getIllegalOperators().add(new IllegalOperatorImpl("illegal op name 1","reason 1"));
		section.getIllegalRequestFormats().add(new IllegalRequestFormatImpl("att1","elem 1","missing attribute 1","missing elem 1"));
		return section;
	}

	private static FuzzedValuesByOperators<String> initOperatorBasedSection() {
		FuzzedValuesByOperators<String> a = new FuzzedValuesByOperatorsImpl<String>();
		a.setOperatorSpecificSections(initOperatorSpecificSection());
		return a;
	}

	private static Collection<OperatorSpecificFuzzedValues<String>> initOperatorSpecificSection() {
		OperatorSpecificFuzzedValues<String> vals = new OperatorSpecificFuzzedValuesImpl<String>();
		vals.setFuzzedValues(initFuzzedValuesByOperator(initOperator()));
		vals.setName("name 1");
		OperatorSpecificFuzzedValues<String> vals2 = new OperatorSpecificFuzzedValuesImpl<String>();
		vals2.setFuzzedValues(initFuzzedValuesByOperator(initOperator()));
		vals2.setName("name 2");
		return Arrays.asList(vals,vals2);
	}

	private static List<FuzzedValue<String>> initFuzzedValuesByOperator(FuzzingHeuristic operator) {
		return Arrays.asList(
				new FuzzedValue<String>("fuzzed value 1", "vase val 1",operator),
				new FuzzedValue<String>("fuzzed value 2", "base val 2",operator)
			);
	}

	private static FuzzingHeuristic initOperator() {
		return new DelimiterOperator(Arrays.asList("1","2"), new StringSpecificationImpl(), 25);
	}

	private static FuzzedValuesByGenerators<String> initGeneratorBaseSection() {
		FuzzedValuesByGenerators<String> a = new FuzzedValuesByGeneratorsImpl<>();
		a.setGeneratorSpecificSections(initGenSpecificSection());
		return a;
	}

	private static Collection<GeneratorSpecificFuzzedValues<String>> initGenSpecificSection() {
		GeneratorSpecificFuzzedValues<String> vals = new GeneratorSpecificFuzzedValuesImpl<String>();
		vals.setFuzzedValues(initFuzzedValues(new AllBadStringsGenerator(new StringSpecificationImpl(), 13)));
		vals.setName("name");
		GeneratorSpecificFuzzedValues<String> vals2 = new GeneratorSpecificFuzzedValuesImpl<String>();
		vals2.setFuzzedValues(initFuzzedValues(new BadDateGenerator(new StringSpecificationImpl(), 20)));
		vals2.setName("name 2");
		return Arrays.asList(vals,vals2);
	}

	private static List<FuzzedValue<String>> initFuzzedValues(FuzzingHeuristic heuristic) {
		return Arrays.asList(
				new FuzzedValue<String>("fuzzed value 1", heuristic),
				new FuzzedValue<String>("fuzzed value 2", heuristic)
			);
	}

}
