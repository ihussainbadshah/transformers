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

import org.eclipse.ecsp.domain.AbstractBlobEventData.Encoding;
import org.eclipse.ecsp.domain.BlobDataV1_0;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteBlobEvent;
import org.eclipse.ecsp.serializer.IngestionSerializerFstImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

/**
 * Testing IngestionSerializerFSTImpl.
 *
 * @see IngestionSerializerFSTImpl
 */

public class IgniteFstConfigurationTest {

    /** The Constant Custom. */
    private static final String CUSTOM = "custom";
    
    /** The ifst conf. */
    IngestionSerializerFstImpl ifstConf;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        ifstConf = new IngestionSerializerFstImpl();
    }

    /**
     * Serialize test.
     */
    @Test
    public void serializeTest() {
        final int Two = 2;
        IgniteBlobEvent igniteBlobEvent = new IgniteBlobEvent();
        igniteBlobEvent.setSourceDeviceId("Device123");
        BlobDataV1_0 blobDataV10 = new BlobDataV1_0();
        blobDataV10.setEventSource(CUSTOM);
        blobDataV10.setEncoding(Encoding.GPB);
        blobDataV10.setPayload("Use actual protopuf object".getBytes());
        igniteBlobEvent.setEventData(blobDataV10);
        igniteBlobEvent.setRequestId("Request1");

        byte[] original = ifstConf.serialize(igniteBlobEvent);
        byte[] streamMagicBytes = Arrays.copyOfRange(original, 0, Two);
        System.out.println(streamMagicBytes[0] + " " + streamMagicBytes[1]);
        Assert.assertArrayEquals((byte[]) ReflectionTestUtils.getField(ifstConf, "STREAM_MAGIC_IN_BYTES"), 
                Arrays.copyOfRange(original, 0, Two));
    }

    /**
     * Deserialize test.
     */
    @Test
    public void deserializeTest() {
        IgniteBlobEvent igniteBlobEvent = new IgniteBlobEvent();
        igniteBlobEvent.setSourceDeviceId("Device123");
        BlobDataV1_0 blobDataV10 = new BlobDataV1_0();
        blobDataV10.setEventSource(CUSTOM);
        blobDataV10.setEncoding(Encoding.GPB);
        blobDataV10.setPayload("Use actual protopuf object".getBytes());
        igniteBlobEvent.setEventData(blobDataV10);
        igniteBlobEvent.setVersion(Version.V1_0);
        igniteBlobEvent.setRequestId("Request1");

        byte[] original = ifstConf.serialize(igniteBlobEvent);
        igniteBlobEvent = (IgniteBlobEvent) ifstConf.deserialize(original);
        Assert.assertEquals("Device123", igniteBlobEvent.getSourceDeviceId());
        Assert.assertEquals("Request1", igniteBlobEvent.getRequestId());
    }

    /**
     * Checks if the byte array isSerialed.
     */
    @Test
    public void isSerialedTest() {
        IgniteBlobEvent igniteBlobEvent = new IgniteBlobEvent();
        igniteBlobEvent.setSourceDeviceId("Device123");
        BlobDataV1_0 blobDataV10 = new BlobDataV1_0();
        blobDataV10.setEventSource(CUSTOM);
        blobDataV10.setEncoding(Encoding.GPB);
        blobDataV10.setPayload("Use actual protopuf object".getBytes());
        igniteBlobEvent.setEventData(blobDataV10);
        igniteBlobEvent.setVersion(Version.V1_0);
        igniteBlobEvent.setRequestId("Request1");

        byte[] original = ifstConf.serialize(igniteBlobEvent);

        Assert.assertTrue(ifstConf.isSerialized(original));
    }
}
