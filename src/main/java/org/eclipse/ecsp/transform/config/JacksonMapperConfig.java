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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Customised jackson mapper initialization sot that it can be used by APIs as well as stream processors.
 * 
 *<p>
 * Many times we would like to add custom serde for our POJO, we can use this jackson mapper configuration to handle.
 *</p>
 */
@Configuration
public class JacksonMapperConfig {

    /** The logger. */
    IgniteLogger logger = IgniteLoggerFactory.getLogger(JacksonMapperConfig.class);

    /** The Constant CUSTOM_SERIALIZERS. */
    private static final String CUSTOM_SERIALIZERS = "custom.serializers";
    
    /** The Constant CUSTOM_DESERIALIZERS. */
    private static final String CUSTOM_DESERIALIZERS = "custom.deserializers";
    
    /** The Constant CUSTOM_SUBTYPES. */
    private static final String CUSTOM_SUBTYPES = "custom.subtypes";
    
    /** The Constant TWO. */
    public static final int TWO = 2;
    
    /** The mandatory serializer. */
    private final String mandatorySerializer
            = "org.eclipse.ecsp.entities.EventData:org.eclipse.ecsp.entities.EventDataDeSerializer";
    
    /** The comma. */
    private final String comma = ",";
    
    /** The colon. */
    private final String colon = ":";
    
    /** 
     * comma separated list of "className:Serializers" to be added to the
     * Jackson's ObjectMapper. Class and it's corresponding serializers has to
     * be separated by colon.
    */
    @Value("${" + CUSTOM_SERIALIZERS + ":}")
    private String customSerializers;

    /** 
     * comma separated list of "className:Deserializers" to be added to the
     * Jackson's ObjectMapper. Class and it's corresponding deserializers has to
     * be separated by colon.
     */
    @Value("${" + CUSTOM_DESERIALIZERS + ":}")
    private String customDeserializers;

    /** Sometimes you might want to add named subtypes. */
    @Value("${" + CUSTOM_SUBTYPES + ":}")
    private String customSubtypes;

    /**
     * Instantiates a new jackson mapper config.
     */
    @Autowired
    public JacksonMapperConfig() {

    }

    /**
     * Method for JacksonMapper Configuration.
     *
     * @param props the props
     */
    public JacksonMapperConfig(Properties props) {
        if (null != props.get(CUSTOM_SERIALIZERS)) {
            this.customSerializers = props.getProperty(CUSTOM_SERIALIZERS);
        }
        if (null != props.get(CUSTOM_DESERIALIZERS)) {
            this.customDeserializers = props.getProperty(CUSTOM_DESERIALIZERS);
        }
        if (null != props.get(CUSTOM_SUBTYPES)) {
            this.customSubtypes = props.getProperty(CUSTOM_SUBTYPES);
        }
        logger.info("Values loaded from properties for JacksonMapperConfig - "
                + "customSerializers :{}, customDeserializers: {},customSubtypes: {}",
                customSerializers, customDeserializers, customSubtypes);
    }

    /**
     * Creates and applies relevant settings on the ObjectMapper instance.
     * For example: Setting serializers, deserializers, etc. in the ObjectMapper instance.
     *
     * @return ObjectMapper : ObjectMapper
     */
    @Bean("jsonMapper")
    public ObjectMapper jsonObjectMapper() {
        ArrayList<Module> modules = new ArrayList<>();
        SimpleModule jacksonsModule = new SimpleModule();
        ObjectMapper objectMapper = null;

        try {
            if (!StringUtils.isEmpty(customSerializers)) {
                logger.info("Adding custom serializer:{}", customSerializers);
                add(jacksonsModule, customSerializers, JacksonConfigType.SERIALIZER);
            }

            String deserializersList = mandatorySerializer;

            if (!StringUtils.isEmpty(customDeserializers)) {
                deserializersList = mandatorySerializer + comma + customDeserializers;
            }

            logger.info("Adding  deserializer list:{}", deserializersList);
            add(jacksonsModule, deserializersList, JacksonConfigType.DESERIALIZER);

            // adding named subtypes if any
            if (!StringUtils.isEmpty(customSubtypes)) {
                logger.info("Adding custom named subtypes:{}", customSubtypes);
                add(jacksonsModule, customSubtypes, JacksonConfigType.SUBTYPE);
            }

            modules.add(jacksonsModule);

            objectMapper = new ObjectMapper();
            objectMapper.registerModules(modules);

            objectMapper.setSerializationInclusion(Include.NON_NULL);
            objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
            objectMapper.setSerializationInclusion(Include.NON_EMPTY);
            objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        } catch (Exception e) {
            logger.error("Exception while creating object mapper", e);
            throw new IllegalStateException("Exception while creating object mapper");
        }
        return objectMapper;

    }

    /**
     * Helper method to add the serializer and deserializer parameter to the
     * jackson module.
     *
     * @param jacksonModule the SimpleModule instance.
     * @param serdes SerDe class for specific types
     * @param type the JacksonConfigType
     * 
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws ClassNotFoundException the class not found exception
     * @throws InvocationTargetException the invocation target exception
     * @throws NoSuchMethodException the no such method exception
     */
    private void add(SimpleModule jacksonModule, String serdes, JacksonConfigType type)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException {
        if (StringUtils.isNotEmpty(serdes)) {
            String[] serdeList = serdes.split(comma);
            logger.info("Processing:{} for serde type:{}", serdes, type);
            for (String entry : serdeList) {
                processEachSerde(jacksonModule, type, entry);
            }
        }
    }

