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

import org.eclipse.ecsp.domain.BlobDataV1_0;
import org.eclipse.ecsp.domain.DeviceConnStatusV1_0;
import org.eclipse.ecsp.entities.IgniteBlobEvent;
import org.eclipse.ecsp.entities.IgniteDeviceAwareBlobEvent;
import org.eclipse.ecsp.serializer.IngestionSerializerFstImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Base64;

/**
 * Testing IngestionSerializerFstImpl for events serialized with legacy classes and package name.
 */
public class IngestionSerializerFstImplTest {

    /** Property to check if device aware is enabled. */
    private static final String DEVICE_AWARE_ENABLED = "device.aware.enabled";
    
    /** 
     * Reset properties before each test.
     */
    @Before
    public void resetProperties() throws Exception {
        Field propertiesField = IngestionSerializerFstImpl.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        propertiesField.set(null, null);
    }

    /**
     * Test de-serialization of DeviceConnStatusV1_0 data.
     *
     * <p>This test uses a serialized data string that represents
     * an instance of DeviceConnStatusV1_0 and IgniteBlobEvent.
     * These instances have been serialized using the legacy classes and package names.
     */
    @Test
    public void testDeviceConnStatusDataDeserialization() {

        IngestionSerializerFstImpl serializer = new IngestionSerializerFstImpl();
        System.setProperty(DEVICE_AWARE_ENABLED, "false");
        /*
        // Commented out code for creating an instance of DeviceConnStatusV1_0 and IgniteBlobEvent
        // using legacy classes and package names.
        // This is for reference and can be used to generate the serialized data string.
        com.harman.ignite.entities.IgniteBlobEvent originalEvent = new com.harman.ignite.entities.IgniteBlobEvent();
        com.harman.ignite.domain.DeviceConnStatusV1_0 data = new com.harman.ignite.domain.DeviceConnStatusV1_0();
        data.setServiceName("TestService1");
        data.setConnStatus(com.harman.ignite.domain.DeviceConnStatusV1_0.ConnectionStatus.ACTIVE);
        originalEvent.setEventId("testEvent");
        originalEvent.setVersion(Version.V1_0);
        originalEvent.setRequestId("Req1234");
        originalEvent.setVehicleId("Vehicle1234");
        originalEvent.setEventData(data);
        
        byte[] serializedDataB = serializer.serialize(originalEvent);
        
        System.out.println("Serialized data : " + new String(serializedDataB));
        
        String data1 = Base64.getEncoder().encodeToString(serializedDataB);
        System.out.println("Data encoded to String: " + data1);
        */

        // Serialized data string representing an instance of DeviceConnStatusV1_0 and IgniteBlobEvent
        // created using legacy classes and package names.
        String serializedDataStr = "rO0AASpjb20uaGFybWFuLmlnbml0ZS5lbnRpdGllcy5JZ25pdGVCbG9iRX"
                + "ZlbnQAAAEtY29tLmhhcm1hbi5pZ25pdGUuZG9tYWluLkRldmljZUNvbm5TdGF0dXNWMV8w+gE+Y"
                + "29tLmhhcm1hbi5pZ25pdGUuZG9tYWluLkRldmljZUNvbm5TdGF0dXNWMV8wJENvbm5lY3Rpb25T"
                + "dGF0dXMA//wMVGVzdFNlcnZpY2UxAPwJdGVzdEV2ZW50/AdSZXExMjM0/////AtWZWhpY2xlMTI"
                + "zNPoBH29yZy5lY2xpcHNlLmVjc3AuZG9tYWluLlZlcnNpb24AAA==";
        byte[] serializedData = Base64.getDecoder().decode(serializedDataStr);
        
        IgniteBlobEvent deserializedEvent = serializer.deserialize(serializedData);
        
        Assert.assertNotNull(deserializedEvent);
        Assert.assertEquals("testEvent", deserializedEvent.getEventId());
        Assert.assertEquals("TestService1", ((DeviceConnStatusV1_0) deserializedEvent.getEventData()).getServiceName());
        Assert.assertEquals("ACTIVE", 
                ((DeviceConnStatusV1_0) deserializedEvent.getEventData()).getConnStatus().getConnectionStatus());
    }
    
