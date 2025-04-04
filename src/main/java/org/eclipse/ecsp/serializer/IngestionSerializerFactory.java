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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import java.lang.reflect.InvocationTargetException;

/**
 * Factory class that creates instance of IngestionSerializer.
 */
public class IngestionSerializerFactory {
  
    /** The instance. */
    private static IngestionSerializer instance;
    
    /** The Constant LOCK. */
    private static final Object LOCK = new Object();
    
    /** The Constant LOGGER. */
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(IngestionSerializerFactory.class);

    /**
     * Private constructor to not allow to instantiate IngestionSerializerFactory.
     */
    private IngestionSerializerFactory() {

    }
    
    /**
     * Returns the instance of the class for the given fully qualified class name.
     *
     * @param serializerClassName the serializer class name
     * @return single instance of IngestionSerializerFactory
     */
    
    public static IngestionSerializer getInstance(String serializerClassName) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    load(serializerClassName);
                }
            }
        }
        return instance;
    }
    
    /**
     * Instantiates the given class.
     *
     * @param className Fully qualified name of the class that needs to be instantiated.
     */
    private static void load(String className) {

        if (StringUtils.isEmpty(className)) {
            throw new IllegalArgumentException("Serializer class name cannot be null or empty.");
        }
        try {
            LOGGER.debug("Load IngestionSerializerFactory with classname {}", className);
            instance = (IngestionSerializer) IngestionSerializerFactory.class.getClassLoader().loadClass(className)
                    .getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException
                | NoSuchMethodException e) {
            LOGGER.error(className + "  is not available on the classpath");
            throw new IllegalArgumentException(className + "  is not available on the classpath");
        }
    }
}