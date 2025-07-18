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
import org.eclipse.ecsp.entities.IgniteDeviceAwareBlobEvent;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.util.FSTUtil;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.util.Properties;

/**
 * This Class provides method implementation of IngestionSerializer.
 *
 */

public class IngestionSerializerFstImpl implements IngestionSerializer {
    
    /** The Constant STREAM_MAGIC_IN_BYTES. */
    public static final byte[] STREAM_MAGIC_IN_BYTES;
    
    /** The Constant STREAM_MAGIC_STR. */
    public static final String STREAM_MAGIC_STR = "aced"; // 0xaced get from
    
    /** The Constant STREAM_MAGIC_BYTES_LEN. */
    // ObjectStreamConstants.STREAM_MAGIC
    public static final int STREAM_MAGIC_BYTES_LEN = 2;
    
    /** The Constant DEVICE_AWARE_ENABLED. */
    private static final String DEVICE_AWARE_ENABLED = "device.aware.enabled";
    
    /** The Constant STUB_ENTITIES_PREFIX. */
    private static final String STUB_ENTITIES_PREFIX = "com.harman.ignite.entities.";
    
    /** The Constant REAL_ENTITIES_PREFIX. */
    private static final String REAL_ENTITIES_PREFIX = "org.eclipse.ecsp.entities.";
    
    /** The Constant STUB_DOMAIN_PREFIX. */
    private static final String STUB_DOMAIN_PREFIX = "com.harman.ignite.domain.";
    
    /** The Constant REAL_DOMAIN_PREFIX. */
    private static final String REAL_DOMAIN_PREFIX = "org.eclipse.ecsp.domain.";
    
    /** The Constant TWO. */
    public static final int TWO = 2;
    
    /** The Constant THREE. */
    public static final int THREE = 3;
    
    /** The Constant SIXTEEN. */
    public static final int SIXTEEN = 16;
    
    /** The Constant FOUR. */
    public static final int FOUR = 4;
    
    /** The Constant HEX_BASE_16. */
    public static final int HEX_BASE_16 = 0xff;
    
    /** The Constant EIGHT. */
    public static final int EIGHT = 8;
    
    /** The logger. */
    private static IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(IngestionSerializerFstImpl.class);
    
    /** The FSTConfiguration instance which contains the default configuration. */
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    
    /** The properties. */
    private static Properties properties;

    static {
        // Do not use shortpath for the common string while doing serialization.
        conf.setShareReferences(false);
        // convert the hex aced to byte[]
        STREAM_MAGIC_IN_BYTES = hexStringToByteArray(STREAM_MAGIC_STR);
    }

