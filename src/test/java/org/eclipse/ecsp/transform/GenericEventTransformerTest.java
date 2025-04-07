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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.eclipse.ecsp.domain.DongleStatusV1_0;
import org.eclipse.ecsp.domain.IgniteEventSource;
import org.eclipse.ecsp.domain.SpeedV1_0;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.UserContext;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test class for testing the functionality of GenericEventTransform.
 *
 * @author ksharma
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { org.eclipse.ecsp.transform.config.TransformerTestConfig.class })
public class GenericEventTransformerTest {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericEventTransformerTest.class);
    
    /** The Constant JSON_MAPPER. */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /** The Constant DOUBLE_20. */
    private static final double DOUBLE_20 = 20.0d;

    static {
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSON_MAPPER.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    /** The GenericIgniteEventTransformer instance. */
    @Autowired
    private GenericIgniteEventTransformer transformer;

    /**
     * Negative test case with null. Should throw RuntimeException.
     */
    @Test(expected = TransformerSerDeException.class)
    public void testGenericEventTransformWithNullValue() {
        transformer.fromBlob(null, Optional.empty());
    }

    /**
     * Test with proper event.
     *
     * @throws JsonProcessingException jsonProcessing exception
     * @throws JSONException JSON Exception
     */
    @Test
    public void testWithPropertEvent() throws JsonProcessingException, JSONException {
        String speedEvent = "{\"EventID\": \"Speed\","
                + "\"Version\": \"1.0\","
                + "\"Data\": "
                + "{\"value\":20.0,\"customExtension\":{\"oemKey\":\"oemValue\"}}"
                + ",\"RequestId\":\"d575f05c-23db-4b4e-81d6-b69102bec61b\",\"MessageId\": \"123456\""
                + ",\"CorrelationId\": \"1234\",\"BizTransactionId\": \"Biz1234\"}";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());

        // check the actual event should not be null
        Assert.assertNotNull(event);

        // check the event data shouldn't be null
        Assert.assertNotNull(event.getEventData());
        LOGGER.info(event.getEventData().toString());

        // Verify the event data is of type SpeedV1_0
        Assert.assertEquals(true, event.getEventData() instanceof SpeedV1_0);

        SpeedV1_0 speedData = (SpeedV1_0) event.getEventData();
        // verify the speed value

        Assert.assertEquals(DOUBLE_20, speedData.getValue(), 0.0d);

        String expectedDeserializedJsonString = "{\"EventID\":\"Speed\",\"Version\":\"1.0\","
                + "\"Timestamp\":0,"
                + "\"Data\":{\"value\":20.0,\"customExtension\":{\"oemKey\":\"oemValue\"}},"
                + "\"RequestId\":\"d575f05c-23db-4b4e-81d6-b69102bec61b\","
                + "\"SourceDeviceId\":null,\"VehicleId\":null,\"MessageId\":\"123456\","
                + "\"CorrelationId\":\"1234\",\"BizTransactionId\":\"Biz1234\"}";

        // verify when you deserialize the event as string, it is as expected or
        // not.
        JSONAssert.assertEquals(expectedDeserializedJsonString, JSON_MAPPER.writeValueAsString(event), false);

    }

    /**
     * Test case for asserting that the EventID in the deserialized event is the same as 
     * what was set in the JSON string.
     */
    @Test
    public void testWithImproperEvent() {
        String speedEvent = "{\"EventID\": \"Undefined\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());
        Assert.assertEquals("Undefined", event.getEventId());
    }

    /**
     * In this test case, the EventId field is not present.
     * It throws runtime exception from the EventDataDeSerializer class while
     * deserializing the data.
     */
    @Test(expected = TransformerSerDeException.class)
    public void testWithNoEventIdField() {
        String speedEvent = "{\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        transformer.fromBlob(speedEvent.getBytes(), Optional.empty());
    }

    /**
     * In this test case, the Version field is not present.
     * It should add the default version 1.0 to this field
     */
    @Test
    public void testWithNoVersionField() {
        String speedEvent = "{\"EventID\": \"Speed\",\"Data\": {\"value\":20.0}}";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());

        // check the actual event should not be null
        Assert.assertNotNull(event);

        // check the event data shouldn't be null
        Assert.assertNotNull(event.getEventData());
        LOGGER.info(event.getEventData().toString());

        // Verify the event data is of type SpeedV1
        Assert.assertEquals(true, event.getEventData() instanceof SpeedV1_0);
    }

    /**
     * In this test case, the EventId field is empty.
     * It throws runtime exception from the EventDataDeSerializer class while deserializing
     * the data.
     */
    @Test(expected = RuntimeException.class)
    public void testWithEmptyEventId() {
        String speedEvent = "{\"EventID\": \"\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        transformer.fromBlob(speedEvent.getBytes(), Optional.empty());
    }

    /**
     * In this test case, the Version field is empty. It will attach default version
     */
    public void testWithEmptyVersion() {
        String speedEvent = "{\"EventID\": \"Speed\",\"Version\":\"\",\"Data\": {\"value\":20.0}}";
        transformer.fromBlob(speedEvent.getBytes(), Optional.empty());

    }

    /**
     * In this test case we are passing headers along with the actual event.
     *
     * @throws JsonProcessingException jsonProcessing exception
     * @throws JSONException json exception
     */
    @Test
    public void testWithAllHeaders() throws JsonProcessingException, JSONException {
        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\","
                + "\"Data\": "
                + "{\"value\":20.0},"
                + "\"MessageId\": \"123456\",\"CorrelationId\": \"1234\","
                + "\"BizTransactionId\": \"Biz1234\"}";
        // creating a header events

        String vehicleId = "Vehicle123";
        String requestId = "d575f05c-23db-4b4e-81d6-b69102bec61b";
        String deviceId = "Device123";
        String messageId = "123456";

        // creating header event
        IgniteEvent headerEvent = getIgniteEvent(vehicleId, requestId, deviceId);

        // send the optional header event to the method
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.of(headerEvent));

        // check the actual event should not be null
        Assert.assertNotNull(event);

        // check the event data shouldn't be null
        Assert.assertNotNull(event.getEventData());

        // Verify the event data is of type SpeedV1_0
        Assert.assertEquals(true, event.getEventData() instanceof SpeedV1_0);

        SpeedV1_0 speedData = (SpeedV1_0) event.getEventData();
        // verify the speed value

        Assert.assertEquals(DOUBLE_20, speedData.getValue(), 0.0d);

        // verify the vehicle id
        Assert.assertEquals(vehicleId, event.getVehicleId());

        // verify the device id
        Assert.assertEquals(deviceId, event.getSourceDeviceId());

        // verify the request id
        Assert.assertEquals(requestId, event.getRequestId());

        // verify the message id
        Assert.assertEquals(messageId, event.getMessageId());

        String expectedDeserializedJsonString = "{\"EventID\":\"Speed\",\"Version\":\"1.0\","
                + "\"Timestamp\":0,"
                + "\"Data\":{\"value\":20.0,\"customExtension\":null},"
                + "\"RequestId\":\"d575f05c-23db-4b4e-81d6-b69102bec61b\","
                + "\"SourceDeviceId\":\"Device123\",\"VehicleId\":\"Vehicle123\","
                + "\"MessageId\":\"123456\",\"CorrelationId\":\"1234\","
                + "\"BizTransactionId\":\"Biz1234\"}";

        // verify when you deserialize the event as string, it is as expected or
        // not.
        JSONAssert.assertEquals(expectedDeserializedJsonString, JSON_MAPPER.writeValueAsString(event), false);
    }

    /**
     * Gets the ignite event.
     *
     * @param vehicleId the vehicle id
     * @param requestId the request id
     * @param deviceId the device id
     * @return the ignite event
     */
    private static IgniteEvent getIgniteEvent(String vehicleId, String requestId, String deviceId) {
        IgniteEvent headerEvent = new TestEvent(vehicleId, requestId, deviceId);
        return headerEvent;
    }

    /**
     * Test cases to check only vehicle id is present.
     */
    @Test
    public void testWithVehicleIdOnly() throws JsonProcessingException, JSONException {

        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        // creating a header events

        String vehicleId = "Vehicle123";
        String requestId = null;
        String sourceDeviceId = null;
        String messageId = null;

        // creating header event
        IgniteEvent headerEvent = new TestEvent(vehicleId, requestId, sourceDeviceId);
        // send the optional header event to the method
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.of(headerEvent));

        // check the actual event should not be null
        Assert.assertNotNull(event);

        // check the event data shouldn't be null
        Assert.assertNotNull(event.getEventData());

        // Verify the event data is of type SpeedV1_0
        Assert.assertEquals(true, event.getEventData() instanceof SpeedV1_0);

        SpeedV1_0 speedData = (SpeedV1_0) event.getEventData();
        // verify the speed value

        Assert.assertEquals(DOUBLE_20, speedData.getValue(), 0.0d);

        // verify the vehicle id
        Assert.assertEquals(vehicleId, event.getVehicleId());

        // verify the device id
        Assert.assertEquals(sourceDeviceId, event.getSourceDeviceId());

        // verify the request id
        Assert.assertEquals(requestId, event.getRequestId());

        // verify the message id
        Assert.assertEquals(messageId, event.getMessageId());

        String expectedDeserializedJsonString = "{\"EventID\":\"Speed\",\"Version\":\"1.0\","
                + "\"Timestamp\":0,"
                + "\"Data\":{\"value\":20.0,\"customExtension\":null},"
                + "\"RequestId\":null,\"SourceDeviceId\":null,\"VehicleId\":\"Vehicle123\","
                + "\"MessageId\":null,\"CorrelationId\":null,\"BizTransactionId\":null}";

        // verify when you deserialize the event as string, it is as expected or
        // not.
        JSONAssert.assertEquals(expectedDeserializedJsonString, JSON_MAPPER.writeValueAsString(event), false);
    }

    /**
     * Negative test case when the IgniteEvent is null.
     */
    @Test(expected = RuntimeException.class)
    public void testToBlobWithNullParameter() {
        transformer.toBlob(null);
    }

    /**
     * Testing the happy flow of toBlob method.
     *
     * @throws JSONException json format exception
     */
    @Test
    public void testToBlobHappyFlow() throws JSONException {

        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());

        // check the actual event should not be null
        Assert.assertNotNull(event);

        byte[] bytes = transformer.toBlob(event);
        Assert.assertNotNull(bytes);

        // convert the byte[] to string and check with the
        /*
         * In GenericIgniteEventTransformer, we have used
         * jsonMapper.setSerializationInclusion(Include.NON_NULL); because of
         * which no null values will be serialized
         */
        String expectedDeserializedJsonString = "{\"EventID\":\"Speed\",\"Version\":\"1.0\","
                + "\"Timestamp\":0,"
                + "\"Data\":{\"value\":20.0}}";

        JSONAssert.assertEquals(expectedDeserializedJsonString, new String(bytes), false);
    }

    /**
     * Method to check the source type of GenericIgniteEventTransformer.
     */
    @Test
    public void testSourceType() {
        String actualSource = transformer.getSource();
        Assert.assertEquals(IgniteEventSource.IGNITE, actualSource);

    }

    /**
     * Test with improper event data as list.
     */
    @Test
    public void testWithImproperEventDataAsList() {
        String speedEvent = "[{\"EventID\": \"Undefined\",\"Version\": \"1.0\","
                + "\"Data\": {\"value\":20.0}},"
                + "{\"EventID\": \"Undefined\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}]";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());
        Assert.assertEquals("CompositeEvent", event.getEventId());
    }

    /**
     * Test with proper event data as list.
     */
    @Test
    public void testWithProperEventDataAsList() {
        String speedEvent = "[" + "{\"EventID\": \"Undefined\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}" + ","
                + "{\"EventID\": \"Undefined\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}" + "]";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());
        Assert.assertEquals("CompositeEvent", event.getEventId());
    }

    /**
     * Testing the happy flow of toBlob method.
     *
     * @throws JsonProcessingException jsonProcessing exception
     * @throws JSONException json exception
     */
    @Test
    public void testEventListWithData() throws JsonProcessingException, JSONException {
        String speedEvent = "[{\"EventID\":\"Speed\",\"Version\":\"1.0\",\"Timestamp\":0,"
                + "\"Data\":{\"value\":20.0},\"Timezone\":0,\"DeviceDeliveryCutoff\":-1}]";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());
        Assert.assertNotNull(event.getNestedEvents());
        Assert.assertEquals("Expected one event", 1, event.getNestedEvents().size());
        byte[] bytes = transformer.toBlob(event);
        Assert.assertNotNull(bytes);
        JSONAssert.assertEquals(speedEvent, new String(bytes), false);
    }

    /**
     * Test cases to check only vehicle id is present.Same as above test but
     * sending the Optional value as null itself.
     *
     */
    @Test
    public void testWithVehicleIdOnlyWithOptionalsNull() throws JsonProcessingException, JSONException {

        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}";
        // creating a header events

        String vehicleId = "Vehicle123";
        String requestId = null;
        String deviceId = null;

        // creating header event
        IgniteEvent headerEvent = getIgniteEvent(vehicleId, requestId, null);

        // send the optional header event to the method
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.of(headerEvent));
        // check the actual event should not be null
        Assert.assertNotNull(event);

        // check the event data shouldn't be null
        Assert.assertNotNull(event.getEventData());

        // Verify the event data is of type SpeedV1_0
        Assert.assertEquals(true, event.getEventData() instanceof SpeedV1_0);

        SpeedV1_0 speedData = (SpeedV1_0) event.getEventData();
        // verify the speed value

        Assert.assertEquals(DOUBLE_20, speedData.getValue(), 0.0d);

        // verify the vehicle id
        Assert.assertEquals(vehicleId, event.getVehicleId());

        // verify the device id
        Assert.assertEquals(deviceId, event.getSourceDeviceId());

        // verify the request id
        Assert.assertEquals(requestId, event.getRequestId());
        // verify when you deserialize the event as string, it is as expected or
        // not.
        JSONAssert.assertEquals(speedEvent, JSON_MAPPER.writeValueAsString(event), false);

    }

    /**
     * Testing the happy flow of toBlob method.
     *
     * @throws JsonProcessingException jsonProcessing exception
     * @throws JSONException json exception
     */
    @Test
    public void testToBlobHappyFlowList() throws JsonProcessingException, JSONException {

        String speedEvent = "[{\"EventID\": \"Speed\",\"Version\": \"1.0\",\"Data\": {\"value\":20.0}}]";
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());
        // check the actual event should not be null
        Assert.assertNotNull(event);
        byte[] bytes = transformer.toBlob(event);
        Assert.assertNotNull(bytes);
        JSONAssert.assertEquals(speedEvent, new String(bytes), false);
    }

    /**
     * Test dongle status event data.
     *
     * @throws JsonProcessingException the json processing exception
     * @throws JSONException the JSON exception
     */
    @Test
    public void testDongleStatusEventData() throws JsonProcessingException, JSONException {
        final double latitude = 33.79374806;
        final double longitude = -118.00800745;
        final double delta = 0.1;
        String dongleStatusEvent = "{\"EventID\": \"DongleStatus\",\"Version\": \"1.0\","
                + "\"Data\": {\"status\":\"attached\","
                + "\"latitude\":33.793748059999999,\"longitude\":-118.00800744999999}}";

        IgniteEvent event = transformer.fromBlob(dongleStatusEvent.getBytes(), Optional.empty());
        DongleStatusV1_0 data = (DongleStatusV1_0) event.getEventData();
        Assert.assertEquals("attached", data.getStatus().getValue());
        Assert.assertEquals(latitude, data.getLatitude(), delta);
        Assert.assertEquals(longitude, data.getLongitude(), delta);
        // check the actual event should not be null
        Assert.assertNotNull(event);
        byte[] bytes = transformer.toBlob(event);
        Assert.assertNotNull(bytes);
        JSONAssert.assertEquals(dongleStatusEvent, new String(bytes), false);

        // DongleStatus - detached
        String detachedDongleStatusEvent = "{\"EventID\": \"DongleStatus\","
                + "\"Version\": \"1.0\",\""
                + "Data\": {\"status\":\"detached\",\"latitude\":33.793748059999999,"
                + "\"longitude\":-118.00800744999999}}";
        event = transformer.fromBlob(detachedDongleStatusEvent.getBytes(), Optional.empty());
        data = (DongleStatusV1_0) event.getEventData();
        Assert.assertEquals("detached", data.getStatus().getValue());

    }

    /**
     * Test dongle status event data with tripid.
     *
     * @throws JsonProcessingException the json processing exception
     * @throws JSONException the JSON exception
     */
    @Test
    public void testDongleStatusEventDataWithTripid() throws JsonProcessingException, JSONException {
        String dongleStatusEvent = "{\"EventID\": \"DongleStatus\","
                + "\"Version\": \"1.0\","
                + "\"Data\": {\"status\":\"attached\",\"latitude\":10,"
                + "\"longitude\":20,\"tripid\":\"dfad3bf2-2e73-4d30-8bca-dc403350f80c\"}}";
        IgniteEvent event = transformer.fromBlob(dongleStatusEvent.getBytes(), Optional.empty());
        DongleStatusV1_0 data = (DongleStatusV1_0) event.getEventData();
        Assert.assertEquals("dfad3bf2-2e73-4d30-8bca-dc403350f80c", data.getTripid());
        // check the actual event should not be null
        Assert.assertNotNull(event);
        byte[] bytes = transformer.toBlob(event);
        Assert.assertNotNull(bytes);
        JSONAssert.assertEquals(dongleStatusEvent, new String(bytes), false);
    }

    /**
     * It won't throw the exception because if the enum value is not matched it will set it as null.
     * jsonMapper.configure(DeserializationFeature. READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
     *
     * @throws JsonProcessingException jsonProcessing exception
     * @throws JSONException json format exception
     */
    public void testIncorrectDongleStatusEventData() throws JsonProcessingException, JSONException {
        String incorrectDongleStatusEvent = "{\"EventID\": \"DongleStatus\",\"Version\": \"1.0\","
                + "\"Data\": {\"status\":\"incorrect\",\"latitude\":10,\"longitude\":20}}";
        transformer.fromBlob(incorrectDongleStatusEvent.getBytes(), Optional.empty());

    }

    /**
     * This is to verify if there is batch of events and if few events doesn't have version,
     * then the entire batch shouldn't get dropped.
     * <br>
     * For the events for which the version is not present,
     * default version should be appended, and it should pass through
     *
     * @throws IOException io exception
     */
    @Test
    public void testEventListWithEmptyVersion() throws IOException {
        InputStream istream = GenericEventTransformerTest.class.getClassLoader()
                .getResourceAsStream("eventListWithEmptyVersion.txt");
        assert istream != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));

        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        IgniteEvent events = transformer.fromBlob(builder.toString().getBytes(), Optional.empty());
        Assert.assertNotNull(events);
        // it will be a list of events
        List<IgniteEvent> eventList = events.getNestedEvents();
        Assert.assertEquals(1, eventList.size());
        IgniteEvent ievent = eventList.get(0);
        Assert.assertNotNull(ievent);
        Assert.assertNotNull(ievent.getEventData());
        Assert.assertTrue(ievent.getEventData() instanceof GenericEventData);
    }

    /**
     * Test with propert event with input validation enabled.
     *
     * @throws JsonProcessingException the json processing exception
     * @throws JSONException the JSON exception
     * @throws NoSuchFieldException the no such field exception
     * @throws IllegalAccessException the illegal access exception
     */
    @Test
    public void testWithPropertEventWithisInputValidationEnabledTrue()
            throws JsonProcessingException, JSONException, NoSuchFieldException, IllegalAccessException {
        String speedEvent = "{\"EventID\": \"Speed\",\"Version\": \"1.0\","
                + "\"Data\": {\"value\":20.0,\"customExtension\":{\"oemKey\":\"oemValue\"}},"
                + "\"RequestId\":\"d575f05c-23db-4b4e-81d6-b69102bec61b\",\"MessageId\": \"123456\","
                + "\"CorrelationId\": \"1234\",\"BizTransactionId\": \"Biz1234\"}";
        Field isInputValidationEnabled = transformer.getClass().getDeclaredField("isInputValidationEnabled");
        isInputValidationEnabled.setAccessible(true);
        isInputValidationEnabled.setBoolean(transformer, true);
        IgniteEvent event = transformer.fromBlob(speedEvent.getBytes(), Optional.empty());

        // check the actual event should not be null
        Assert.assertNotNull(event);

        // check the event data shouldn't be null
        Assert.assertNotNull(event.getEventData());
        LOGGER.info(event.getEventData().toString());

        // Verify the event data is of type SpeedV1_0
        Assert.assertEquals(true, event.getEventData() instanceof SpeedV1_0);

        SpeedV1_0 speedData = (SpeedV1_0) event.getEventData();
        // verify the speed value

        Assert.assertEquals(DOUBLE_20, speedData.getValue(), 0.0d);

        String expectedDeserializedJsonString = "{\"EventID\":\"Speed\",\"Version\":\"1.0\","
                + "\"Timestamp\":0,"
                + "\"Data\":{\"value\":20.0,\"customExtension\":{\"oemKey\":\"oemValue\"}},"
                + "\"RequestId\":\"d575f05c-23db-4b4e-81d6-b69102bec61b\",\"SourceDeviceId\":null,"
                + "\"VehicleId\":null,\"MessageId\":\"123456\",\"CorrelationId\":\"1234\","
                + "\"BizTransactionId\":\"Biz1234\"}";

        // verify when you deserialize the event as string, it is as expected or
        // not.
        JSONAssert.assertEquals(expectedDeserializedJsonString, JSON_MAPPER.writeValueAsString(event), false);

    }

    /**
     * The Class TestEvent.
     */
    private static class TestEvent implements IgniteEvent {

        /** The vehicle id. */
        private final String vehicleId;
        
        /** The request id. */
        private final String requestId;
        
        /** The source device id. */
        private final String sourceDeviceId;

        /**
         * Instantiates a new test event.
         *
         * @param vehicleId the vehicle id
         * @param requestId the request id
         * @param sourceDeviceId the source device id
         * @param messageId the message id
         */
        public TestEvent(String vehicleId, String requestId, String sourceDeviceId) {
            this.vehicleId = vehicleId;
            this.requestId = requestId;
            this.sourceDeviceId = sourceDeviceId;
        }

        /**
         * Gets the platform id.
         *
         * @return the platform id
         */
        @Override
        public String getPlatformId() {
            return null;
        }

        /**
         * Gets the schema version.
         *
         * @return the schema version
         */
        @Override
        public Version getSchemaVersion() {
            return null;
        }

        /**
         * Sets the schema version.
         *
         * @param v the new schema version
         */
        @Override
        public void setSchemaVersion(Version v) {
            return;
        }

        /**
         * Gets the version.
         *
         * @return the version
         */
        @Override
        public Version getVersion() {
            return null;
        }

        /**
         * Gets the vehicle id.
         *
         * @return the vehicle id
         */
        @Override
        public String getVehicleId() {
            return vehicleId;
        }

        /**
         * Gets the timestamp.
         *
         * @return the timestamp
         */
        @Override
        public long getTimestamp() {
            return 0;
        }

        /**
         * Gets the request id.
         *
         * @return the request id
         */
        @Override
        public String getRequestId() {
            return requestId;
        }

        /**
         * Gets the nested events.
         *
         * @return the nested events
         */
        @Override
        public List<IgniteEvent> getNestedEvents() {
            return null;
        }

        /**
         * Gets the event id.
         *
         * @return the event id
         */
        @Override
        public String getEventId() {
            return null;
        }

        /**
         * Gets the event data.
         *
         * @return the event data
         */
        @Override
        public EventData getEventData() {
            return null;
        }

        /**
         * Gets the source device id.
         *
         * @return the source device id
         */
        @Override
        public String getSourceDeviceId() {
            return sourceDeviceId;
        }

        /**
         * Gets the DFF qualifier.
         *
         * @return the DFF qualifier
         */
        @Override
        public String getDFFQualifier() {
            return null;
        }

        /**
         * Gets the biz transaction id.
         *
         * @return the biz transaction id
         */
        @Override
        public String getBizTransactionId() {
            return null;
        }

        /**
         * Gets the correlation id.
         *
         * @return the correlation id
         */
        @Override
        public String getCorrelationId() {
            return null;
        }

        /**
         * Gets the message id.
         *
         * @return the message id
         */
        @Override
        public String getMessageId() {
            return null;
        }

        /**
         * Gets the target device id.
         *
         * @return the target device id
         */
        @Override
        public Optional<String> getTargetDeviceId() {
            return Optional.empty();
        }

        /**
         * Checks if is device routable.
         *
         * @return true, if is device routable
         */
        @Override
        public boolean isDeviceRoutable() {
            return false;
        }

        /**
         * Checks if is shoulder tap enabled.
         *
         * @return true, if is shoulder tap enabled
         */
        @Override
        public boolean isShoulderTapEnabled() {
            return false;
        }

        /**
         * Checks if is transient data.
         *
         * @return true, if is transient data
         */
        @Override
        public boolean isTransientData() {
            return false;
        }

        /**
         * Checks if is response expected.
         *
         * @return true, if is response expected
         */
        @Override
        public boolean isResponseExpected() {
            return false;
        }

        /**
         * Gets the timezone.
         *
         * @return the timezone
         */
        @Override
        public short getTimezone() {
            return 0;
        }

        /**
         * Gets the device delivery cutoff.
         *
         * @return the device delivery cutoff
         */
        @Override
        public long getDeviceDeliveryCutoff() {
            return 0;
        }

        /**
         * Gets the dev msg topic suffix.
         *
         * @return the dev msg topic suffix
         */
        @Override
        public Optional<String> getDevMsgTopicSuffix() {
            return null;
        }

        /**
         * Gets the tracing context.
         *
         * @return the tracing context
         */
        public String getTracingContext() {
            return null;
        }

        /**
         * Gets the user context info.
         *
         * @return the user context info
         */
        @Override
        public List<UserContext> getUserContextInfo() {
            return null;
        }

        /**
         * Checks if is duplicate message.
         *
         * @return the boolean
         */
        @Override
        public Boolean isDuplicateMessage() {
            return false;
        }

        /**
         * Sets the tracing context.
         *
         * @param context the new tracing context
         */
        @Override
        public void setTracingContext(String context) {
            return;
        }

        /**
         * Gets the ecu type.
         *
         * @return the ecu type
         */
        @Override
        public String getEcuType() {
            return null;
        }

        /**
         * Gets the mqtt topic.
         *
         * @return the mqtt topic
         */
        @Override
        public String getMqttTopic() {
            return null;
        }

        /**
         * Gets the kafka headers.
         *
         * @return the kafka headers
         */
        @Override
        public Map<String, String> getKafkaHeaders() {
            return null;
        }
    }
}