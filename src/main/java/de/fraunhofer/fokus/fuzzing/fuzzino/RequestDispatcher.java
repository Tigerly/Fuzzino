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
package de.fraunhofer.fokus.fuzzing.fuzzino;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;

import de.fraunhofer.fokus.fuzzing.fuzzino.exceptions.IllegalRequestIdException;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.CloseRequest;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.CollectionRequest;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.IntegerSpecification;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.NumberRequest;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.NumberSpecification;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.NumberType;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.Request;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.StringRequest;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.StructureRequest;
import de.fraunhofer.fokus.fuzzing.fuzzino.request.impl.RequestImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.BigIntegerResponse;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.CloseRequestConfirmation;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.ErrorResponse;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.IllegalRequestFormat;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.IntegerResponse;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.NumberResponse;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.Response;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.ResponseFactory;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.StringResponse;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.WarningsSection;
import de.fraunhofer.fokus.fuzzing.fuzzino.response.impl.ResponseImpl;
import de.fraunhofer.fokus.fuzzing.fuzzino.util.StackTrace;

/**
 * This class dispatches a request from an XML file (compliant to fuzzingRequest.xsd) to type specific request processors. 
 * These are:
 * <ul>
 * <li>for {@link NumberRequest} a {@link IntegerRequestProcessor} or a {@link FloatRequestProcessor}</li>
 * <li>for {@link StringRequest} a {@link StringRequestProcessor}</li>
 * <li>for {@link StructureRequest} a {@link StructureRequestProcessor}</li>
 * <li>for {@link CollectionRequest} a {@link CollectionRequestProcessor}</li>
 * </ul>
 * 
 * It collects all the results from the specific processors and creates a response.
 *
 * It also provides serialization of its {@link RequestProcessor}s to
 * provide requesting further values by calling the fuzzing library again and referring
 * to the original request.
 * 
 * @author Martin Schneider (martin.schneider@fokus.fraunhofer.de)
 */
public class RequestDispatcher {

	protected String requestFilename;
	protected String responseFileName;
	protected Request request;
	protected transient Response response;
	protected transient String xmlResponseString;
	
	protected boolean encodeAllCharacters = false;

	protected Throwable xmlParsingError = null;

	protected Map<UUID, StringRequestProcessor> stringRequestProcessors;
	protected Map<UUID, IntegerRequestProcessor> integerRequestProcessors;
	protected Map<UUID, BigIntegerRequestProcessor> bigIntegerRequestProcessors;
	protected Map<UUID, StructureRequest> structureRequestsById;
	protected Map<UUID, CollectionRequest> collectionRequestsById;

	/**
	 * Creates a new instance that reads a request from a file. The file must be an XML file compliant to 
	 * fuzzingRequest.xsd.
	 * 
	 * @param xmlFilenameOrString Either a name of an XML file or a string that contains an XML document.
	 */
	public RequestDispatcher(String xmlFilenameOrString) {
		init(xmlFilenameOrString, false);
	}
	
	/**
	 * Creates a new instance that reads a request from a file. The file must be an XML file compliant to 
	 * fuzzingRequest.xsd. Allows activating of encoding of all characters instead of just control
	 * characters.
	 * 
	 * @param xmlFilenameOrString Either a name of an XML file or a string that contains an XML document.
	 * @param encodeAllCharacters Set to {@code false} if only control characters shall be encoded.
	 */
	public RequestDispatcher(String xmlFilenameOrString, boolean encodeAllCharacters) {
		init(xmlFilenameOrString, encodeAllCharacters);
	}
	
	/**
	 * Performs the initialization of this RequestDispatcher.
	 * 
	 * @param xmlFilenameOrString Either a name of an XML file or a string that contains an XML document.
	 * @param encodeAllCharacters Set to {@code false} if only control characters shall be encoded.
	 */
	protected void init(String xmlFilenameOrString, boolean encodeAllCharacters) {
		this.encodeAllCharacters = encodeAllCharacters;
		if (xmlFilenameOrString.startsWith("<?xml")) {
			// a string containing an XML document
			requestFilename = null;
			loadRequestFromString(xmlFilenameOrString);
			responseFileName = null;
		} else {
			// otherwise assume it is a filename
			requestFilename = xmlFilenameOrString;
			loadRequestFromFile();
			responseFileName = xmlFilenameOrString.replace(".request.xml", "") + ".response.xml";
		}
		
		collectAllRequests();
	}
	
	/**
	 * Loads an XML document containing requests from a string using JAXB.
	 * 
	 * @param xmlString The string containing an XML document.
	 */
	protected void loadRequestFromString(String xmlString) {
		prepareEmptyResponse();
		try {
			request = RequestImpl.unmarshall(xmlString);
		} catch (Exception e) {
			xmlParsingError = e.getCause();
			createErrorResponse();
		}
	}
	