    /**
     * Hex string to byte array.
     *
     * @param s the string
     * @return the byte[]
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / TWO];
        for (int i = 0; i < len; i += TWO) {
            data[i / TWO] = (byte) ((Character.digit(s.charAt(i), SIXTEEN) << FOUR)
                    + Character.digit(s.charAt(i + 1), SIXTEEN));
        }

        return data;
    }

    /**
     * Load properties from system env into Properties object.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void loadProperties() throws IOException {

        if (ObjectUtils.isEmpty(properties)) {
            properties = new Properties();
            LOGGER.info("Loading properties from environment");
            System.getenv().forEach((k, v) -> properties.setProperty(k.replace("_", "."), v));
            System.getProperties().forEach((k, v) -> properties.setProperty(k.toString(), v.toString()));
            LOGGER.info("device.aware.enabled:" + properties.get(DEVICE_AWARE_ENABLED));
        }
    }

    /**
     * Serialize the given IgniteBlobEvent obj.
     *
     * @param obj the obj
     * @return the byte[]
     */
    @Override
    public byte[] serialize(IgniteBlobEvent obj) {
        FSTObjectOutput objectOutput = conf.getObjectOutput();
        try {
            // Add the magic bytes in output stream.
            objectOutput.write(STREAM_MAGIC_IN_BYTES);
            objectOutput.writeObject(obj);
            return objectOutput.getCopyOfWrittenBuffer();
        } catch (IOException e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return new byte[0];
    }

    /**
     * Deserialize the given byte array.
     *
     * @param b the byte array
     * @return the IgniteBlobEvent object
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public IgniteBlobEvent deserialize(byte[] b) {
        try {
            // discard the first two magic bytes from input stream.
            loadProperties();
            boolean deviceAwareEnableFlag = Boolean.parseBoolean((String) properties.get(DEVICE_AWARE_ENABLED));
            Object object = (Object) conf.getObjectInputCopyFrom(b, STREAM_MAGIC_BYTES_LEN, 
                    b.length - STREAM_MAGIC_BYTES_LEN).readObject();

            if (object instanceof org.eclipse.ecsp.entities.IgniteBlobEvent) {
                return (IgniteBlobEvent) object;
            } 
            if (object instanceof org.eclipse.ecsp.entities.IgniteDeviceAwareBlobEvent) {
                return (IgniteDeviceAwareBlobEvent) object;
            } else if (object != null && object.getClass().getName().startsWith(STUB_ENTITIES_PREFIX)) {
                // Map stub to real class and copy fields using reflection
                org.eclipse.ecsp.entities.IgniteBlobEvent real;
                if (deviceAwareEnableFlag) {
                    real = new org.eclipse.ecsp.entities.IgniteDeviceAwareBlobEvent(null, null);
                } else {
                    real = new org.eclipse.ecsp.entities.IgniteBlobEvent();
                }
                try {
                    java.lang.reflect.Method[] getters = object.getClass().getMethods();
                    java.lang.reflect.Method[] setters = real.getClass().getMethods();
                    for (java.lang.reflect.Method getter : getters) {
                        String name = getter.getName();
                        if ((name.startsWith("get") || name.startsWith("is")) 
                                && getter.getParameterCount() == 0 
                                && !getter.getReturnType().equals(void.class)) {
                            Object value = getter.invoke(object);
                            // Recursively map nested stubs for both .entities and .domain
                            if (value != null) {
                                String valueClassName = value.getClass().getName();
                                if (valueClassName.startsWith(STUB_ENTITIES_PREFIX) 
                                        || valueClassName.startsWith(STUB_DOMAIN_PREFIX)) {
                                    value = deepMapToRealClass(value);
                                }
                            }
                            String setterName = name.startsWith("is") 
                                    ? "set" + name.substring(TWO) : "set" + name.substring(THREE);
                            for (java.lang.reflect.Method setter : setters) {
                                if (setter.getName().equals(setterName) && setter.getParameterCount() == 1) {
                                    try {
                                        setter.invoke(real, value);
                                    } catch (Exception ignore) {
                                        // Ignore any exception during setter invocation, continue with next field
                                        LOGGER.debug("Failed to invoke setter: " + setterName + " for value: " + value);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error mapping stub to real class during FST deserialization", e);
                    throw e;
                }
                return real;
            }
        } catch (Exception e) {
            FSTUtil.<RuntimeException>rethrow(e);
        }
        return null;
    }

    /**
     * Checks if the given byte array is serialized.
     *
     * @param bytes the byte array
     * @return true, if it is serialized
     */
    @Override
    public boolean isSerialized(byte[] bytes) {
        // Convert to short from reading the first two bytes from byte array
        short sm = (short) (((bytes[0] & HEX_BASE_16) << EIGHT) + (bytes[1] & HEX_BASE_16));
        return sm == ObjectStreamConstants.STREAM_MAGIC;
    }
    
    /**
     * Utility method to deep map the stub object to its real class.
     *
     * @param stubObj the stub object
     * @return the real class object
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private Object deepMapToRealClass(Object stubObj) {
        if (stubObj == null) {
            return null;
        }
        Class<?> stubClass = stubObj.getClass();
        String stubClassName = stubClass.getName();
        String realClassName = null;
        if (stubClassName.startsWith(STUB_ENTITIES_PREFIX)) {
            realClassName = REAL_ENTITIES_PREFIX + stubClassName.substring(STUB_ENTITIES_PREFIX.length());
        } else if (stubClassName.startsWith(STUB_DOMAIN_PREFIX)) {
            realClassName = REAL_DOMAIN_PREFIX + stubClassName.substring(STUB_DOMAIN_PREFIX.length());
        } else {
            // Not a stub, return as is
            return stubObj;
        }
        try {
            Class<?> realClass = Class.forName(realClassName);
            // Handle enum mapping
            if (realClass.isEnum() && stubClass.isEnum()) {
                return Enum.valueOf((Class<Enum>) realClass, ((Enum<?>) stubObj).name());
            }
            Object realObj = realClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method[] getters = stubClass.getMethods();
            java.lang.reflect.Method[] setters = realClass.getMethods();
            for (java.lang.reflect.Method getter : getters) {
                String name = getter.getName();
                if ((name.startsWith("get") || name.startsWith("is")) 
                        && getter.getParameterCount() == 0 
                        && !getter.getReturnType().equals(void.class)) {
                    Object value = getter.invoke(stubObj);
                    // Recursively map nested stubs for both .entities and .domain
                    if (value != null) {
                        String valueClassName = value.getClass().getName();
                        if (valueClassName.startsWith(STUB_ENTITIES_PREFIX) 
                                || valueClassName.startsWith(STUB_DOMAIN_PREFIX)) {
                            value = deepMapToRealClass(value);
                        }
                    }
                    String setterName = name.startsWith("is") 
                            ? "set" + name.substring(TWO) : "set" + name.substring(THREE);
                    for (java.lang.reflect.Method setter : setters) {
                        if (setter.getName().equals(setterName) && setter.getParameterCount() == 1) {
                            setter.invoke(realObj, value);
                            break;
                        }
                    }
                }
            }
            return realObj;
        } catch (Exception e) {
            LOGGER.error("Error mapping stub to real class during FST deserialization", e);
        }
        return null;
    }
}
