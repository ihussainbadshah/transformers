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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertNull;

/**
 * Test class for testing the functionality of GenericEventTransformer.
 *
 * @author ksharma
 */
public class GenericEventTransformerUnitTest {

    /** The transformer. */
    private GenericIgniteEventTransformer transformer;

    /** The mockito rule. */
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    /** The json mapper. */
    @Mock
    ObjectMapper jsonMapper;

    /** The event. */
    @Mock
    IgniteEvent event;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        transformer = new GenericIgniteEventTransformer();
        transformer.setObjectMapper(jsonMapper);
    }

    /**
     * Testing Json Exception in toBlob method.
     *
     * @throws JsonProcessingException exception
     */
    
    @Test(expected = TransformerSerDeException.class)
    public void testJsonException() throws JsonProcessingException {
        Mockito.when(jsonMapper.writeValueAsBytes(Mockito.isA(IgniteEvent.class)))
                .thenThrow(JsonProcessingException.class);
        transformer.toBlob(event);
    }

    /**
     * Test throwing of JSONException if relevant properties are not set.
     *
     * @throws JsonProcessingException the json processing exception
     */
    @Test(expected = TransformerSerDeException.class)
    public void testJsonException2() throws JsonProcessingException {
        Properties p = new Properties();
        transformer = new GenericIgniteEventTransformer(p);
        Mockito.when(jsonMapper.writeValueAsBytes(Mockito.isA(IgniteEvent.class)))
                .thenThrow(JsonProcessingException.class);
        transformer.toBlob(event);
    }

    /**
     * Testing if the headers are null.
     *
     * @throws JsonParseException JsonParseException
     * @throws JsonMappingException JsonMappingException
     * @throws IOException        I/O exception
     */
    
    @Test
    public void testHeaderSettingsWithNullHeaders() throws JsonParseException, JsonMappingException, IOException {

        IgniteEventImpl actualEvent = new IgniteEventImpl();
        JsonNode node = new ObjectNode(JsonNodeFactory.instance);

        Mockito.when(jsonMapper.readTree("test")).thenReturn(node);
        Mockito.when(jsonMapper.readValue("test", IgniteEventImpl.class)).thenReturn(actualEvent);
        transformer.fromBlob("test".getBytes(), Optional.empty());

        assertNull(actualEvent.getSourceDeviceId());
        assertNull(actualEvent.getVehicleId());
        assertNull(actualEvent.getRequestId());

    }

    /**
     * Testing if the headers are not present.
     *
     * @throws JsonParseException JsonParseException
     * @throws JsonMappingException JsonMappingException
     * @throws IOException       I/O exception
     */
    
    @Test
    public void testHeaderSettingsWithEmptyHeaders() throws JsonParseException, JsonMappingException, IOException {

        IgniteEventImpl actualEvent = new IgniteEventImpl();
        JsonNode node = new ObjectNode(JsonNodeFactory.instance);

        IgniteEvent headerEvent = null;
        Mockito.when(jsonMapper.readTree("test")).thenReturn(node);
        Mockito.when(jsonMapper.readValue("test", IgniteEventImpl.class)).thenReturn(actualEvent);
        transformer.fromBlob("test".getBytes(), Optional.ofNullable(headerEvent));

        assertNull(actualEvent.getSourceDeviceId());
        assertNull(actualEvent.getVehicleId());
        assertNull(actualEvent.getRequestId());

    }

    /**
     * Testing with valid headers.
     *
     * @throws JsonParseException JsonParseException
     * @throws JsonMappingException JsonMappingException
     * @throws IOException I/O exception
     */
    
    @Test
    public void testHeaderSettingsWithValidHeaders() throws JsonParseException, JsonMappingException, IOException {

        IgniteEventImpl actualEvent = new IgniteEventImpl();
        JsonNode node = new ObjectNode(JsonNodeFactory.instance);

        IgniteEvent headerEvent = new IgniteEventImpl();
        ((IgniteEventImpl) headerEvent).setSourceDeviceId("testSource");
        ((IgniteEventImpl) headerEvent).setVehicleId("testVehicle");
        ((IgniteEventImpl) headerEvent).setRequestId("testRequest");

        Mockito.when(jsonMapper.readTree("test")).thenReturn(node);
        Mockito.when(jsonMapper.readValue("test", IgniteEventImpl.class)).thenReturn(actualEvent);
        transformer.fromBlob("test".getBytes(), Optional.ofNullable(headerEvent));

        Assert.assertEquals(actualEvent.getSourceDeviceId(), headerEvent.getSourceDeviceId());
        Assert.assertEquals(actualEvent.getVehicleId(), headerEvent.getVehicleId());
        Assert.assertEquals(actualEvent.getRequestId(), headerEvent.getRequestId());

    }

    /**
     * Testing with valid headers but null vehicle id.
     *
     * @throws JsonParseException JsonParseException
     * @throws JsonMappingException JsonMappingException
     * @throws IOException I/O exception
     */
    
    @Test
    public void testHeaderSettingsWithValidHeadersAndNullVehicleId()
            throws JsonParseException, JsonMappingException, IOException {
   
        IgniteEventImpl actualEvent = new IgniteEventImpl();
        JsonNode node = new ObjectNode(JsonNodeFactory.instance);

        IgniteEvent headerEvent = new IgniteEventImpl();
        ((IgniteEventImpl) headerEvent).setSourceDeviceId("testSource");
        ((IgniteEventImpl) headerEvent).setVehicleId(null);
        ((IgniteEventImpl) headerEvent).setRequestId("testRequest");

        Mockito.when(jsonMapper.readTree("test")).thenReturn(node);
        Mockito.when(jsonMapper.readValue("test", IgniteEventImpl.class)).thenReturn(actualEvent);
        transformer.fromBlob("test".getBytes(), Optional.ofNullable(headerEvent));

        Assert.assertEquals(actualEvent.getSourceDeviceId(), headerEvent.getSourceDeviceId());
        assertNull(actualEvent.getVehicleId());
        Assert.assertEquals(actualEvent.getRequestId(), headerEvent.getRequestId());
    }
}