    /**
     * Process each serde. This is the helper method to add each SerDe to ObjectMapper instance.
     *
     * @param jacksonModule the SimpleModule instance
     * @param type JacksonConfigType 
     * @param serde the serde
     * @throws ClassNotFoundException the class not found exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws NoSuchMethodException the no such method exception
     */
    private void processEachSerde(SimpleModule jacksonModule, JacksonConfigType type, String serde)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        // entry can be null while running test cases.
        if (!StringUtils.isEmpty(serde) && !StringUtils.isEmpty(serde.trim())) {
            String[] serdesArray = serde.split(colon);
            if (serdesArray.length == TWO) {
                String className = serdesArray[0];
                if (StringUtils.isEmpty(className)) {
                    throw new IllegalStateException("Class name cannot be null or empty. Received entry as:" + serde);
                }

                addToJacksonModuleWithType(type, serdesArray, jacksonModule);
            } else {
                throw new IllegalStateException("Expecting key value pair separated by colon but received:" + serde);
            }
        }
    }

    /**
     * Adds the to jackson module with type. 
     * This is the helper method to add each SerDe to ObjectMapper instance.
     *
     * @param type the JacksonConfigType
     * @param serdesArray the serdes array
     * @param jacksonModule the SimpleModule
     * @throws ClassNotFoundException the class not found exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws NoSuchMethodException the no such method exception
     */
    private void addToJacksonModuleWithType(JacksonConfigType type, String[] serdesArray, SimpleModule jacksonModule)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        String className = serdesArray[0];
        String serdeName = serdesArray[1];
        switch (type) {
            case SERIALIZER:
                logger.info("Adding {} class and its serializer class:{} to jackson simple module",
                        className, serdeName);
                addSerializer(serdeName, jacksonModule);
                break;

            case DESERIALIZER:
                logger.info("Adding {} class and its deserializer class:{} to jackson simple module",
                        className, serdeName);
                addDeSerializer(serdeName, jacksonModule);
                break;

            case SUBTYPE:
                logger.info("Registering the named subtype with typename:{} and classname:{}",
                        serdeName, className);
                jacksonModule.registerSubtypes(new NamedType(Class.forName(className), serdeName));
                break;

            default:
                break;
        }
    }

    /**
     * Adds the serializer. This is the helper method to add each serializer to ObjectMapper instance.
     *
     * @param <T> the generic type
     * @param serdeName the serde name
     * @param jacksonModule the jackson module
     * @throws ClassNotFoundException the class not found exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     */
    private <T> void addSerializer(String serdeName, SimpleModule jacksonModule)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        JsonSerializer<T> serializer = (JsonSerializer<T>) Class.forName(serdeName).getClassLoader()
                .loadClass(serdeName).getDeclaredConstructor().newInstance();

        ResolvableType resolvableType = ResolvableType.forClass(JsonSerializer.class, serializer.getClass());
        jacksonModule.addSerializer((Class<T>) resolvableType.resolveGeneric(), serializer);
    }

    /**
     * Adds the de serializer. This is the helper method to add each deserializer to ObjectMapper instance.
     *
     * @param <T> the generic type
     * @param serdeName the serde name
     * @param jacksonModule the jackson module
     * @throws ClassNotFoundException the class not found exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     */
    private <T> void addDeSerializer(String serdeName, SimpleModule jacksonModule)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        JsonDeserializer<T> deserializer = (JsonDeserializer<T>) Class.forName(serdeName).getClassLoader()
                .loadClass(serdeName).getDeclaredConstructor().newInstance();
        ResolvableType resolvableType = ResolvableType.forClass(JsonDeserializer.class, deserializer.getClass());
        jacksonModule.addDeserializer((Class<T>) resolvableType.resolveGeneric(), deserializer);
    }

    /**
     * Gets the custom serializers.
     *
     * @return the custom serializers
     */
    String getCustomSerializers() {
        return customSerializers;
    }

    /**
     * Sets the custom serializers.
     *
     * @param customSerializers the new custom serializers
     */
    void setCustomSerializers(String customSerializers) {
        this.customSerializers = customSerializers;
    }

    /**
     * Gets the custom deserializers.
     *
     * @return the custom deserializers
     */
    String getCustomDeserializers() {
        return customDeserializers;
    }

    /**
     * Sets the custom deserializers.
     *
     * @param customDeserializers the new custom deserializers
     */
    void setCustomDeserializers(String customDeserializers) {
        this.customDeserializers = customDeserializers;
    }

    /**
     * Gets the custom subtypes.
     *
     * @return the custom subtypes
     */
    String getCustomSubtypes() {
        return customSubtypes;
    }

    /**
     * Sets the custom subtypes.
     *
     * @param customSubtypes the new custom subtypes
     */
    void setCustomSubtypes(String customSubtypes) {
        this.customSubtypes = customSubtypes;
    }

}