	/**
	 * Loads an XML document containing requests using JAXB.
	 */
	protected void loadRequestFromFile() {
		prepareEmptyResponse();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(RequestImpl.class);
			request = RequestImpl.unmarshall(new File(requestFilename));
		} catch (UnmarshalException e) {
			xmlParsingError = e;//.getCause();
			createErrorResponse();
		} catch (Exception e) {
			xmlParsingError = e;
			createErrorResponse();
		} 
	}
	
	protected void prepareEmptyResponse() {
		response = ResponseFactory.INSTANCE.createResponse();
	}

	protected void createErrorResponse() {
		ErrorResponse errorResponse = ResponseFactory.INSTANCE.createErrorResponse();
		if (xmlParsingError == null) {
			errorResponse.setMessage("failed due to unknown reason");
		} else{
			if(xmlParsingError.getCause()!=null && xmlParsingError.getCause().getMessage()!=null){
				errorResponse.setReason(xmlParsingError.getCause().getMessage());
			} 
			if(xmlParsingError.getMessage()!=null){
				errorResponse.setMessage(xmlParsingError.getMessage());
			} 
			if(xmlParsingError.getStackTrace()!=null){
				errorResponse.setStackTrace(new StackTrace(xmlParsingError).toString());
			}
		}
		response.setErrorResponse(errorResponse);
	}

	protected void collectAllRequests() {
		collectStringRequests();
		collectIntegerRequests();
		collectStructureRequests();
		collectCollectionRequests();
		closeRequests();
	}

	protected void collectStringRequests() {
		if (stringRequestProcessors == null) {
			stringRequestProcessors = new HashMap<>();
		}
		if (request == null || request.getStringRequests().isEmpty()) {
			return;
		}

		for (StringRequest stringRequest : request.getStringRequests()) {
			if (!stringRequest.isValid()) {
				StringResponse stringResponse = ResponseFactory.INSTANCE.createStringResponse(stringRequest);
				stringResponse.setWarningsSection(stringRequest.validate().getWarnings());
				response.getStringResponses().add(stringResponse);
				
				continue;
			}
			// check if request is a continuation, i.e. a request for further values
			if (stringRequest.isContinued()) {
				try { // try to load the original request
					StringRequestProcessor stringRequestProcessor = StringRequestProcessor.loadFromFile(stringRequest.getId());
					stringRequestProcessors.put(stringRequestProcessor.getId(), stringRequestProcessor);
					stringRequestProcessor.continueRequest(stringRequest);
				} catch (Exception e) {
					createStringResponseForInvalidId(stringRequest);
				}
			} else {
				// store StringRequest in the map using a fresh UUID
				UUID id = UUID.randomUUID();
				RequestRegistry.INSTANCE.add(stringRequest);
				StringRequestProcessor stringRequestProcessor = new StringRequestProcessor(stringRequest, id, encodeAllCharacters);
				stringRequestProcessors.put(id, stringRequestProcessor);
			}
		}
	}
	
	protected void collectIntegerRequests() {
		if (integerRequestProcessors == null) {
			integerRequestProcessors = new HashMap<>();
		}
		if (bigIntegerRequestProcessors == null) {
			bigIntegerRequestProcessors = new HashMap<>();
		}
		if (request == null || request.getNumberRequests().isEmpty()) {
			return;
		}
		
		// check if request is a continuation, i.e. a request for further values
		for (NumberRequest numberRequest : request.getNumberRequests()) {
			if (!numberRequest.isValid()) {
				NumberResponse<? extends Number> numberResponse = ResponseFactory.INSTANCE.createIntegerResponse(numberRequest);
				numberResponse.setWarningsSection(numberRequest.validate().getWarnings());
				response.getNumberResponses().add(numberResponse);
				continue;
			}
			if (numberRequest.isContinued()) {
				boolean validId = true;
				try { // try to load the original request as an Integer request
					IntegerRequestProcessor integerRequestProcessor = IntegerRequestProcessor.loadFromFile(numberRequest.getId());
					integerRequestProcessors.put(integerRequestProcessor.getId(), integerRequestProcessor);
					integerRequestProcessor.continueRequest(numberRequest);
					continue;
				} catch (Exception e) {
					// may be a requests for numbers larger than Integer supports
					validId = false;
				}
				 try { // try to load the original request as an BigInteger request
					BigIntegerRequestProcessor bigIntegerRequestProcessor = BigIntegerRequestProcessor.loadFromFile(numberRequest.getId());
					bigIntegerRequestProcessors.put(bigIntegerRequestProcessor.getId(), bigIntegerRequestProcessor);
					bigIntegerRequestProcessor.continueRequest(numberRequest);
					continue;
				} catch (Exception e) {
					validId = false;
				}
				if (!validId) {
					createNumberResponseForInvalidId(numberRequest);
				}
			} else {
				// store NumberRequest in the map using a fresh UUID
				UUID id = UUID.randomUUID();
				RequestRegistry.INSTANCE.add(numberRequest);
				NumberSpecification<? extends Number> numberSpec = numberRequest.getNumberSpecification();
				if(numberSpec.getType()==NumberType.INTEGER){
					IntegerSpecification integerSpec = (IntegerSpecification) numberSpec;
					if (integerSpec.isLongCompatible()) {
						IntegerRequestProcessor integerRequestProcessor = new IntegerRequestProcessor(numberRequest, id);
						integerRequestProcessors.put(id, integerRequestProcessor);
					}
					if (integerSpec.isBigIntegerCompatible()) {
						BigIntegerRequestProcessor bigIntegerRequestProcessor = new BigIntegerRequestProcessor(numberRequest, id);
						bigIntegerRequestProcessors.put(id, bigIntegerRequestProcessor);
					}
				} else{
					//TODO: more to do with this map?
					FloatRequestProcessor reqProc = new FloatRequestProcessor(numberRequest, id);
					//floatRequestProcessors.put(id,reqProc);
				}
			}
		}
	}
	
	protected void closeRequests() {
		if (request == null || request.getCloseRequests().isEmpty()) {
			return;
		}

		for (CloseRequest closeRequest : request.getCloseRequests()) {
			String requestId = closeRequest.getId();
			// check if a serialized processor file exists
			try {  // load the request processor...
				RequestProcessor<?> requestProcessor = getRequestProcessorById(requestId);
				String name = requestProcessor.getName();

				// ...delete it...
				RequestRegistry.INSTANCE.remove(requestProcessor.getRequest());
				requestProcessor.delete();

				// ...and confirm the deletion
				CloseRequestConfirmation closeRequestConfirmation = ResponseFactory.INSTANCE.createCloseRequestConfirmation(requestId, name);
				
				response.getCloseRequestConfirmations().add(closeRequestConfirmation);
			} catch (Exception e) {
				// if no request file exists, give warning
				addWarningForIllegalIdInCloseRequest(requestId);
			}
			
		}
	}
	
	protected RequestProcessor<?> getRequestProcessorById(String requestId) throws IllegalRequestIdException {
		try {
			return getStringRequestProcessorById(requestId);
		} catch (IllegalRequestIdException e) { }
		try {
			return getIntegerRequestProcessorById(requestId);
		} catch (IllegalRequestIdException e) { }
		
		throw new IllegalRequestIdException();
	}

	protected void addWarningForIllegalIdInCloseRequest(String id) {
		IllegalRequestFormat illegalId = 
				ResponseFactory.INSTANCE.createIllegalRequestFormatWithWrongAttribute("closeRequest", "id");

		WarningsSection warnings = ResponseFactory.INSTANCE.createWarnings();
		warnings.getIllegalRequestFormats().add(illegalId);

		CloseRequestConfirmation closeRequestConfirmation = 
				ResponseFactory.INSTANCE.createCloseRequestConfirmation(id, null, warnings);

		// add close request confirmation including warnig to response
		response.getCloseRequestConfirmations().add(closeRequestConfirmation);
	}

	protected StringRequestProcessor getStringRequestProcessorById(String id) throws IllegalRequestIdException {
		StringRequestProcessor stringRequestProcessor = null;
		try {
			UUID uuid = UUID.fromString(id);
			if (stringRequestProcessors.containsKey(uuid)) {
				stringRequestProcessor = stringRequestProcessors.get(uuid);
			} else {
				stringRequestProcessor = StringRequestProcessor.loadFromFile(id);
			}
			return stringRequestProcessor;
		} catch (Exception e) {
			throw new IllegalRequestIdException();
		}
	}
	
	protected IntegerRequestProcessor getIntegerRequestProcessorById(String id) throws IllegalRequestIdException {
		IntegerRequestProcessor integerRequestProcessor = null;
		try {
			UUID uuid = UUID.fromString(id);
			if (integerRequestProcessors.containsKey(uuid)) {
				integerRequestProcessor = integerRequestProcessors.get(uuid);
			} else {
				integerRequestProcessor = IntegerRequestProcessor.loadFromFile(id);
			}
			return integerRequestProcessor;
		} catch (Exception e) {
			throw new IllegalRequestIdException();
		}
	}
	
	protected void createStringResponseForInvalidId(StringRequest stringRequest) {
		IllegalRequestFormat illegalFormat = 
				ResponseFactory.INSTANCE.createIllegalRequestFormatWithWrongAttribute("string", "id");

		WarningsSection warningsPart = ResponseFactory.INSTANCE.createWarnings();
		warningsPart.getIllegalRequestFormats().add(illegalFormat);

		StringResponse stringResponse = ResponseFactory.INSTANCE.createStringResponse();
		stringResponse.setName(stringRequest.getName());
		stringResponse.setId(stringRequest.getId());
		stringResponse.setMoreValuesAvailable(false);
		stringResponse.setWarningsSection(warningsPart);

		response.getStringResponses().add(stringResponse);
	}

	protected void createNumberResponseForInvalidId(NumberRequest numberRequest) {
		IllegalRequestFormat illegalFormat = 
				ResponseFactory.INSTANCE.createIllegalRequestFormatWithWrongAttribute("number", "id");

		WarningsSection warningsPart = ResponseFactory.INSTANCE.createWarnings();
		warningsPart.getIllegalRequestFormats().add(illegalFormat);

		IntegerResponse integerResponse = ResponseFactory.INSTANCE.createIntegerResponse();
		integerResponse.setName(numberRequest.getName());
		integerResponse.setId(numberRequest.getId());
		integerResponse.setMoreValuesAvailable(false);
		integerResponse.setWarningsSection(warningsPart);

		response.getNumberResponses().add(integerResponse);
	}

	protected void collectStructureRequests() {
		structureRequestsById = new HashMap<>();
		if (!structureRequestsById.isEmpty()) {
			// store each StructureRequest in the map using a fresh UUID
			for (StructureRequest structureRequest : request.getStructureRequests()) {
				structureRequestsById.put(UUID.randomUUID(), structureRequest);
			}
		}
	}

	protected void collectCollectionRequests() {
		collectionRequestsById = new HashMap<>();
		if (!collectionRequestsById.isEmpty()) {
			// store each CollectionRequest in the map using a fresh UUID
			for (CollectionRequest collectionRequest : request.getCollectionRequests()) {
				collectionRequestsById.put(UUID.randomUUID(), collectionRequest);
			}
		}
	}

	/**
	 * Dispatches all requests to type specific request processors and saves the response to an XML file.
	 */
	public void dispatch() {
		dispatchStringRequests();
		dispatchNumberRequests();
		serializeRequestProcessors();
		saveResponse();
	}

	/**
	 * Returns the response to the request as a Java object. This method has to be called
	 * if the request was transmitted via {@link Request}
	 * 
	 * @return the response to the request as a Java object.
	 */
	public Response getResponse() {
		return response;
	}
	
	public String getXmlResponseString() {
		return xmlResponseString;
	}

	protected void dispatchStringRequests() {
		for (StringRequestProcessor stringRequestProcessor : stringRequestProcessors.values()) {
			StringResponse stringResponse = (StringResponse) stringRequestProcessor.getResponse();
			response.getStringResponses().add(stringResponse);
		}
	}
	
	protected void dispatchNumberRequests() {
		for (IntegerRequestProcessor integerRequestProcessor : integerRequestProcessors.values()) {
			IntegerResponse integerResponse = integerRequestProcessor.getResponse();
			response.getNumberResponses().add(integerResponse);
		}
		for (BigIntegerRequestProcessor bigIntegerRequestProcessor : bigIntegerRequestProcessors.values()) {
			BigIntegerResponse bigIntegerResponse = bigIntegerRequestProcessor.getResponse();
			response.getNumberResponses().add(bigIntegerResponse);
		}
	}

	private void saveResponse() {
		// save only the response if
		// - a request file exists and
		// - either the request file does not exists or it is EMF based that originates from an XML file
		if (response != null) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(ResponseImpl.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				// output pretty printed
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				OutputStream out;
				if (responseFileName == null) {
					out = new ByteArrayOutputStream();
				} else {
					out = new FileOutputStream(responseFileName.replace("file://", ""));
				}
				jaxbMarshaller.marshal(response, out);			
				if (responseFileName == null) {
					xmlResponseString = out.toString();
				}
				out.close();
			} catch (IOException | JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves all type specific request processors and the original requests.
	 */
	protected void serializeRequestProcessors() {
		for (StringRequestProcessor stringRequestProcessor : stringRequestProcessors.values()) {
			stringRequestProcessor.serialize();
		}
		for (IntegerRequestProcessor integerRequestProcessor : integerRequestProcessors.values()) {
			integerRequestProcessor.serialize();
		}
		for (BigIntegerRequestProcessor bigIntegerRequestProcessor : bigIntegerRequestProcessors.values()) {
			bigIntegerRequestProcessor.serialize();
		}
	}

}