    /**
     * Test de-serialization of BlobEventData.
     *
     * <p>This test uses a serialized data string that represents
     * an instance of BlobDataV1_0 and IgniteBlobEvent.
     * These instances have been serialized using the legacy classes and package names.
     */
    @Test
    public void testBlobEventDataDeserialization() {
        
        IngestionSerializerFstImpl serializer = new IngestionSerializerFstImpl();
        System.setProperty(DEVICE_AWARE_ENABLED, "false");
        /*
        // Commented out code for creating an instance of DeviceConnStatusV1_0 and IgniteBlobEvent
        // using legacy classes and package names.
        // This is for reference and can be used to generate the serialized data string.
        String payloadStr = "dummypayload";
        byte[] payload = Base64.getDecoder().decode(payloadStr);
        com.harman.ignite.entities.IgniteBlobEvent originalEvent = new com.harman.ignite.entities.IgniteBlobEvent();
        com.harman.ignite.domain.BlobDataV1_0 data = new com.harman.ignite.domain.BlobDataV1_0();
        data.setEventSource("Ignite");
        data.setEncoding(com.harman.ignite.domain.AbstractBlobEventData.Encoding.JSON);
        data.setPayload(payload);
        originalEvent.setEventId("testEvent");
        originalEvent.setVersion(Version.V1_0);
        originalEvent.setRequestId("Req1234");
        originalEvent.setVehicleId("Vehicle1234");
        originalEvent.setEventData(data);
        
        byte[] serializedDataB = serializer.serialize(originalEvent);
        
        System.out.println("Serialized data : " + new String(serializedDataB));
        
        String data1 = Base64.getEncoder().encodeToString(serializedDataB);
        System.out.println("Data encoded to String: " + data1);
        */

        // Serialized data string representing an instance of BlobDataV1_0 and IgniteBlobEvent
        // created using legacy classes and package names.
        String serializedDataStr = "rO0AASpjb20uaGFybWFuLmlnbml0ZS5lbnRpdGllcy5JZ25pdGVCbG9iRXZlbn"
                + "QAAAElY29tLmhhcm1hbi5pZ25pdGUuZG9tYWluLkJsb2JEYXRhVjFfMPoBNm9yZy5lY2xpcHNlLmVjc"
                + "3AuZG9tYWluLkFic3RyYWN0QmxvYkV2ZW50RGF0YSRFbmNvZGluZwH//AZJZ25pdGX7JAl26abKlrKW"
                + "hp0A/Al0ZXN0RXZlbnT8B1JlcTEyMzT////8C1ZlaGljbGUxMjM0+gEfb3JnLmVjbGlwc2UuZWNzcC5"
                + "kb21haW4uVmVyc2lvbgAA";
        byte[] serializedData = Base64.getDecoder().decode(serializedDataStr);
        
        IgniteBlobEvent deserializedEvent = serializer.deserialize(serializedData);

        Assert.assertNotNull(deserializedEvent);
        Assert.assertEquals("testEvent", deserializedEvent.getEventId());
        Assert.assertEquals("Ignite", ((BlobDataV1_0) deserializedEvent.getEventData()).getEventSource());
        Assert.assertEquals("json", ((BlobDataV1_0) deserializedEvent.getEventData()).getEncoding().getEncode());
        
        byte[] payloadReceived = ((BlobDataV1_0) deserializedEvent.getEventData()).getPayload();
        String payloadStr = Base64.getEncoder().encodeToString(payloadReceived);
        Assert.assertEquals("dummypayload", payloadStr);
    }
    
