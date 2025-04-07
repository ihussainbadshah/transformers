/*
 *
 *
 *   ******************************************************************************
 *
 *    Copyright (c) 2023-24 Harman International
 *
 *
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *
 *    you may not use this file except in compliance with the License.
 *
 *    You may obtain a copy of the License at
 *
 *
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *    Unless required by applicable law or agreed to in writing, software
 *
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *    See the License for the specific language governing permissions and
 *
 *    limitations under the License.
 *
 *
 *
 *    SPDX-License-Identifier: Apache-2.0
 *
 *    *******************************************************************************
 *
 *
 */

package org.eclipse.ecsp.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.EventAttribute;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.IgniteEventSource;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.CompositeIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventBase;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.transform.config.JacksonMapperConfig;
import org.eclipse.ecsp.transform.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Generic value transformer to convert the Data to appropriate POJO.
 * 
 * <p>
 * Example: Sample dongle status event looks like as follows:
 *
 * "{"EventID": "DongleStatus","Version: "1.0","Data": { "status": "attached", "latitude": 42.2363501,
 * "longitude": -87.9428014 }}" ;
 *
 * In the above event EventData is "Data": { "status":"attached", "latitude": 42.2363501, "longitude": -87.9428014 }
 *
 * This transformer based on the EventId and Version will load appropriate class to
 * represent the Data and along with header forms the
 * ignite event
 * </p>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@PropertySource(ignoreResourceNotFound = true, value = "classpath:inputvalidation-base.properties")
