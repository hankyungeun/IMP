package com.bootest.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Objects {
    private static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(new JavaTimeModule());

    public static <T> T convert(Object obj, TypeReference<T> type) throws IllegalArgumentException {
        return mapper.convertValue(obj, type);
    }

    public static <T> T convertByJson(String json, TypeReference<T> type)
            throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(json, type);
    }
}