    /**
     * Test de-serialization of DeviceAwareBlobEventData.
     *
     * <p>his test uses a serialized data string that represents
     * an instance of IgniteDeviceAwareBlobEvent and BlobDataV1_0.
     * These instances have been serialized using the legacy classes and package names.
     */
    @Test
    public void testDeviceAwareBlobEventDataDeserialization() {

        IngestionSerializerFstImpl serializer = new IngestionSerializerFstImpl();
        System.setProperty(DEVICE_AWARE_ENABLED, "true");
        
        /*
        // Commented out code for creating an instance of DeviceConnStatusV1_0 and IgniteBlobEvent
        // using legacy classes and package names.
        // This is for reference and can be used to generate the serialized data string.
        String payloadStr = "dummypayload";
        byte[] payload = Base64.getDecoder().decode(payloadStr);
        com.harman.ignite.entities.IgniteBlobEvent originalEvent = 
            new com.harman.ignite.entities.IgniteDeviceAwareBlobEvent("ECU1234", "MQTT/Topic/1234");
        com.harman.ignite.domain.BlobDataV1_0 data = new com.harman.ignite.domain.BlobDataV1_0();
        data.setEventSource("Ignite");
        data.setEncoding(com.harman.ignite.domain.AbstractBlobEventData.Encoding.JSON);
        data.setPayload(payload);
        originalEvent.setEventId("testEvent");
        originalEvent.setVersion(Version.V1_0);
        originalEvent.setRequestId("Req1234");
        originalEvent.setVehicleId("Vehicle1234");
        originalEvent.setEventData(data);
        
        byte[] serializedDataB = serializer.serialize(originalEvent);
        
        System.out.println("Serialized data : " + new String(serializedDataB));
        
        String data1 = Base64.getEncoder().encodeToString(serializedDataB);
        System.out.println("Data encoded to String: " + data1);
        */

        // Serialized data string representing an instance of IgniteDeviceAwareBlobEvent and BlobDataV1_0
        // created using legacy classes and package names.
        String serializedDataStr = "rO0AATVjb20uaGFybWFuLmlnbml0ZS5lbnRpdGllcy5JZ25pdGVEZXZpY2VBd2FyZUJ"
                + "sb2JFdmVudAAAASVjb20uaGFybWFuLmlnbml0ZS5kb21haW4uQmxvYkRhdGFWMV8w+gE3Y29tLmhhcm1hbi5p"
                + "Z25pdGUuZG9tYWluLkFic3RyYWN0QmxvYkV2ZW50RGF0YSRFbmNvZGluZwH//AZJZ25pdGX7JAl26abKlrKWh"
                + "p0A/AdFQ1UxMjM0/Al0ZXN0RXZlbnT8D01RVFQvVG9waWMvMTIzNPwHUmVxMTIzNP////wLVmVoaWNsZTEyMzT"
                + "6AR9vcmcuZWNsaXBzZS5lY3NwLmRvbWFpbi5WZXJzaW9uAAA=";
        byte[] serializedData = Base64.getDecoder().decode(serializedDataStr);
        
        IgniteDeviceAwareBlobEvent deserializedEvent = (IgniteDeviceAwareBlobEvent) serializer
                .deserialize(serializedData);

        Assert.assertNotNull(deserializedEvent);
        Assert.assertTrue(deserializedEvent instanceof org.eclipse.ecsp.entities.IgniteDeviceAwareBlobEvent);
        Assert.assertEquals("testEvent", deserializedEvent.getEventId());
        Assert.assertEquals("Ignite", ((BlobDataV1_0) deserializedEvent.getEventData()).getEventSource());
        Assert.assertEquals("json", ((BlobDataV1_0) deserializedEvent.getEventData()).getEncoding().getEncode());
        
        Assert.assertEquals("ECU1234", deserializedEvent.getEcuType());
        Assert.assertEquals("MQTT/Topic/1234", deserializedEvent.getMqttTopic());
        
        byte[] payloadReceived = ((BlobDataV1_0) deserializedEvent.getEventData()).getPayload();
        String payloadStrReceived = Base64.getEncoder().encodeToString(payloadReceived);
        Assert.assertEquals("dummypayload", payloadStrReceived);
    }
}
