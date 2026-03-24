package com.digitallib;

import com.digitallib.exception.RepositoryException;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import com.digitallib.model.entity.Entity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonGenerator {

    static Logger logger = LogManager.getLogger();

    public static String GenerateJsonFromDoc(Object obj) throws RepositoryException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            String jsonString = mapper.writeValueAsString(obj);
            logger.debug(jsonString);
            return jsonString;
        } catch (JsonProcessingException e) {
            throw new RepositoryException("Failed to serialize object to JSON: " + e.getOriginalMessage(), e);
        }
    }

    public static Documento GenerateDocFromJson(String jsonText) throws RepositoryException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            return mapper.readValue(jsonText, Documento.class);
        } catch (JsonProcessingException e) {
            throw new RepositoryException("Failed to deserialize Documento from JSON: " + e.getOriginalMessage(), e);
        }
    }

    public static Entity GenerateEntityFromJson(String jsonText) throws RepositoryException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            return mapper.readValue(jsonText, Entity.class);
        } catch (JsonProcessingException e) {
            throw new RepositoryException("Failed to deserialize Entity from JSON: " + e.getOriginalMessage(), e);
        }
    }

    public static MultiSourcedDocument GenerateMultiSourcedDocumentFromJson(String jsonText) throws RepositoryException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            return mapper.readValue(jsonText, MultiSourcedDocument.class);
        } catch (JsonProcessingException e) {
            throw new RepositoryException("Failed to deserialize MultiSourcedDocument from JSON: " + e.getOriginalMessage(), e);
        }
    }
}
