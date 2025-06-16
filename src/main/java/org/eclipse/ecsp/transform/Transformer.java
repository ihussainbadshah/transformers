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

import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventBase;
import org.eclipse.ecsp.key.IgniteKey;

import java.util.Optional;

/**
 * Transformer is used to convert from byte[] to IgniteEvent and vice versa.
 *
 * @author avadakkootko
 */
public interface Transformer {

    /**
     * Convert byte[] (ignite events) to IgniteEvent.
     * 
     * <p>
     * The optional header argument should be used to extract header info like requestId, vehicleId etc.
     *
     * byte[] value will be either a custom protocol like GPB or JSON serialized
     * (like in case of Ignite Standard approach)
     * </p>
     *
     * @param value : byte[]
     * @param header : Optional of {@link IgniteEventBase}
     * @return igniteEvent : IgniteEvent
     */
    public default IgniteEvent fromBlob(byte[] value, Optional<IgniteEventBase> header) {
        return null;
    }
    
    /**
     *Convert byte[] (ignite events) to IgniteEvent.
     *The key argument could be used to extract info like
     * sourceDeviceID.
     *byte[] value will be either a custom protocol like GPB or JSON serialized
     *(like in case of Ignite Standard approach).
     *
     *@param value The byte array.
     *@param igniteKey The IgniteKey
     *@return The IgniteEvent constructed from byte[].
     */
    public default IgniteEvent fromBlob(byte[] value, IgniteKey<?> igniteKey) {
        return null;
    }

    /**
     * Convert IgniteEvent to byte[] (java serialized or JSON byte array).
     *
     * @param value : IgniteEvent
     * @return IgniteEvent : byte[] of IgniteEvent
     */
    public byte[] toBlob(IgniteEvent value);

    /**
     * Source will determine which implementation of the transformer should be used.
     *
     * @return source : String
     */
    public String getSource();

}