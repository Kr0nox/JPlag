package de.jplag.reporting.serialization;

import java.io.File;

import de.jplag.Language;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Provides utility functions for Jackson.
 */
public class JacksonUtils {
    private JacksonUtils() {
    }

    /**
     * Creates a new ObjectMapper configured to use JPlag specific serializers.
     * @return The object mapper
     */
    public static ObjectMapper createNewObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(File.class, new FileSerializer());
        module.addSerializer(Language.class, new LanguageSerializer());
        mapper.registerModule(module);
        return mapper;
    }
}
