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
import org.eclipse.ecsp.entities.IgniteEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Optional;

/**
 * Unit Test class for DeviceMessageIgniteEventTransformer.
 *
 * @see DeviceMessageIgniteEventTransformer
 *
 */
public class DeviceMessageIgniteEventTransformerUnitTest {

    /** The mockito rule. */
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    /** The IgniteEvent. */
    @Mock
    IgniteEvent event;
    
    /** The DeviceMessageIgniteEventTransformer instance. */
    private DeviceMessageIgniteEventTransformer transformer;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        transformer = new DeviceMessageIgniteEventTransformer();
    }

    /**
     * Test fromBlob with null key value.
     */
    @Test(expected = RuntimeException.class)
    public void testFromBlobWithNullKeyValue() {
        transformer.fromBlob(null, Optional.empty());
    }

    /**
     * Testing null value to "toBlob" method.
     */
    @Test(expected = TransformerSerDeException.class)
    public void testNullIgniteEventValue() {
        transformer.toBlob(null);
    }

    /**
     * testing json exception while converting event to bytes.
     *
     * @throws JsonProcessingException exception
     */
    @Test(expected = TransformerSerDeException.class)
    public void testJsonException() throws JsonProcessingException {
        transformer.toBlob(event);
    }

}
