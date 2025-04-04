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

package org.eclipse.ecsp.serializer;

import org.eclipse.ecsp.entities.IgniteBlobEvent;

/**
 * This Interface provides methods for IngestionSerialization.
 *
 */
public interface IngestionSerializer {

    /**
     * Serialize the given IgniteBlobEvent object.
     *
     * @param obj the IgniteBlobEvent obj
     * @return the byte[]
     */
    byte[] serialize(IgniteBlobEvent obj);

    /**
     * Deserialize the given byte array into IgniteBlobEvent.
     *
     * @param b the byte array
     * @return the IgniteBlobEvent
     */
    IgniteBlobEvent deserialize(byte[] b);

    /**
     * Checks if the given byte array is serialized.
     *
     * @param b the b
     * @return true, if is serialized
     */
    boolean isSerialized(byte[] b);
}
