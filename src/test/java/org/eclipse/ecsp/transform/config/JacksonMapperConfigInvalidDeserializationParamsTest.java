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

package org.eclipse.ecsp.transform.config;

import org.eclipse.ecsp.transform.config.JacksonMapperConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Test case for mapping class to deserializer.
 */

@RunWith(Parameterized.class)
public class JacksonMapperConfigInvalidDeserializationParamsTest {
    String customDeserializerList;

    public JacksonMapperConfigInvalidDeserializationParamsTest(String customDeserializerList) {
        this.customDeserializerList = customDeserializerList;
    }
    
    /**
     * Helper method to pass each item in the array as an argument to the test method.
     */
    
    @Parameterized.Parameters
    public static Collection<?> customDeserializerList() {
        return Arrays.asList(new Object[] { "org.eclipse.ecsp.transform.config.ItemToUserNameMapping:",
                                            ":org.eclipse.ecsp.transform.config.ItemToUserNameDeserializer", 
                                            "org.eclipse.ecsp.transform.config.ItemToUserNameMapping:"
                                            + "org.eclipse.ecsp.transform.config.ItemToUserNameMappingSerializer" });
    }

    /**
     * Negative scenario where proper pair is not given.
     */
    @Test(expected = IllegalStateException.class)
    public void testInsufficientDeSerializationParams() {

        JacksonMapperConfig config = new JacksonMapperConfig();
        config.setCustomDeserializers(customDeserializerList);

        // get the object mapper
        config.jsonObjectMapper();
    }
}