@PropertySource(ignoreResourceNotFound = true, value = "classpath:inputvalidation.properties")
public class GenericIgniteEventTransformer implements Transformer {
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericIgniteEventTransformer.class);
    
    /** The Constant MINUS_ONE. */
    public static final int MINUS_ONE = -1;
    
    /** The Constant FIFTY. */
    public static final int FIFTY = 50;
    
    /** The Constant THREE. */
    public static final int THREE = 3;
    
    /** The Constant TWO. */
    public static final int TWO = 2;

    /** The ObjectMapper instance. */
    private ObjectMapper jsonMapper;

    /** The Environment instance. */
    @Autowired
    private Environment env;

    /** The input validation string. */
    private String inputValidation = ".inputvalidation";

    /** Suggests whether input validation is enabled or not. */
    @Value("${enable.input.validation:false}")
    private boolean isInputValidationEnabled;

    /**
     * Default constructor of GenericIgniteEventTransformer which initializes the ObjectMapper 
     * instance in this transformer class to read / write JSONs.
     */
    @Autowired
    public GenericIgniteEventTransformer() {
        if (jsonMapper == null) {
            jsonMapper = new JacksonMapperConfig().jsonObjectMapper();
        }
    }

    /**
     * Default constructor of GenericIgniteEventTransformer which initializes the ObjectMapper 
     * with the given properties, in this transformer class to read / write JSONs.
     *
     * @param props : Properties
     */
    public GenericIgniteEventTransformer(Properties props) {
        LOGGER.debug("Loading parameterized constructor for GenericIgniteEventTransformer with properties :-{}", props);
        if (jsonMapper == null) {
            jsonMapper = new JacksonMapperConfig(props).jsonObjectMapper();
        }
    }

    /**
     * Transforms from byte[] to IgniteEvent.
     *
     * @param value the byte[] value
     * @param header the header
     * @return the IgniteEvent
     */
    @Override
    public IgniteEvent fromBlob(byte[] value, Optional<IgniteEventBase> header) {

        if (null == value) {
            throw new TransformerSerDeException("Null value received, cannot convert to ignite event.");
        }
        /*
          For Ignite event we can directly convert the value to string and then
          based on eventid and version we can load the appropriate data class
         */
        String eventAsString = new String(value);
        LOGGER.debug("Event received:{}", eventAsString);
        AbstractIgniteEvent igniteEvent = null;
        try {
            JsonNode node = jsonMapper.readTree(eventAsString);
            if (node.isObject()) {
                igniteEvent = jsonMapper.readValue(eventAsString, IgniteEventImpl.class);
            } else {
                igniteEvent = new CompositeIgniteEvent();
                // An eventId is mandatory for each ignite event, or it will
                // throw exception in DFFAgent
                igniteEvent.setEventId(EventID.COMPOSITE_EVENT);
                List<IgniteEvent> eventAsList = new ArrayList<>();
                ArrayNode nodes = (ArrayNode) node;
                int length = nodes.size();
                for (int i = 0; i < length; i++) {
                    JsonNode child = nodes.get(i);
                    IgniteEventImpl event = jsonMapper.readValue(jsonMapper.writeValueAsString(child),
                            IgniteEventImpl.class);
                    eventAsList.add(event);
                }
                ((CompositeIgniteEvent) igniteEvent).setNestedEvents(eventAsList);
            }
        } catch (Exception e) {
            LOGGER.error("Unable to convert the value to IgniteEventImpl List.", e);
            throw new TransformerSerDeException("Unable to deserialize the ignite event List:" + new String(value));
        }

        /*
          Now set the header in the ignite event
         */
        if (header.isPresent()) {
            setHeaders(igniteEvent, header);
        }

        /*
          Validate all input params on ignite event
         */
        if (isInputValidationEnabled && igniteEvent != null && env != null) {
            Properties props = extractProperties(env);
            if (!isAllInputParamsValid(igniteEvent, props)) {
                throw new IllegalArgumentException("Validation Failed.");
            }
        }
        LOGGER.debug("Ignite event returned is:{}", igniteEvent);
        return igniteEvent;
    }

    /**
     * Checks if is all input params are valid.
     *
     * @param igniteEvent the AbstractIgniteEvent
     * @param props the props
     * @return true, if is all input params valid
     */
    private boolean isAllInputParamsValid(AbstractIgniteEvent igniteEvent,
            Properties props) {
        Map<String, String> params = new HashMap<>();
        params.put(EventAttribute.EVENTID, igniteEvent.getEventId());
        params.put(EventAttribute.BIZTRANSACTION_ID, igniteEvent.getBizTransactionId());
        params.put(EventAttribute.TIMESTAMP, String.valueOf(igniteEvent.getTimestamp()));
        params.put(EventAttribute.DFF_QUALIFIER, igniteEvent.getDFFQualifier());
        params.put(EventAttribute.CORRELATION_ID, igniteEvent.getCorrelationId());
        params.put(EventAttribute.MESSAGE_ID, igniteEvent.getMessageId());
        params.put(EventAttribute.REQUEST_ID, igniteEvent.getRequestId());
        params.put(EventAttribute.SOURCE_DEVICE_ID, igniteEvent.getSourceDeviceId());
        params.put(EventAttribute.VEHICLE_ID, igniteEvent.getVehicleId());
        params.put(EventAttribute.SOURCE_DEVICE_ID, igniteEvent.getSourceDeviceId());

        List<String> invalidParams = isValidInputParams(params, props);
        if (!invalidParams.isEmpty()) {
            invalidParams.forEach(LOGGER::error);
            return false;
        }
        return true;
    }

    /**
     * Transforms from IgniteEvent to byte[].
     *
     * @param value the IgniteEvent value
     * @return the byte[]
     */
    @Override
    public byte[] toBlob(IgniteEvent value) {

        if (null == value) {
            LOGGER.error("Received null ignite event value, cannot conver to blob.");
            throw new TransformerSerDeException("Received null ignite event value");
        }

        LOGGER.debug("Converting ignite event:{} to byte array", value);

        byte[] blobData = null;
        try {
            if (EventID.COMPOSITE_EVENT.equals(value.getEventId())) {
                blobData = jsonMapper.writeValueAsBytes(value.getNestedEvents());
            } else {
                blobData = jsonMapper.writeValueAsBytes(value);
            }

        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to convert the ignite event to bytes.", e);
            throw new TransformerSerDeException("Unable to conver the ignite event:"
                    + value.toString() + " to byte array");
        }
        return blobData;
    }

    /**
     * Helper method to set the headers.
     *
     * @param event : AbstractIgniteEvent
     * @param header : Optional of {@Link IgniteEventBase}
     */
    private void setHeaders(AbstractIgniteEvent event, Optional<IgniteEventBase> header) {

        LOGGER.debug("Fetching header event {}", header);

        String sourceDeviceId = null;
        String vehicleId = null;
        String requestId = null;
        String traceCtx = null;
        IgniteEventBase headerEvent = header.isPresent() ? header.get() : null;

        if (headerEvent != null) {
            if (null != headerEvent.getSourceDeviceId()) {
                sourceDeviceId = headerEvent.getSourceDeviceId();
                event.setSourceDeviceId(sourceDeviceId);
            }

            if (!StringUtils.isBlank(headerEvent.getVehicleId())) {
                vehicleId = headerEvent.getVehicleId();
                event.setVehicleId(vehicleId);
            }

            if (!StringUtils.isBlank(headerEvent.getRequestId())) {
                requestId = headerEvent.getRequestId();
                event.setRequestId(requestId);
            }

            if (!StringUtils.isBlank(headerEvent.getTracingContext())) {
                traceCtx = headerEvent.getTracingContext();
                event.setTracingContext(traceCtx);
            }
        }
        LOGGER.debug("IgniteEvent headers are source device: {}, vehicleId: {}, "
                        + "requestId: {}, traceCtx: {}", sourceDeviceId, vehicleId,
                requestId, traceCtx);
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    @Override
    public String getSource() {
        return IgniteEventSource.IGNITE;
    }

    /**
     * Method exposed for testing.
     *
     * @param mapper : ObjectMapper
     */
    void setObjectMapper(ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }

    /**
     * Extract properties.
     *
     * @param env the env
     * @return the properties
     */
    private Properties extractProperties(Environment env) {
        Properties props = new Properties();
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        for (org.springframework.core.env.PropertySource<?> ps : propSrcs) {
            if (ps instanceof EnumerablePropertySource<?> propertySource) {
                for (String pn : propertySource.getPropertyNames()) {
                    props.setProperty(pn, env.getProperty(pn));
                }
            }
        }
        return props;
    }

    /**
     * Checks for special chars. This is a helper method for 
     * {@link GenericIgniteEventTransformer#isAllInputParamsValid(AbstractIgniteEvent, Properties)}
     *
     * @param specialChars the special chars
     * @param inputString the input string
     * @return true, if successful
     */
    private boolean hasAllSpecialChars(String specialChars, String inputString) {
        for (int i = 0; i < specialChars.length(); i++) {
            char c = specialChars.charAt(i);
            if (inputString.indexOf(c) == MINUS_ONE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if is valid input params. This is a helper method for 
     * {@link GenericIgniteEventTransformer#isAllInputParamsValid(AbstractIgniteEvent, Properties)}
     *
     * @param fields the fields
     * @param props the props
     * @return the list
     * @throws TransformerSerDeException the transformer ser de exception
     */
    private List<String> isValidInputParams(Map<String, String> fields, Properties props)
            throws TransformerSerDeException {
        List<String> allValidations = new ArrayList<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            allValidations.addAll(validateEntry(entry, props));
        }
        return allValidations;
    }

    /**
     * Validate each key-value pair for input validation. This is a helper method for 
     * {@link GenericIgniteEventTransformer#isAllInputParamsValid(AbstractIgniteEvent, Properties)}
     *
     * @param entry the entry
     * @param props the props
     * @return the list
     */
    private List<String> validateEntry(Map.Entry<String, String> entry, Properties props) {
        String key;
        String value;
        List<String> allValidations = new ArrayList<>();
        String inputValidationProp = entry.getKey() + inputValidation;
        key = entry.getKey();
        value = entry.getValue();
        if (needToValidate(entry, props)) {
            String inputValidationPropValue = props.getProperty(inputValidationProp);
            String[] args = inputValidationPropValue.split("\\|");
            value = value.trim();

            validateLength(key, value, allValidations);
            if (args.length > 0) {
                validateValueType(key, value, args, allValidations);
                validateValue(key, value, args, allValidations);
            }
        }
        return allValidations;
    }

    /**
     * Need to validate. This is a helper method for 
     * {@link GenericIgniteEventTransformer#isAllInputParamsValid(AbstractIgniteEvent, Properties)}
     *
     * @param entry the entry
     * @param props the props
     * @return true, if successful
     */
    private boolean needToValidate(Map.Entry<String, String> entry, Properties props) {
        String inputValidationProp = entry.getKey() + inputValidation;
        String key = entry.getKey();
        String value = entry.getValue();
        return (key != null && value != null && props.containsKey(inputValidationProp));
    }

    /**
     * Validate length. This is a helper method for 
     * {@link GenericIgniteEventTransformer#isAllInputParamsValid(AbstractIgniteEvent, Properties)}
     *
     * @param key the key
     * @param value the value
     * @param allValidations the all validations
     */
    private void validateLength(String key, String value, List<String> allValidations) {
        if (value.length() > FIFTY) {
            allValidations.add("Max Length exceeded for Property:" + key);
        }
    }

    /**
     * Validate value type. This is a helper method for 
     * {@link GenericIgniteEventTransformer#isAllInputParamsValid(AbstractIgniteEvent, Properties)}
     *
     * @param key the key
     * @param value the value
     * @param args the args
     * @param allValidations the all validations
     */
    private void validateValueType(String key, String value, String[] args, List<String> allValidations) {
        switch (args[0]) {
            case Constants.ALPHA:
                if (!StringUtils.isAlpha(value)) {
                    allValidations.add("Alpha Validation failed for Property:" + key);
                }
                break;
            case Constants.ALPHANUMERIC:
                String aphaNumericValue = value.replaceAll("[^a-zA-Z0-9]", "");
                if (!StringUtils.isAlphanumeric(aphaNumericValue)) {
                    allValidations.add("Alphanumeric Validation failed for Property:" + key);
                }
                break;
            case Constants.NUMERIC:
                if (!StringUtils.isNumeric(value)) {
                    allValidations.add("Numeric Validation failed for Property:" + key);
                }
                break;
            default:
                LOGGER.error("Invalid Validation Type mentioned for property {}.", key);
                throw new TransformerSerDeException("Invalid Validation Type.");
        }
    }

    /**
     * Validate value. This is a helper method for 
     * {@link GenericIgniteEventTransformer#isAllInputParamsValid(AbstractIgniteEvent, Properties)}
     *
     * @param key the key
     * @param value the value
     * @param args the args
     * @param allValidations the all validations
     */
    private void validateValue(String key, String value, String[] args, List<String> allValidations) {
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        if (args.length > 1 && args[1] != null) {
            if (StringUtils.isNumeric(args[1].trim()) && Long.parseLong(value) > 0
                    && value.length() != Long.parseLong(args[1].trim())) {
                allValidations.add("Input Validation Length Mismatch for Property:" + key);
            } else if (pattern.matcher(args[1].trim()).find() && !hasAllSpecialChars(args[1].trim(), value)) {
                allValidations.add("Input Validation Special Characters Mismatch for Property:" + key);
            }
        }

        if (args.length == THREE && pattern.matcher(args[TWO]).find() && !hasAllSpecialChars(args[TWO], value)) {
            allValidations.add("Input Validation Special Characters Mismatch for Property:" + key);
        }
    }
}
