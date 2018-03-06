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
package de.fraunhofer.fokus.fuzzing.fuzzino.request.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.fraunhofer.fokus.fuzzing.fuzzino.LibraryType;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.AbstractRequest;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.Generator;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.RequestFactory;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.StringRequest;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.StringSpecification;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.ValidValuesSection;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.IllegalRequestFormat;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.ResponseFactory;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.WarningsSection;
import de.fraunhofer.fokus.fuzzing.fuzzino.util.ValidationResult;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class StringRequestImpl extends AbstractRequest implements StringRequest {

	private static final long serialVersionUID = 352352153965419478L;
	protected StringSpecification specification;
	protected List<Generator> generators;
	protected ValidValuesSection validValues;
	protected transient ValidationResult validationResult = null;
	
	@XmlElement(name = "noGenerators")
	protected boolean useNoGenerators = false;

	@Override
	@XmlElement(name = "specification", type=StringSpecificationImpl.class)
	public StringSpecification getSpecification() {
		return specification;
	}

	@Override
	public void setSpecification(StringSpecification value) {
		specification = value;
	}

	@Override
	@XmlElement(name="generator", type=GeneratorImpl.class)
	public List<Generator> getRequestedGenerators() {
		if (generators == null) {
			generators = new ArrayList<>();
		}
		return generators;
	}

	@Override
	public void setRequestedGenerators(List<Generator> value) {
		getRequestedGenerators().addAll(value);
	}

	@Override
	public void addRequestedGenerator(Generator value) {
		getRequestedGenerators().add(value);
	}

	@Override
	@XmlElement(name = "validValues",type=ValidValuesSectionImpl.class)
	public ValidValuesSection getValidValuesSection() {
		return validValues;
	}

	@Override
	public void setValidValuesSection(ValidValuesSection value) {
		validValues = value;
	}

	@Override
	public void addValidValue(String value) {
		if (validValues == null) {
			validValues = RequestFactory.INSTANCE.createValidValues();
		}
		validValues.addValue(value);
	}

	@Override
	public ValidationResult validate() {
		if (validationResult == null) {
			ValidationResult validStringRequest = validateMaxValues();
			ValidationResult validSpecification = ValidationResult.VALID;
			if (getSpecification() != null) {
				validSpecification = getSpecification().validate();
			}

			validationResult = validStringRequest.merge(validSpecification);
		}

		return validationResult;
	}

	@Override
	public boolean isValid() {
		return validate().isValid();
	}

	private ValidationResult validateMaxValues() {
		boolean validMaxValues = getMaxValues() > 0;
		WarningsSection warnings = null;

		if (!validMaxValues) {
			warnings = ResponseFactory.INSTANCE.createWarnings();
			IllegalRequestFormat illegalMaxValues = ResponseFactory.INSTANCE
					.createIllegalRequestFormatWithWrongAttribute("string",
							"maxValues");
			warnings.getIllegalRequestFormats().add(illegalMaxValues);
		}

		return new ValidationResult(validMaxValues, warnings);
	}

	@Override
	public String toString() {
		return "[StringRequest name:" + name + " id:" + id + "]";
	}

	@Override
	public boolean useNoGenerators() {
		return useNoGenerators;
	}

	@Override
	public void setUseNoGenerators(boolean value) {
		useNoGenerators = value;
	}

	@Override
	public LibraryType getLibraryType() {
		return LibraryType.STRING;
	}

}
