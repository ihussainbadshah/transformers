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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.IgniteExceptionDataV1_1;
import org.eclipse.ecsp.domain.NestedDLQExceptionData;
import org.eclipse.ecsp.domain.SpeedV1_0;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Unit Test class for GenericIgniteEventTransformer for DLQReprocessingUnit.
 *
 * @see GenericIgniteEventTransformer
 *
 */

public class GenericEventTransformerDlqReprocessingUnitTest {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericEventTransformerDlqReprocessingUnitTest.class);
    
    /** The Constant TEN. */
    private static final int TEN = 10;
    
    /** The Constant TWO. */
    private static final int TWO = 2;
    
    /** The Constant TIMESTAMP. */
    private static final long TIMESTAMP = 1481724868055L;
    
    /** The GenericIgniteEventTransformer instance. */
    private GenericIgniteEventTransformer transformer;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        transformer = new GenericIgniteEventTransformer();
    }

    /**
     * Tests the valid DLQ re-processing event structure with valid header setting.
     *
     * @throws JsonParseException JsonParseException
     * @throws JsonMappingException JsonMappingException
     * @throws IOException I/O exception
     */
    @Test
    public void testHeaderSettingsWithValidHeadersForDlqReprocessing()
            throws JsonParseException, JsonMappingException, IOException {
        IgniteEventImpl failedIgniteEvent = getFailedIgniteEvent();
        short tz = TEN;
        failedIgniteEvent.setTimezone(tz);
        SpeedV1_0 speed = new SpeedV1_0();
        speed.setValue(TWO);
        failedIgniteEvent.setEventData(speed);

        // Using genericTransformer convert above event into bytearray
        byte[] failedIgniteEventByteArray = transformer.toBlob(failedIgniteEvent);

        // Create NestedExceptionData
        final NestedDLQExceptionData nestedDlqExceptionData = new NestedDLQExceptionData(failedIgniteEventByteArray, 1, 
                "someprocessor", new RuntimeException("Connection Error"), new HashMap<String, Object>());

        // set rest of the fields
        HashMap<String, Object> context = new HashMap<String, Object>();
        context.put("Stage1", "Stage1Step1");
        IgniteExceptionDataV1_1 data = new IgniteExceptionDataV1_1();
        data.setIgniteEvent(failedIgniteEvent);
        data.setContext(context);
        data.setErrorTimeInMilis(System.currentTimeMillis());
        data.setException(new NullPointerException());
        data.setNestedDLQExceptionData(nestedDlqExceptionData);
        data.setProcessorName("processorName");
        data.setRetryCount(TWO);

        IgniteEventImpl wrapperExceptionEvent = getIgniteEvent(tz, data); 

        byte[] byteArray = transformer.toBlob(wrapperExceptionEvent);
        IgniteEvent igniteEvent = transformer.fromBlob(byteArray,
                Optional.ofNullable(wrapperExceptionEvent));
        Assert.assertEquals(igniteEvent.getEventId(), wrapperExceptionEvent.getEventId());
        Assert.assertEquals(igniteEvent.getVersion(), wrapperExceptionEvent.getVersion());
        Assert.assertEquals(igniteEvent.getRequestId(), wrapperExceptionEvent.getRequestId());
        Assert.assertEquals(igniteEvent.getBizTransactionId(), wrapperExceptionEvent.getBizTransactionId());
        Assert.assertEquals(igniteEvent.getMessageId(), wrapperExceptionEvent.getMessageId());
        Assert.assertEquals(igniteEvent.getVehicleId(), wrapperExceptionEvent.getVehicleId());
        Assert.assertEquals(igniteEvent.getTimestamp(), wrapperExceptionEvent.getTimestamp());
        Assert.assertEquals(igniteEvent.getTimezone(), wrapperExceptionEvent.getTimezone());
        IgniteExceptionDataV1_1 eventData = (IgniteExceptionDataV1_1) igniteEvent.getEventData();
        Assert.assertEquals(eventData.getContext().get("Stage1"), context.get("Stage1"));
        Assert.assertEquals(eventData.getErrorTimeInMilis(), data.getErrorTimeInMilis());
        Assert.assertEquals("someprocessor", eventData.getNestedDLQExceptionData().getProcessorName());
        Assert.assertEquals(1, eventData.getNestedDLQExceptionData().getIteration());
        Assert.assertEquals(eventData.getProcessorName(), data.getProcessorName());
        Assert.assertEquals(eventData.getRetryCount(), data.getRetryCount());
        IgniteEventImpl igniteEventImpl = eventData.getIgniteEvent();
        Assert.assertEquals(igniteEventImpl.getEventId(),
                failedIgniteEvent.getEventId());
        Assert.assertEquals(igniteEventImpl.getVersion(),
                failedIgniteEvent.getVersion());
        Assert.assertEquals(igniteEventImpl.getRequestId(),
                failedIgniteEvent.getRequestId());
        Assert.assertEquals(igniteEventImpl.getBizTransactionId(),
                failedIgniteEvent.getBizTransactionId());
        Assert.assertEquals(igniteEventImpl.getMessageId(),
                failedIgniteEvent.getMessageId());
        Assert.assertEquals(igniteEventImpl.getVehicleId(),
                failedIgniteEvent.getVehicleId());
        Assert.assertEquals(igniteEventImpl.getTimestamp(),
                failedIgniteEvent.getTimestamp());
        Assert.assertEquals(igniteEventImpl.getTimezone(),
                failedIgniteEvent.getTimezone());

    }

    /**
     * Gets the instance of ignite exception data.
     *
     * @param failedIgniteEvent the failed ignite event
     * @param context the context
     * @return the instance of IgniteExceptionDataV1_1
     */
    @NotNull
    private static IgniteExceptionDataV1_1 getIgniteExceptionDataV2(
            IgniteEventImpl failedIgniteEvent, Map<String, Object> context) {
        IgniteExceptionDataV1_1 data = new IgniteExceptionDataV1_1();
        data.setIgniteEvent(failedIgniteEvent);
        data.setContext(context);
        data.setErrorTimeInMilis(System.currentTimeMillis());
        data.setException(new NullPointerException());
        return data;
    }

    /**
     * Gets the failed ignite event.
     *
     * @return the IgniteEventImpl
     */
    @NotNull
    private static IgniteEventImpl getFailedIgniteEvent() {
        IgniteEventImpl failedIgniteEvent = new IgniteEventImpl();
        failedIgniteEvent.setEventId(EventID.SPEED);
        failedIgniteEvent.setVersion(Version.V1_0);
        failedIgniteEvent.setRequestId("requestId");
        failedIgniteEvent.setBizTransactionId("bizTransactionId123");
        failedIgniteEvent.setMessageId("1001");
        failedIgniteEvent.setVehicleId("vehicleId");
        failedIgniteEvent.setTimestamp(System.currentTimeMillis());
        return failedIgniteEvent;
    }

    /**
     * Gets the ignite event.
     *
     * @param tz the timezone.
     * @param data the IgniteExecptionDataV1_1
     * @return the IgniteEvent
     */
    @NotNull
    private static IgniteEventImpl getIgniteEvent(short tz, IgniteExceptionDataV1_1 data) {
        IgniteEventImpl wrapperExceptionEvent = new IgniteEventImpl();
        wrapperExceptionEvent.setEventId(EventID.IGNITE_EXCEPTION_EVENT);
        wrapperExceptionEvent.setVersion(Version.V1_1);
        wrapperExceptionEvent.setEventData(data);
        wrapperExceptionEvent.setRequestId("requestID123");
        wrapperExceptionEvent.setBizTransactionId("bizTransactionId123");
        wrapperExceptionEvent.setMessageId("1001");
        wrapperExceptionEvent.setVehicleId("vehicleId123");
        wrapperExceptionEvent.setTimestamp(System.currentTimeMillis());
        wrapperExceptionEvent.setTimezone(tz);
        return wrapperExceptionEvent;
    }

    /**
     * Tests the de-serialization of the large IgniteEvent (greater than 32 kb
     * chars) .Usually we get this kind of payload as part of DLQ reprocessing.
     * We are using a webhook event (having length >33 kb) to test.
     *
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDlqLargePayloadDeserialization() throws FileNotFoundException, IOException {

        InputStream istream = GenericEventTransformerDlqReprocessingUnitTest.class.getClassLoader()
                .getResourceAsStream("event.json");
        assert istream != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        System.out.println(builder.toString());
        IgniteEvent igniteEvent = transformer.fromBlob(builder.toString().getBytes(),
                Optional.empty());

        Assert.assertEquals(Version.V1_1, igniteEvent.getVersion());
        Assert.assertEquals("IgniteExceptionEvent", igniteEvent.getEventId());
        Assert.assertEquals(TIMESTAMP, igniteEvent.getTimestamp());
        IgniteExceptionDataV1_1 igniteExceptionData = (IgniteExceptionDataV1_1) igniteEvent.getEventData();
        String processorName = "VehicleInfoNotication";
        Assert.assertEquals(igniteExceptionData.getProcessorName(), processorName);
        Assert.assertEquals(TWO, igniteExceptionData.getRetryCount());
        Assert.assertEquals(1, igniteExceptionData.getNestedDLQExceptionData().getIteration());
        Assert.assertEquals(igniteExceptionData.getNestedDLQExceptionData().getProcessorName(),
                processorName);
        IgniteEventImpl nestedIgniteEvent = igniteExceptionData.getIgniteEvent();
        Assert.assertEquals(Version.V1_0, nestedIgniteEvent.getVersion());
        Assert.assertEquals("WebHook", nestedIgniteEvent.getEventId());
        Assert.assertEquals(TIMESTAMP, nestedIgniteEvent.getTimestamp());
        GenericEventData nestedEventData = (GenericEventData) nestedIgniteEvent.getEventData();
        Assert.assertEquals(nestedEventData.getData("eventType"), Optional.of("accountCreated"));
        Assert.assertEquals(nestedEventData.getData("id"), Optional.of("ae87f362-9f5e-4cb1-a48f-e8f148e2fajd"));
    }
}
