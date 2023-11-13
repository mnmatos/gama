package com.digitallib;

import com.digitallib.manager.EntityManager;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonGenerator {

    static Logger logger = LogManager.getLogger();
    public static String GenerateJsonFromDoc(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        logger.debug(jsonString);
        return jsonString;
    }

    public static Documento GenerateDocFromJson(String jsonText) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
           Documento documento = mapper.readValue(jsonText, Documento.class);
           return documento;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Entity GenerateEntityFromJson(String jsonText) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            Entity entity = mapper.readValue(jsonText, Entity.class);
            if (entity.getType().equals(EntityType.AUTOR)) { //TODO remove after all imports
                entity.setType(EntityType.PESSOA);
                EntityManager.updateEntry(entity);
            }
            return entity;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
